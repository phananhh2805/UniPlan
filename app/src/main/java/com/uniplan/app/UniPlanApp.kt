package com.uniplan.app

import android.app.Application
import com.uniplan.app.notification.NotificationFactory
import com.uniplan.app.notification.ReminderScheduler
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltAndroidApp
class UniPlanApp : Application() {

    @Inject lateinit var notificationFactory: NotificationFactory
    @Inject lateinit var reminderScheduler: ReminderScheduler

    override fun onCreate() {
        super.onCreate()
        notificationFactory.createNotificationChannels()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                reminderScheduler.rescheduleAll()
            } catch (_: Exception) {}
        }
    }
}
