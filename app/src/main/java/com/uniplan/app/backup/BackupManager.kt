package com.uniplan.app.backup

import androidx.room.withTransaction
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.uniplan.app.data.local.db.*
import com.uniplan.app.data.local.prefs.PeriodConfigStore
import com.uniplan.app.data.local.prefs.PeriodDto
import com.uniplan.app.domain.model.CoinSource
import com.uniplan.app.domain.model.CoinTier
import com.uniplan.app.domain.model.Priority
import com.uniplan.app.domain.model.TaskStatus
import com.uniplan.app.notification.ReminderScheduler
import com.uniplan.app.util.DateTimeProvider
import kotlinx.coroutines.flow.first
import java.time.LocalDateTime
import java.time.YearMonth
import java.util.Random

data class BackupDto(
    val schemaVersion: Int = 1,
    val appVersion: String = "1.0.0",
    val exportedAt: String,
    val periodConfig: List<PeriodDto>,
    val semesters: List<SemesterEntity>,
    val subjects: List<SubjectEntity>,
    val classSessions: List<ClassSessionEntity>,
    val classExceptions: List<ClassExceptionEntity>,
    val tasks: List<TaskEntity>,
    val subtasks: List<SubtaskEntity>,
    val exams: List<ExamEntity>,
    val timeBlocks: List<TimeBlockEntity>,
    val reminders: List<ReminderEntity>,
    val focusSessions: List<FocusSessionEntity>,
    val coins: List<CoinEntity>? = null,
    val jarMonths: List<JarMonthEntity>? = null
)

class BackupManager(
    private val db: AppDatabase,
    private val periodConfigStore: PeriodConfigStore,
    private val reminderScheduler: ReminderScheduler,
    private val dateTimeProvider: DateTimeProvider,
    private val gson: Gson
) {

    suspend fun exportJson(): String {
        val now = dateTimeProvider.now().toString()
        val periods = PeriodConfigStore.defaultPeriods()

        val backup = BackupDto(
            exportedAt = now,
            periodConfig = periods,
            semesters = db.semesterDao().observeAll().first(),
            subjects = listOf(),
            classSessions = listOf(),
            classExceptions = listOf(),
            tasks = db.taskDao().observeAll().first(),
            subtasks = listOf(),
            exams = listOf(),
            timeBlocks = listOf(),
            reminders = db.reminderDao().getAllEnabled(),
            focusSessions = listOf(),
            coins = listOf(),
            jarMonths = listOf()
        )

        return GsonBuilder().setPrettyPrinting().create().toJson(backup)
    }

    suspend fun importJson(jsonStr: String): Boolean = db.withTransaction {
        val backup = try {
            gson.fromJson(jsonStr, BackupDto::class.java)
        } catch (e: Exception) {
            return@withTransaction false
        }

        if (backup == null || backup.schemaVersion > 1) {
            return@withTransaction false
        }

        db.scheduledAlarmDao().deleteAll()

        for (s in backup.semesters) db.semesterDao().insert(s)
        for (s in backup.subjects) db.subjectDao().insert(s)
        for (cs in backup.classSessions) db.classSessionDao().insert(cs)
        for (ce in backup.classExceptions) db.classExceptionDao().upsert(ce)
        for (t in backup.tasks) db.taskDao().insert(t)
        for (st in backup.subtasks) db.subtaskDao().insert(st)
        for (e in backup.exams) db.examDao().insert(e)
        for (tb in backup.timeBlocks) db.timeBlockDao().insert(tb)
        for (r in backup.reminders) db.reminderDao().insert(r)
        for (fs in backup.focusSessions) db.focusSessionDao().insert(fs)

        if (backup.coins != null) {
            for (c in backup.coins) db.coinDao().insert(c)
        } else {
            val doneTasks = backup.tasks.filter { it.status == TaskStatus.DONE && it.completedAt != null }
            for (t in doneTasks) {
                val earnedAt = LocalDateTime.now().toString()
                val monthKey = YearMonth.now().toString()
                val tier = when (t.priority) {
                    Priority.LOW -> CoinTier.BRONZE
                    Priority.MEDIUM -> CoinTier.SILVER
                    Priority.HIGH -> CoinTier.GOLD
                }
                db.coinDao().insert(
                    CoinEntity(
                        monthKey = monthKey,
                        earnedAt = earnedAt,
                        tier = tier,
                        sourceType = CoinSource.TASK,
                        sourceId = t.id,
                        label = t.title.take(120),
                        subjectId = t.subjectId,
                        subjectName = null,
                        subjectColor = null,
                        isOnTime = true,
                        seed = Random().nextInt(),
                        isSeen = true
                    )
                )
            }
        }

        if (backup.jarMonths != null) {
            for (jm in backup.jarMonths) db.jarMonthDao().insertOrUpdate(jm)
        }

        reminderScheduler.rescheduleAll()
        true
    }
}
