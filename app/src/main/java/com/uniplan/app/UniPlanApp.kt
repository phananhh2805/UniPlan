package com.uniplan.app

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import androidx.room.Room
import com.google.gson.GsonBuilder
import com.uniplan.app.backup.BackupManager
import com.uniplan.app.data.local.db.AppDatabase
import com.uniplan.app.data.local.prefs.PeriodConfigStore
import com.uniplan.app.data.local.prefs.SettingsDataStore
import com.uniplan.app.data.repository.*
import com.uniplan.app.domain.usecase.CheckConflictUseCase
import com.uniplan.app.domain.usecase.GetDayTimelineUseCase
import com.uniplan.app.domain.usecase.GroupTasksByPriorityUseCase
import com.uniplan.app.domain.usecase.OccurrenceGenerator
import com.uniplan.app.notification.ReminderScheduler
import com.uniplan.app.util.DefaultDateTimeProvider

class UniPlanApp : Application() {

    lateinit var database: AppDatabase
    lateinit var settingsDataStore: SettingsDataStore
    lateinit var periodConfigStore: PeriodConfigStore
    lateinit var dateTimeProvider: DefaultDateTimeProvider

    lateinit var occurrenceGenerator: OccurrenceGenerator
    lateinit var checkConflictUseCase: CheckConflictUseCase
    lateinit var getDayTimelineUseCase: GetDayTimelineUseCase
    lateinit var groupTasksByPriorityUseCase: GroupTasksByPriorityUseCase

    lateinit var reminderScheduler: ReminderScheduler

    lateinit var semesterRepository: SemesterRepository
    lateinit var subjectRepository: SubjectRepository
    lateinit var scheduleRepository: ScheduleRepository
    lateinit var taskRepository: TaskRepository
    lateinit var examRepository: ExamRepository
    lateinit var timeBlockRepository: TimeBlockRepository
    lateinit var focusRepository: FocusRepository
    lateinit var coinJarRepository: CoinJarRepository
    lateinit var backupManager: BackupManager

    override fun onCreate() {
        super.onCreate()
        instance = this
        createNotificationChannels()
        initContainer()
    }

    private fun initContainer() {
        val gson = GsonBuilder().create()
        database = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java,
            "uniplan_db"
        ).fallbackToDestructiveMigration().build()

        settingsDataStore = SettingsDataStore(applicationContext, gson)
        periodConfigStore = PeriodConfigStore(applicationContext, gson)
        dateTimeProvider = DefaultDateTimeProvider()

        occurrenceGenerator = OccurrenceGenerator()
        checkConflictUseCase = CheckConflictUseCase()
        getDayTimelineUseCase = GetDayTimelineUseCase()
        groupTasksByPriorityUseCase = GroupTasksByPriorityUseCase()

        reminderScheduler = ReminderScheduler(
            applicationContext,
            database,
            database.reminderDao(),
            database.scheduledAlarmDao(),
            database.semesterDao(),
            database.subjectDao(),
            database.classSessionDao(),
            database.classExceptionDao(),
            database.taskDao(),
            database.examDao(),
            occurrenceGenerator,
            dateTimeProvider
        )

        semesterRepository = SemesterRepository(database, database.semesterDao(), reminderScheduler)
        subjectRepository = SubjectRepository(database.subjectDao())
        scheduleRepository = ScheduleRepository(
            database,
            database.classSessionDao(),
            database.classExceptionDao(),
            database.subjectDao(),
            database.semesterDao(),
            occurrenceGenerator,
            reminderScheduler
        )
        taskRepository = TaskRepository(
            database,
            database.taskDao(),
            database.subtaskDao(),
            database.coinDao(),
            database.jarMonthDao(),
            settingsDataStore,
            reminderScheduler,
            dateTimeProvider
        )
        examRepository = ExamRepository(database, database.examDao(), reminderScheduler)
        timeBlockRepository = TimeBlockRepository(database.timeBlockDao())
        focusRepository = FocusRepository(
            database,
            database.focusSessionDao(),
            database.coinDao(),
            database.jarMonthDao(),
            settingsDataStore,
            dateTimeProvider
        )
        coinJarRepository = CoinJarRepository(database.coinDao(), database.jarMonthDao())
        backupManager = BackupManager(
            database,
            periodConfigStore,
            reminderScheduler,
            dateTimeProvider,
            gson
        )
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = getSystemService(NotificationManager::class.java)

            val classChannel = NotificationChannel(
                CHANNEL_CLASS,
                getString(R.string.channel_class_name),
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = getString(R.string.channel_class_desc)
            }

            val deadlineChannel = NotificationChannel(
                CHANNEL_DEADLINE,
                getString(R.string.channel_deadline_name),
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = getString(R.string.channel_deadline_desc)
            }

            val examChannel = NotificationChannel(
                CHANNEL_EXAM,
                getString(R.string.channel_exam_name),
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = getString(R.string.channel_exam_desc)
            }

            val focusChannel = NotificationChannel(
                CHANNEL_FOCUS,
                getString(R.string.channel_focus_name),
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = getString(R.string.channel_focus_desc)
                setSound(null, null)
            }

            notificationManager?.createNotificationChannels(
                listOf(classChannel, deadlineChannel, examChannel, focusChannel)
            )
        }
    }

    companion object {
        const val CHANNEL_CLASS = "class_reminder"
        const val CHANNEL_DEADLINE = "deadline_reminder"
        const val CHANNEL_EXAM = "exam_reminder"
        const val CHANNEL_FOCUS = "focus_timer"

        lateinit var instance: UniPlanApp
            private set
    }
}
