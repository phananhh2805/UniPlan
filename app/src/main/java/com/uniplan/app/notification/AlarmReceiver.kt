package com.uniplan.app.notification

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.app.NotificationCompat
import com.uniplan.app.R
import com.uniplan.app.UniPlanApp
import com.uniplan.app.domain.model.AlarmState
import com.uniplan.app.domain.model.ExceptionStatus
import com.uniplan.app.domain.model.ReminderOwner
import com.uniplan.app.domain.model.TaskStatus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime

class AlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val alarmId = intent.getLongExtra(EXTRA_ALARM_ID, -1L)
        if (alarmId == -1L) return

        val app = UniPlanApp.instance
        val scheduledAlarmDao = app.database.scheduledAlarmDao()
        val reminderDao = app.database.reminderDao()
        val taskDao = app.database.taskDao()
        val examDao = app.database.examDao()
        val subjectDao = app.database.subjectDao()
        val classSessionDao = app.database.classSessionDao()
        val classExceptionDao = app.database.classExceptionDao()
        val semesterDao = app.database.semesterDao()
        val occurrenceGenerator = app.occurrenceGenerator
        val settingsDataStore = app.settingsDataStore

        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val alarm = scheduledAlarmDao.getById(alarmId)
                if (alarm == null || alarm.state != AlarmState.SCHEDULED) {
                    pendingResult.finish()
                    return@launch
                }

                val reminder = reminderDao.getById(alarm.reminderId)
                if (reminder == null || !reminder.isEnabled) {
                    scheduledAlarmDao.update(alarm.copy(state = AlarmState.CANCELED))
                    pendingResult.finish()
                    return@launch
                }

                val dndEnabled = settingsDataStore.dndEnabled.first()
                val dndStartStr = settingsDataStore.dndStart.first()
                val dndEndStr = settingsDataStore.dndEnd.first()
                val isDndNow = isDndTime(LocalTime.now(), dndEnabled, dndStartStr, dndEndStr)

                when (reminder.ownerType) {
                    ReminderOwner.CLASS -> {
                        val session = classSessionDao.getById(reminder.ownerId)
                        if (session == null || !session.isActive) {
                            scheduledAlarmDao.update(alarm.copy(state = AlarmState.CANCELED))
                            pendingResult.finish()
                            return@launch
                        }
                        val subject = subjectDao.getById(session.subjectId)
                        val sem = subject?.let { semesterDao.getById(it.semesterId) }
                        if (subject != null && sem != null) {
                            val date = LocalDate.parse(alarm.targetDate)
                            val exceptions = classExceptionDao.getByDate(date.toString())
                            val occurrences = occurrenceGenerator.forDate(sem, mapOf(subject.id to subject), listOf(session), exceptions, date)
                            val occ = occurrences.find { it.sessionId == session.id && it.status != ExceptionStatus.CANCELED }

                            if (occ != null) {
                                val title = "${subject.name} bắt đầu sau ${reminder.offsetMin} phút"
                                var body = "${occ.startTime}–${occ.endTime}"
                                if (!occ.room.isNullOrEmpty()) body += " · Phòng ${occ.room}"
                                if (!occ.campus.isNullOrEmpty()) body += ", ${occ.campus}"

                                val deepLink = Intent(Intent.ACTION_VIEW, Uri.parse("uniplan://class/${session.id}/${alarm.targetDate}"))
                                showNotification(context, alarmId.toInt(), UniPlanApp.CHANNEL_CLASS, title, body, deepLink, isDndNow, null)
                                scheduledAlarmDao.update(alarm.copy(state = AlarmState.FIRED))
                            } else {
                                scheduledAlarmDao.update(alarm.copy(state = AlarmState.CANCELED))
                            }
                        }
                    }
                    ReminderOwner.TASK -> {
                        val task = taskDao.getById(reminder.ownerId)
                        if (task == null || task.status == TaskStatus.DONE) {
                            scheduledAlarmDao.update(alarm.copy(state = AlarmState.CANCELED))
                            pendingResult.finish()
                            return@launch
                        }
                        val title = "Sắp đến hạn: ${task.title}"
                        val body = if (!task.dueAt.isNullOrEmpty()) "Hạn ${task.dueAt.replace('T', ' ')}" else ""
                        val deepLink = Intent(Intent.ACTION_VIEW, Uri.parse("uniplan://task/${task.id}"))

                        val actions = listOf(
                            NotificationCompat.Action.Builder(
                                0, "Xong",
                                createActionPendingIntent(context, ReminderActionReceiver.ACTION_DONE, task.id, alarmId)
                            ).build(),
                            NotificationCompat.Action.Builder(
                                0, "Hoãn 10 phút",
                                createActionPendingIntent(context, ReminderActionReceiver.ACTION_SNOOZE_10, task.id, alarmId)
                            ).build()
                        )

                        showNotification(context, alarmId.toInt(), UniPlanApp.CHANNEL_DEADLINE, title, body, deepLink, isDndNow, actions)
                        scheduledAlarmDao.update(alarm.copy(state = AlarmState.FIRED))
                    }
                    ReminderOwner.EXAM -> {
                        val exam = examDao.getById(reminder.ownerId)
                        if (exam == null) {
                            scheduledAlarmDao.update(alarm.copy(state = AlarmState.CANCELED))
                            pendingResult.finish()
                            return@launch
                        }
                        val subject = subjectDao.getById(exam.subjectId)
                        val title = "Lịch thi ${subject?.name ?: ""}"
                        var body = "${exam.examType} · ${exam.startAt.replace('T', ' ')}"
                        if (!exam.room.isNullOrEmpty()) body += " · Phòng ${exam.room}"
                        val deepLink = Intent(Intent.ACTION_VIEW, Uri.parse("uniplan://exam/${exam.id}"))

                        showNotification(context, alarmId.toInt(), UniPlanApp.CHANNEL_EXAM, title, body, deepLink, isDndNow, null)
                        scheduledAlarmDao.update(alarm.copy(state = AlarmState.FIRED))
                    }
                    ReminderOwner.BLOCK -> {}
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                pendingResult.finish()
            }
        }
    }

    private fun isDndTime(now: LocalTime, enabled: Boolean, startStr: String, endStr: String): Boolean {
        if (!enabled) return false
        return try {
            val start = LocalTime.parse(startStr)
            val end = LocalTime.parse(endStr)
            if (start.isBefore(end)) {
                now.isAfter(start) && now.isBefore(end)
            } else {
                now.isAfter(start) || now.isBefore(end)
            }
        } catch (e: Exception) {
            false
        }
    }

    private fun showNotification(
        context: Context,
        notificationId: Int,
        channelId: String,
        title: String,
        body: String,
        contentIntent: Intent,
        silent: Boolean,
        actions: List<NotificationCompat.Action>?
    ) {
        val pendingContentIntent = PendingIntent.getActivity(
            context,
            notificationId,
            contentIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(body)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingContentIntent)

        if (silent) {
            builder.setSilent(true)
        }

        actions?.forEach { builder.addAction(it) }

        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        nm.notify(notificationId, builder.build())
    }

    private fun createActionPendingIntent(context: Context, action: String, taskId: Long, alarmId: Long): PendingIntent {
        val intent = Intent(context, ReminderActionReceiver::class.java).apply {
            this.action = action
            putExtra(ReminderActionReceiver.EXTRA_TASK_ID, taskId)
            putExtra(ReminderActionReceiver.EXTRA_ALARM_ID, alarmId)
        }
        return PendingIntent.getBroadcast(
            context,
            (taskId * 100 + action.hashCode()).toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    companion object {
        const val ACTION_FIRE_ALARM = "com.uniplan.app.ACTION_FIRE_ALARM"
        const val EXTRA_ALARM_ID = "extra_alarm_id"
    }
}
