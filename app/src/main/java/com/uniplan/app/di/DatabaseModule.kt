package com.uniplan.app.di

import android.content.Context
import androidx.room.Room
import com.google.gson.Gson
import com.uniplan.app.data.local.db.*
import com.uniplan.app.util.DefaultDateTimeProvider
import com.uniplan.app.util.DateTimeProvider
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideGson(): Gson = Gson()

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "uniplan.db"
        ).build()
    }

    @Provides fun provideSemesterDao(db: AppDatabase): SemesterDao = db.semesterDao()
    @Provides fun provideSubjectDao(db: AppDatabase): SubjectDao = db.subjectDao()
    @Provides fun provideClassSessionDao(db: AppDatabase): ClassSessionDao = db.classSessionDao()
    @Provides fun provideClassExceptionDao(db: AppDatabase): ClassExceptionDao = db.classExceptionDao()
    @Provides fun provideTaskDao(db: AppDatabase): TaskDao = db.taskDao()
    @Provides fun provideSubtaskDao(db: AppDatabase): SubtaskDao = db.subtaskDao()
    @Provides fun provideExamDao(db: AppDatabase): ExamDao = db.examDao()
    @Provides fun provideTimeBlockDao(db: AppDatabase): TimeBlockDao = db.timeBlockDao()
    @Provides fun provideReminderDao(db: AppDatabase): ReminderDao = db.reminderDao()
    @Provides fun provideScheduledAlarmDao(db: AppDatabase): ScheduledAlarmDao = db.scheduledAlarmDao()
    @Provides fun provideFocusSessionDao(db: AppDatabase): FocusSessionDao = db.focusSessionDao()
    @Provides fun provideCoinDao(db: AppDatabase): CoinDao = db.coinDao()
    @Provides fun provideJarMonthDao(db: AppDatabase): JarMonthDao = db.jarMonthDao()
}

@Module
@InstallIn(SingletonComponent::class)
abstract class UtilityModule {
    @Binds
    @Singleton
    abstract fun bindDateTimeProvider(impl: DefaultDateTimeProvider): DateTimeProvider
}
