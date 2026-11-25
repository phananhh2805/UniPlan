package com.uniplan.app.notification

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.uniplan.app.data.local.db.AppDatabase
import com.uniplan.app.data.local.db.ScheduledAlarmEntity
import com.uniplan.app.domain.model.AlarmState
import com.uniplan.app.domain.model.ReminderOwner
import com.uniplan.app.domain.model.TaskStatus
import com.uniplan.app.util.DateTimeProvider
import dagger.hilt.android.qualifiers.ApplicationContext
import java.time.LocalDateTime
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ReminderScheduler @Inject constructor(
    @ApplicationContext private val context: Context,
    private val db: AppDatabase,
    private val dateTimeProvider: DateTimeProvider
) {
    companion object {
        const val ACTION_FIRE_ALARM = "com.uniplan.app.ACTION_FIRE_ALARM"
        const val EXTRA_ALARM_ID = "extra_alarm_id"
    }

    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    suspend fun rescheduleAll() {
        val reminders = db.reminderDao().getAllEnabled()
        for (r in reminders) {
            rescheduleOwner(r.ownerType, r.ownerId)
        }
    }

    suspend fun rescheduleOwner(ownerType: ReminderOwner, ownerId: Long) {
        val windowEndMs = dateTimeProvider.nowMillis() + 7 * 24 * 60 * 60 * 1000L
        val reminders = db.reminderDao().getByOwner(ownerType, ownerId).filter { it.isEnabled }

        for (r in reminders) {
            when (r.ownerType) {
                ReminderOwner.TASK -> {
                    val task = db.taskDao().getById(r.ownerId)
                    if (task != null && task.status != TaskStatus.DONE && task.dueAt != null) {
                        val dueMs = LocalDateTime.parse(task.dueAt)
                            .atZone(dateTimeProvider.zone())
                            .toInstant()
                            .toEpochMilli()
                        val fireAt = dueMs - (r.offsetMin * 60 * 1000L)
                        if (fireAt in (dateTimeProvider.nowMillis() + 1000)..windowEndMs) {
                            scheduleAlarm(r.id, task.dueAt.take(10), fireAt)
                        }
                    }
                }
                ReminderOwner.EXAM -> {
                    val exam = db.examDao().getById(r.ownerId)
                    if (exam != null) {
                        val startMs = LocalDateTime.parse(exam.startAt)
                            .atZone(dateTimeProvider.zone())
                            .toInstant()
                            .toEpochMilli()
                        val fireAt = startMs - (r.offsetMin * 60 * 1000L)
                        if (fireAt in (dateTimeProvider.nowMillis() + 1000)..windowEndMs) {
                            scheduleAlarm(r.id, exam.startAt.take(10), fireAt)
                        }
                    }
                }
                ReminderOwner.CLASS -> {
                    // Handled by occurrence generator when calculating class occurrences
                }
                ReminderOwner.BLOCK -> {}
            }
        }
    }

    private suspend fun scheduleAlarm(reminderId: Long, targetDate: String, fireAtMs: Long) {
        val alarm = ScheduledAlarmEntity(
            reminderId = reminderId,
            targetDate = targetDate,
            fireAt = fireAtMs,
            state = AlarmState.SCHEDULED
        )
        val alarmId = db.scheduledAlarmDao().insert(alarm)

        val intent = Intent(context, AlarmReceiver::class.java).apply {
            action = ACTION_FIRE_ALARM
            putExtra(EXTRA_ALARM_ID, alarmId)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            alarmId.toInt(),
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val canExact = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            alarmManager.canScheduleExactAlarms()
        } else {
            true
        }

        if (canExact) {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, fireAtMs, pendingIntent)
        } else {
            alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, fireAtMs, pendingIntent)
        }
    }

    suspend fun snooze10Minutes(reminderId: Long) {
        val fireAt = dateTimeProvider.nowMillis() + 10 * 60 * 1000L
        val todayStr = dateTimeProvider.today().toString()
        scheduleAlarm(reminderId, todayStr, fireAt)
    }

    suspend fun cancelAlarmsOf(ownerType: ReminderOwner, ownerId: Long) {
        val reminders = db.reminderDao().getByOwner(ownerType, ownerId)
        val reminderIds = reminders.map { it.id }
        if (reminderIds.isEmpty()) return

        val scheduled = db.scheduledAlarmDao().scheduledFor(reminderIds)
        for (alarm in scheduled) {
            val intent = Intent(context, AlarmReceiver::class.java).apply { action = ACTION_FIRE_ALARM }
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                alarm.id.toInt(),
                intent,
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_NO_CREATE
            )
            if (pendingIntent != null) {
                alarmManager.cancel(pendingIntent)
                pendingIntent.cancel()
            }
            db.scheduledAlarmDao().updateState(alarm.id, AlarmState.CANCELED)
        }
    }
}
