package com.uniplan.app.notification

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.uniplan.app.data.local.db.*
import com.uniplan.app.data.local.db.dao.*
import com.uniplan.app.domain.model.AlarmState
import com.uniplan.app.domain.model.ExceptionStatus
import com.uniplan.app.domain.model.ReminderOwner
import com.uniplan.app.domain.model.TaskStatus
import com.uniplan.app.domain.usecase.OccurrenceGenerator
import com.uniplan.app.util.DateTimeProvider
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId

class ReminderScheduler(
    private val context: Context,
    private val db: AppDatabase,
    private val reminderDao: ReminderDao,
    private val scheduledAlarmDao: ScheduledAlarmDao,
    private val semesterDao: SemesterDao,
    private val subjectDao: SubjectDao,
    private val classSessionDao: ClassSessionDao,
    private val classExceptionDao: ClassExceptionDao,
    private val taskDao: TaskDao,
    private val examDao: ExamDao,
    private val occurrenceGenerator: OccurrenceGenerator,
    private val dateTimeProvider: DateTimeProvider
) {
    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    suspend fun rescheduleForOwner(ownerType: ReminderOwner, ownerId: Long) {
        val reminders = reminderDao.getByOwner(ownerType, ownerId).filter { it.isEnabled }
        val now = dateTimeProvider.now()
        val zone = dateTimeProvider.zone()
        val windowEnd = now.plusDays(7)

        val desiredAlarms = mutableListOf<DesiredAlarm>()

        for (rem in reminders) {
            when (ownerType) {
                ReminderOwner.CLASS -> {
                    val session = classSessionDao.getById(ownerId) ?: continue
                    val subject = subjectDao.getById(session.subjectId) ?: continue
                    val sem = semesterDao.getById(subject.semesterId) ?: continue

                    val subjectsMap = mapOf(subject.id to subject)
                    val sessions = listOf(session)

                    var date = now.toLocalDate()
                    val endDate = windowEnd.toLocalDate()

                    while (!date.isAfter(endDate)) {
                        val exceptions = classExceptionDao.getByDate(date.toString())
                        val occurrences = occurrenceGenerator.forDate(sem, subjectsMap, sessions, exceptions, date)
                        val occ = occurrences.find { it.sessionId == session.id && it.status != ExceptionStatus.CANCELED }

                        if (occ != null) {
                            val eventTime = LocalDateTime.of(date, occ.startTime)
                            val fireAtTime = eventTime.minusMinutes(rem.offsetMin.toLong())
                            val fireAtMs = fireAtTime.atZone(zone).toInstant().toEpochMilli()

                            if (fireAtTime.isAfter(now) && fireAtTime.isBefore(windowEnd)) {
                                desiredAlarms.add(DesiredAlarm(rem.id, date.toString(), fireAtMs))
                            }
                        }
                        date = date.plusDays(1)
                    }
                }
                ReminderOwner.TASK -> {
                    val task = taskDao.getById(ownerId) ?: continue
                    if (task.status != TaskStatus.DONE && !task.dueAt.isNullOrEmpty()) {
                        val dueAt = LocalDateTime.parse(task.dueAt)
                        val fireAtTime = dueAt.minusMinutes(rem.offsetMin.toLong())
                        val fireAtMs = fireAtTime.atZone(zone).toInstant().toEpochMilli()

                        if (fireAtTime.isAfter(now) && fireAtTime.isBefore(windowEnd)) {
                            desiredAlarms.add(DesiredAlarm(rem.id, dueAt.toLocalDate().toString(), fireAtMs))
                        }
                    }
                }
                ReminderOwner.EXAM -> {
                    val exam = examDao.getById(ownerId) ?: continue
                    val startAt = LocalDateTime.parse(exam.startAt)
                    val fireAtTime = startAt.minusMinutes(rem.offsetMin.toLong())
                    val fireAtMs = fireAtTime.atZone(zone).toInstant().toEpochMilli()

                    if (fireAtTime.isAfter(now) && fireAtTime.isBefore(windowEnd)) {
                        desiredAlarms.add(DesiredAlarm(rem.id, startAt.toLocalDate().toString(), fireAtMs))
                    }
                }
                ReminderOwner.BLOCK -> { /* TimeBlock reminders reserved */ }
            }
        }

        // Compare desired alarms with existing scheduled alarms
        val existingScheduled = scheduledAlarmDao.scheduledFor(reminders.map { it.id })

        for (existing in existingScheduled) {
            val matched = desiredAlarms.find {
                it.reminderId == existing.reminderId &&
                        it.targetDate == existing.targetDate &&
                        Math.abs(it.fireAt - existing.fireAt) < 1000
            }
            if (matched == null) {
                cancelAlarmPendingIntent(existing.id)
                scheduledAlarmDao.update(existing.copy(state = AlarmState.CANCELED))
            }
        }

        for (desired in desiredAlarms) {
            val alreadyScheduled = existingScheduled.any {
                it.reminderId == desired.reminderId &&
                        it.targetDate == desired.targetDate &&
                        Math.abs(it.fireAt - desired.fireAt) < 1000 &&
                        it.state == AlarmState.SCHEDULED
            }
            if (!alreadyScheduled) {
                val entity = ScheduledAlarmEntity(
                    reminderId = desired.reminderId,
                    targetDate = desired.targetDate,
                    fireAt = desired.fireAt,
                    state = AlarmState.SCHEDULED
                )
                val alarmId = scheduledAlarmDao.insert(entity)
                scheduleExactSystemAlarm(alarmId, desired.fireAt)
            }
        }
    }

    suspend fun rescheduleAll() {
        val allReminders = reminderDao.getAllEnabled()
        val owners = allReminders.map { Pair(it.ownerType, it.ownerId) }.distinct()
        for ((ownerType, ownerId) in owners) {
            rescheduleForOwner(ownerType, ownerId)
        }
        val purgeBefore = dateTimeProvider.nowMillis() - 7 * 86400000L
        scheduledAlarmDao.purgeOld(purgeBefore)
    }

    suspend fun cancelAlarmsForOwner(ownerType: ReminderOwner, ownerId: Long) {
        val reminders = reminderDao.getByOwner(ownerType, ownerId)
        val existingScheduled = scheduledAlarmDao.scheduledFor(reminders.map { it.id })
        for (sa in existingScheduled) {
            cancelAlarmPendingIntent(sa.id)
            scheduledAlarmDao.update(sa.copy(state = AlarmState.CANCELED))
        }
    }

    private fun scheduleExactSystemAlarm(alarmId: Long, fireAtMs: Long) {
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            action = AlarmReceiver.ACTION_FIRE_ALARM
            putExtra(AlarmReceiver.EXTRA_ALARM_ID, alarmId)
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            alarmId.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
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

    private fun cancelAlarmPendingIntent(alarmId: Long) {
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            action = AlarmReceiver.ACTION_FIRE_ALARM
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            alarmId.toInt(),
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        if (pendingIntent != null) {
            alarmManager.cancel(pendingIntent)
            pendingIntent.cancel()
        }
    }

    private data class DesiredAlarm(
        val reminderId: Long,
        val targetDate: String,
        val fireAt: Long
    )
}
