package com.uniplan.app.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.core.app.NotificationCompat
import com.uniplan.app.R
import com.uniplan.app.ui.main.MainActivity
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationFactory @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        const val CHANNEL_CLASS = "class_reminder"
        const val CHANNEL_DEADLINE = "deadline_reminder"
        const val CHANNEL_EXAM = "exam_reminder"
        const val CHANNEL_FOCUS = "focus_timer"

        const val ACTION_MARK_DONE = "com.uniplan.app.ACTION_MARK_DONE"
        const val ACTION_SNOOZE = "com.uniplan.app.ACTION_SNOOZE"
        const val EXTRA_TASK_ID = "extra_task_id"
        const val EXTRA_REMINDER_ID = "extra_reminder_id"
        const val EXTRA_NOTIFICATION_ID = "extra_notification_id"
    }

    fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            val chClass = NotificationChannel(
                CHANNEL_CLASS,
                "Nhắc lịch học",
                NotificationManager.IMPORTANCE_HIGH
            ).apply { description = "Thông báo nhắc giờ học" }

            val chDeadline = NotificationChannel(
                CHANNEL_DEADLINE,
                "Nhắc deadline",
                NotificationManager.IMPORTANCE_HIGH
            ).apply { description = "Thông báo nhắc hạn nộp nhiệm vụ" }

            val chExam = NotificationChannel(
                CHANNEL_EXAM,
                "Nhắc lịch thi",
                NotificationManager.IMPORTANCE_HIGH
            ).apply { description = "Thông báo nhắc lịch thi" }

            val chFocus = NotificationChannel(
                CHANNEL_FOCUS,
                "Đồng hồ tập trung",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Thông báo đồng hồ tập trung"
                setSound(null, null)
            }

            nm.createNotificationChannels(listOf(chClass, chDeadline, chExam, chFocus))
        }
    }

    fun buildClassNotification(
        notificationId: Int,
        title: String,
        content: String,
        deepLinkUrl: String
    ): NotificationCompat.Builder {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(deepLinkUrl), context, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            context,
            notificationId,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        return NotificationCompat.Builder(context, CHANNEL_CLASS)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(content)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
    }

    fun buildDeadlineNotification(
        notificationId: Int,
        taskId: Long,
        reminderId: Long,
        title: String,
        content: String,
        deepLinkUrl: String
    ): NotificationCompat.Builder {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(deepLinkUrl), context, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            context,
            notificationId,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val doneIntent = Intent(context, ReminderActionReceiver::class.java).apply {
            action = ACTION_MARK_DONE
            putExtra(EXTRA_TASK_ID, taskId)
            putExtra(EXTRA_NOTIFICATION_ID, notificationId)
        }
        val donePendingIntent = PendingIntent.getBroadcast(
            context,
            notificationId * 10 + 1,
            doneIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val snoozeIntent = Intent(context, ReminderActionReceiver::class.java).apply {
            action = ACTION_SNOOZE
            putExtra(EXTRA_REMINDER_ID, reminderId)
            putExtra(EXTRA_NOTIFICATION_ID, notificationId)
        }
        val snoozePendingIntent = PendingIntent.getBroadcast(
            context,
            notificationId * 10 + 2,
            snoozeIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        return NotificationCompat.Builder(context, CHANNEL_DEADLINE)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(content)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .addAction(0, "Xong", donePendingIntent)
            .addAction(0, "Hoãn 10 phút", snoozePendingIntent)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
    }

    fun buildExamNotification(
        notificationId: Int,
        title: String,
        content: String,
        deepLinkUrl: String
    ): NotificationCompat.Builder {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(deepLinkUrl), context, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            context,
            notificationId,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        return NotificationCompat.Builder(context, CHANNEL_EXAM)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(content)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
    }
}
