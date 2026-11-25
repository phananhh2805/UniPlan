package com.uniplan.app.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationManagerCompat
import com.uniplan.app.data.local.db.AppDatabase
import com.uniplan.app.domain.model.AlarmState
import com.uniplan.app.domain.model.ReminderOwner
import com.uniplan.app.domain.model.TaskStatus
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class AlarmReceiver : BroadcastReceiver() {

    @Inject lateinit var db: AppDatabase
    @Inject lateinit var notificationFactory: NotificationFactory

    override fun onReceive(context: Context, intent: Intent) {
        val alarmId = intent.getLongExtra(ReminderScheduler.EXTRA_ALARM_ID, -1L)
        if (alarmId == -1L) return

        val pendingResult = goAsync()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val alarm = db.scheduledAlarmDao().getById(alarmId) ?: return@launch
                if (alarm.state != AlarmState.SCHEDULED) return@launch

                val reminder = db.reminderDao().getByOwner(ReminderOwner.TASK, alarm.reminderId).firstOrNull()
                    ?: db.reminderDao().getByOwner(ReminderOwner.EXAM, alarm.reminderId).firstOrNull()
                    ?: db.reminderDao().getByOwner(ReminderOwner.CLASS, alarm.reminderId).firstOrNull()

                if (reminder == null || !reminder.isEnabled) {
                    db.scheduledAlarmDao().updateState(alarmId, AlarmState.CANCELED)
                    return@launch
                }

                when (reminder.ownerType) {
                    ReminderOwner.TASK -> {
                        val task = db.taskDao().getById(reminder.ownerId)
                        if (task != null && task.status != TaskStatus.DONE) {
                            val builder = notificationFactory.buildDeadlineNotification(
                                notificationId = alarmId.toInt(),
                                taskId = task.id,
                                reminderId = reminder.id,
                                title = "Sắp đến hạn: ${task.title}",
                                content = "Hạn ${task.dueAt?.replace('T', ' ') ?: ""}",
                                deepLinkUrl = "uniplan://task/${task.id}"
                            )
                            try {
                                NotificationManagerCompat.from(context).notify(alarmId.toInt(), builder.build())
                            } catch (_: SecurityException) {}
                        }
                    }
                    ReminderOwner.EXAM -> {
                        val exam = db.examDao().getById(reminder.ownerId)
                        if (exam != null) {
                            val subject = db.subjectDao().getById(exam.subjectId)
                            val builder = notificationFactory.buildExamNotification(
                                notificationId = alarmId.toInt(),
                                title = "Lịch thi ${subject?.name ?: ""}",
                                content = "${exam.examType} · ${exam.startAt.replace('T', ' ')}",
                                deepLinkUrl = "uniplan://exam/${exam.id}"
                            )
                            try {
                                NotificationManagerCompat.from(context).notify(alarmId.toInt(), builder.build())
                            } catch (_: SecurityException) {}
                        }
                    }
                    ReminderOwner.CLASS -> {}
                    ReminderOwner.BLOCK -> {}
                }

                db.scheduledAlarmDao().updateState(alarmId, AlarmState.FIRED)
            } finally {
                pendingResult.finish()
            }
        }
    }
}
