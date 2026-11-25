package com.uniplan.app.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationManagerCompat
import com.uniplan.app.data.repository.TaskRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class ReminderActionReceiver : BroadcastReceiver() {

    @Inject lateinit var taskRepository: TaskRepository
    @Inject lateinit var reminderScheduler: ReminderScheduler

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action ?: return
        val notifId = intent.getIntExtra(NotificationFactory.EXTRA_NOTIFICATION_ID, -1)

        val pendingResult = goAsync()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                if (notifId != -1) {
                    NotificationManagerCompat.from(context).cancel(notifId)
                }

                when (action) {
                    NotificationFactory.ACTION_MARK_DONE -> {
                        val taskId = intent.getLongExtra(NotificationFactory.EXTRA_TASK_ID, -1)
                        if (taskId != -1L) {
                            taskRepository.markTaskDone(taskId)
                        }
                    }
                    NotificationFactory.ACTION_SNOOZE -> {
                        val reminderId = intent.getLongExtra(NotificationFactory.EXTRA_REMINDER_ID, -1)
                        if (reminderId != -1L) {
                            reminderScheduler.snooze10Minutes(reminderId)
                        }
                    }
                }
            } finally {
                pendingResult.finish()
            }
        }
    }
}
