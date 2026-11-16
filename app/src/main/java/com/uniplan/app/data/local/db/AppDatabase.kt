package com.uniplan.app.data.local.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.uniplan.app.data.local.db.dao.*

@Database(
    entities = [
        SemesterEntity::class,
        SubjectEntity::class,
        ClassSessionEntity::class,
        ClassExceptionEntity::class,
        TaskEntity::class,
        SubtaskEntity::class,
        ExamEntity::class,
        TimeBlockEntity::class,
        ReminderEntity::class,
        ScheduledAlarmEntity::class,
        FocusSessionEntity::class,
        CoinEntity::class,
        JarMonthEntity::class
    ],
    version = 1,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun semesterDao(): SemesterDao
    abstract fun subjectDao(): SubjectDao
    abstract fun classSessionDao(): ClassSessionDao
    abstract fun classExceptionDao(): ClassExceptionDao
    abstract fun taskDao(): TaskDao
    abstract fun subtaskDao(): SubtaskDao
    abstract fun examDao(): ExamDao
    abstract fun timeBlockDao(): TimeBlockDao
    abstract fun reminderDao(): ReminderDao
    abstract fun scheduledAlarmDao(): ScheduledAlarmDao
    abstract fun focusSessionDao(): FocusSessionDao
    abstract fun coinDao(): CoinDao
    abstract fun jarMonthDao(): JarMonthDao
}
