package com.uniplan.app.data.local.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.uniplan.app.domain.model.*

@Entity(tableName = "semester")
data class SemesterEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    @ColumnInfo(name = "start_date") val startDate: String,
    @ColumnInfo(name = "end_date") val endDate: String,
    @ColumnInfo(name = "is_current") val isCurrent: Boolean = false
)

@Entity(
    tableName = "subject",
    foreignKeys = [
        ForeignKey(
            entity = SemesterEntity::class,
            parentColumns = ["id"],
            childColumns = ["semester_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("semester_id")]
)
data class SubjectEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "semester_id") val semesterId: Long,
    val code: String? = null,
    val name: String,
    val credits: Int? = null,
    val teacher: String? = null,
    val color: Int,
    val icon: String = "book",
    val note: String? = null
)

@Entity(
    tableName = "class_session",
    foreignKeys = [
        ForeignKey(
            entity = SubjectEntity::class,
            parentColumns = ["id"],
            childColumns = ["subject_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["subject_id", "day_of_week"])]
)
data class ClassSessionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "subject_id") val subjectId: Long,
    @ColumnInfo(name = "day_of_week") val dayOfWeek: Int,
    @ColumnInfo(name = "start_period") val startPeriod: Int? = null,
    @ColumnInfo(name = "end_period") val endPeriod: Int? = null,
    @ColumnInfo(name = "start_time") val startTime: String,
    @ColumnInfo(name = "end_time") val endTime: String,
    val room: String? = null,
    val campus: String? = null,
    val type: ClassType = ClassType.LECTURE,
    @ColumnInfo(name = "week_from") val weekFrom: Int,
    @ColumnInfo(name = "week_to") val weekTo: Int,
    @ColumnInfo(name = "week_pattern") val weekPattern: WeekPattern = WeekPattern.ALL,
    @ColumnInfo(name = "is_active") val isActive: Boolean = true
)

@Entity(
    tableName = "class_exception",
    foreignKeys = [
        ForeignKey(
            entity = ClassSessionEntity::class,
            parentColumns = ["id"],
            childColumns = ["session_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["session_id", "date"], unique = true)]
)
data class ClassExceptionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "session_id") val sessionId: Long,
    val date: String,
    val status: ExceptionStatus,
    @ColumnInfo(name = "new_room") val newRoom: String? = null,
    @ColumnInfo(name = "new_start_time") val newStartTime: String? = null,
    @ColumnInfo(name = "new_end_time") val newEndTime: String? = null,
    val note: String? = null
)

@Entity(
    tableName = "task",
    foreignKeys = [
        ForeignKey(
            entity = SubjectEntity::class,
            parentColumns = ["id"],
            childColumns = ["subject_id"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [Index("subject_id"), Index(value = ["status", "due_at"])]
)
data class TaskEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "subject_id") val subjectId: Long? = null,
    val title: String,
    val description: String? = null,
    @ColumnInfo(name = "due_at") val dueAt: String? = null,
    val priority: Priority = Priority.MEDIUM,
    val status: TaskStatus = TaskStatus.TODO,
    @ColumnInfo(name = "task_type") val taskType: TaskType = TaskType.ASSIGNMENT,
    @ColumnInfo(name = "estimate_min") val estimateMin: Int? = null,
    @ColumnInfo(name = "created_at") val createdAt: Long,
    @ColumnInfo(name = "updated_at") val updatedAt: Long,
    @ColumnInfo(name = "completed_at") val completedAt: Long? = null
)

@Entity(
    tableName = "subtask",
    foreignKeys = [
        ForeignKey(
            entity = TaskEntity::class,
            parentColumns = ["id"],
            childColumns = ["task_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("task_id")]
)
data class SubtaskEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "task_id") val taskId: Long,
    val title: String,
    @ColumnInfo(name = "is_done") val isDone: Boolean = false,
    val position: Int = 0
)

@Entity(
    tableName = "exam",
    foreignKeys = [
        ForeignKey(
            entity = SubjectEntity::class,
            parentColumns = ["id"],
            childColumns = ["subject_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("start_at")]
)
data class ExamEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "subject_id") val subjectId: Long,
    @ColumnInfo(name = "exam_type") val examType: String,
    @ColumnInfo(name = "start_at") val startAt: String,
    @ColumnInfo(name = "duration_min") val durationMin: Int,
    val room: String? = null,
    val note: String? = null
)

@Entity(
    tableName = "time_block",
    foreignKeys = [
        ForeignKey(
            entity = TaskEntity::class,
            parentColumns = ["id"],
            childColumns = ["task_id"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [Index("date")]
)
data class TimeBlockEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "task_id") val taskId: Long? = null,
    val title: String,
    val date: String,
    @ColumnInfo(name = "start_time") val startTime: String,
    @ColumnInfo(name = "end_time") val endTime: String,
    val category: BlockCategory = BlockCategory.STUDY,
    val color: Int,
    val icon: String = "calendar",
    @ColumnInfo(name = "repeat_rule") val repeatRule: RepeatRule = RepeatRule.NONE,
    @ColumnInfo(name = "repeat_until") val repeatUntil: String? = null
)

@Entity(
    tableName = "reminder",
    indices = [Index(value = ["owner_type", "owner_id", "offset_min"], unique = true)]
)
data class ReminderEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "owner_type") val ownerType: ReminderOwner,
    @ColumnInfo(name = "owner_id") val ownerId: Long,
    @ColumnInfo(name = "offset_min") val offsetMin: Int,
    @ColumnInfo(name = "is_enabled") val isEnabled: Boolean = true
)

@Entity(
    tableName = "scheduled_alarm",
    foreignKeys = [
        ForeignKey(
            entity = ReminderEntity::class,
            parentColumns = ["id"],
            childColumns = ["reminder_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["fire_at", "state"])]
)
data class ScheduledAlarmEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "reminder_id") val reminderId: Long,
    @ColumnInfo(name = "target_date") val targetDate: String,
    @ColumnInfo(name = "fire_at") val fireAt: Long,
    val state: AlarmState = AlarmState.SCHEDULED
)

@Entity(
    tableName = "focus_session",
    foreignKeys = [
        ForeignKey(
            entity = TaskEntity::class,
            parentColumns = ["id"],
            childColumns = ["task_id"],
            onDelete = ForeignKey.SET_NULL
        ),
        ForeignKey(
            entity = SubjectEntity::class,
            parentColumns = ["id"],
            childColumns = ["subject_id"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [Index("started_at")]
)
data class FocusSessionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "task_id") val taskId: Long? = null,
    @ColumnInfo(name = "subject_id") val subjectId: Long? = null,
    @ColumnInfo(name = "started_at") val startedAt: Long,
    @ColumnInfo(name = "planned_min") val plannedMin: Int,
    @ColumnInfo(name = "actual_sec") val actualSec: Int,
    @ColumnInfo(name = "is_completed") val isCompleted: Boolean = false
)

@Entity(
    tableName = "coin",
    indices = [Index("month_key")]
)
data class CoinEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "month_key") val monthKey: String,
    @ColumnInfo(name = "earned_at") val earnedAt: String,
    val tier: CoinTier,
    @ColumnInfo(name = "source_type") val sourceType: CoinSource,
    @ColumnInfo(name = "source_id") val sourceId: Long? = null,
    val label: String,
    @ColumnInfo(name = "subject_id") val subjectId: Long? = null,
    @ColumnInfo(name = "subject_name") val subjectName: String? = null,
    @ColumnInfo(name = "subject_color") val subjectColor: Int? = null,
    @ColumnInfo(name = "is_on_time") val isOnTime: Boolean = true,
    val seed: Int,
    @ColumnInfo(name = "is_seen") val isSeen: Boolean = false
)

@Entity(tableName = "jar_month")
data class JarMonthEntity(
    @PrimaryKey @ColumnInfo(name = "month_key") val monthKey: String,
    val goal: Int
)

data class SubjectTime(
    val subjectId: Long?,
    val totalSec: Int
)
