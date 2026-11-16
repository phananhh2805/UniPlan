package com.uniplan.app.notification

import android.app.AlarmManager
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import com.uniplan.app.UniPlanApp
import com.uniplan.app.data.local.db.CoinEntity
import com.uniplan.app.data.local.db.ScheduledAlarmEntity
import com.uniplan.app.domain.model.AlarmState
import com.uniplan.app.domain.model.CoinSource
import com.uniplan.app.domain.model.CoinTier
import com.uniplan.app.domain.model.Priority
import com.uniplan.app.domain.model.TaskStatus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.YearMonth
import java.util.Random

class ReminderActionReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val taskId = intent.getLongExtra(EXTRA_TASK_ID, -1L)
        val alarmId = intent.getLongExtra(EXTRA_ALARM_ID, -1L)

        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (alarmId != -1L) {
            nm.cancel(alarmId.toInt())
        }

        if (taskId == -1L) return

        val app = UniPlanApp.instance
        val taskDao = app.database.taskDao()
        val scheduledAlarmDao = app.database.scheduledAlarmDao()
        val coinDao = app.database.coinDao()
        val jarMonthDao = app.database.jarMonthDao()
        val settingsDataStore = app.settingsDataStore
        val dateTimeProvider = app.dateTimeProvider

        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                when (intent.action) {
                    ACTION_DONE -> {
                        val task = taskDao.getById(taskId)
                        if (task != null && task.status != TaskStatus.DONE) {
                            val now = dateTimeProvider.now()
                            val nowMs = dateTimeProvider.nowMillis()
                            taskDao.update(task.copy(status = TaskStatus.DONE, completedAt = nowMs, updatedAt = nowMs))

                            if (coinDao.findBySource(CoinSource.TASK, taskId) == null) {
                                val monthKey = YearMonth.from(now.toLocalDate()).toString()
                                val tier = when (task.priority) {
                                    Priority.LOW -> CoinTier.BRONZE
                                    Priority.MEDIUM -> CoinTier.SILVER
                                    Priority.HIGH -> CoinTier.GOLD
                                }
                                val defaultGoal = settingsDataStore.jarDefaultGoal.first()
                                jarMonthDao.ensure(monthKey, defaultGoal)

                                val isOnTime = task.dueAt.isNullOrEmpty() || !now.isAfter(LocalDateTime.parse(task.dueAt))
                                coinDao.insert(
                                    CoinEntity(
                                        monthKey = monthKey,
                                        earnedAt = now.toString(),
                                        tier = tier,
                                        sourceType = CoinSource.TASK,
                                        sourceId = taskId,
                                        label = task.title.take(120),
                                        subjectId = task.subjectId,
                                        subjectName = null,
                                        subjectColor = null,
                                        isOnTime = isOnTime,
                                        seed = Random().nextInt(),
                                        isSeen = false
                                    )
                                )
                            }
                        }
                    }
                    ACTION_SNOOZE_10 -> {
                        if (alarmId != -1L) {
                            val alarm = scheduledAlarmDao.getById(alarmId)
                            if (alarm != null) {
                                val snoozeTimeMs = dateTimeProvider.nowMillis() + 10 * 60 * 1000L
                                val newAlarm = ScheduledAlarmEntity(
                                    reminderId = alarm.reminderId,
                                    targetDate = alarm.targetDate,
                                    fireAt = snoozeTimeMs,
                                    state = AlarmState.SCHEDULED
                                )
                                val newAlarmId = scheduledAlarmDao.insert(newAlarm)

                                val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
                                val fireIntent = Intent(context, AlarmReceiver::class.java).apply {
                                    action = AlarmReceiver.ACTION_FIRE_ALARM
                                    putExtra(AlarmReceiver.EXTRA_ALARM_ID, newAlarmId)
                                }
                                val pi = PendingIntent.getBroadcast(
                                    context,
                                    newAlarmId.toInt(),
                                    fireIntent,
                                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                                )

                                val canExact = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                                    alarmManager.canScheduleExactAlarms()
                                } else {
                                    true
                                }

                                if (canExact) {
                                    alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, snoozeTimeMs, pi)
                                } else {
                                    alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, snoozeTimeMs, pi)
                                }
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                pendingResult.finish()
            }
        }
    }

    companion object {
        const val ACTION_DONE = "com.uniplan.app.ACTION_DONE"
        const val ACTION_SNOOZE_10 = "com.uniplan.app.ACTION_SNOOZE_10"
        const val EXTRA_TASK_ID = "extra_task_id"
        const val EXTRA_ALARM_ID = "extra_alarm_id"
    }
}
