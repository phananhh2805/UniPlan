package com.uniplan.app.data.repository

import androidx.room.withTransaction
import com.uniplan.app.data.local.db.*
import com.uniplan.app.data.local.db.dao.*
import com.uniplan.app.data.local.prefs.SettingsDataStore
import com.uniplan.app.domain.model.*
import com.uniplan.app.domain.usecase.OccurrenceGenerator
import com.uniplan.app.notification.ReminderScheduler
import com.uniplan.app.util.DateTimeProvider
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.YearMonth
import java.util.Random

class SemesterRepository(
    private val db: AppDatabase,
    private val semesterDao: SemesterDao,
    private val reminderScheduler: ReminderScheduler
) {
    fun observeAll(): Flow<List<SemesterEntity>> = semesterDao.observeAll()
    fun observeCurrent(): Flow<SemesterEntity?> = semesterDao.observeCurrent()

    suspend fun getById(id: Long): SemesterEntity? = semesterDao.getById(id)

    suspend fun saveSemester(semester: SemesterEntity): Long {
        return if (semester.id == 0L) {
            val id = semesterDao.insert(semester)
            if (semester.isCurrent) {
                semesterDao.setSingleCurrent(id)
            }
            id
        } else {
            semesterDao.update(semester)
            if (semester.isCurrent) {
                semesterDao.setSingleCurrent(semester.id)
            }
            semester.id
        }
    }

    suspend fun setCurrent(id: Long) {
        semesterDao.setSingleCurrent(id)
    }

    suspend fun deleteSemester(semester: SemesterEntity) = db.withTransaction {
        val subjects = db.subjectDao().getBySemester(semester.id)
        for (sub in subjects) {
            val sessions = db.classSessionDao().getBySubject(sub.id)
            for (cs in sessions) {
                reminderScheduler.cancelAlarmsForOwner(ReminderOwner.CLASS, cs.id)
            }
            val exams = db.examDao().getBySemester(semester.id)
            for (ex in exams) {
                reminderScheduler.cancelAlarmsForOwner(ReminderOwner.EXAM, ex.id)
            }
            db.taskDao().clearSubjectAssociation(sub.id)
        }
        semesterDao.delete(semester)
        val remaining = semesterDao.observeAll().first()
        if (remaining.isNotEmpty() && remaining.none { it.isCurrent }) {
            semesterDao.setSingleCurrent(remaining.first().id)
        }
    }
}

class SubjectRepository(
    private val subjectDao: SubjectDao
) {
    fun observeBySemester(semId: Long): Flow<List<SubjectEntity>> = subjectDao.observeBySemester(semId)
    suspend fun getById(id: Long): SubjectEntity? = subjectDao.getById(id)

    suspend fun saveSubject(subject: SubjectEntity): Long {
        return if (subject.id == 0L) subjectDao.insert(subject)
        else {
            subjectDao.update(subject)
            subject.id
        }
    }

    suspend fun deleteSubject(subject: SubjectEntity) {
        subjectDao.delete(subject)
    }
}

class ScheduleRepository(
    private val db: AppDatabase,
    private val classSessionDao: ClassSessionDao,
    private val classExceptionDao: ClassExceptionDao,
    private val subjectDao: SubjectDao,
    private val semesterDao: SemesterDao,
    private val occurrenceGenerator: OccurrenceGenerator,
    private val reminderScheduler: ReminderScheduler
) {
    fun observeOccurrencesForDate(semId: Long, date: LocalDate): Flow<List<Occurrence>> {
        return classSessionDao.observeBySemester(semId).flatMapLatest { sessions ->
            classExceptionDao.observeBetween(date.toString(), date.toString()).flatMapLatest { exceptions ->
                subjectDao.observeBySemester(semId).flatMapLatest { subjects ->
                    semesterDao.observeCurrent().flatMapLatest { sem ->
                        if (sem == null) flowOf(emptyList())
                        else {
                            val subMap = subjects.associateBy { it.id }
                            val occurrences = occurrenceGenerator.forDate(sem, subMap, sessions, exceptions, date)
                            flowOf(occurrences)
                        }
                    }
                }
            }
        }
    }

    suspend fun saveClassSession(session: ClassSessionEntity, remOffsetMin: Int?): Long = db.withTransaction {
        val id = if (session.id == 0L) classSessionDao.insert(session)
        else {
            classSessionDao.update(session)
            session.id
        }

        if (remOffsetMin != null) {
            db.reminderDao().deleteByOwner(ReminderOwner.CLASS, id)
            db.reminderDao().insert(
                ReminderEntity(ownerType = ReminderOwner.CLASS, ownerId = id, offsetMin = remOffsetMin, isEnabled = true)
            )
            reminderScheduler.rescheduleForOwner(ReminderOwner.CLASS, id)
        }

        id
    }

    suspend fun deleteClassSession(session: ClassSessionEntity) = db.withTransaction {
        reminderScheduler.cancelAlarmsForOwner(ReminderOwner.CLASS, session.id)
        classSessionDao.delete(session)
    }

    suspend fun addOrUpdateException(exception: ClassExceptionEntity) = db.withTransaction {
        classExceptionDao.upsert(exception)
        reminderScheduler.rescheduleForOwner(ReminderOwner.CLASS, exception.sessionId)
    }

    suspend fun deleteException(sessionId: Long, date: String) = db.withTransaction {
        classExceptionDao.deleteBySessionAndDate(sessionId, date)
        reminderScheduler.rescheduleForOwner(ReminderOwner.CLASS, sessionId)
    }
}

class TaskRepository(
    private val db: AppDatabase,
    private val taskDao: TaskDao,
    private val subtaskDao: SubtaskDao,
    private val coinDao: CoinDao,
    private val jarMonthDao: JarMonthDao,
    private val settingsDataStore: SettingsDataStore,
    private val reminderScheduler: ReminderScheduler,
    private val dateTimeProvider: DateTimeProvider
) {
    fun observeOpen(): Flow<List<TaskEntity>> = taskDao.observeOpen()
    fun observeDone(): Flow<List<TaskEntity>> = taskDao.observeDone()
    suspend fun getById(id: Long): TaskEntity? = taskDao.getById(id)

    suspend fun saveTask(task: TaskEntity, subtasks: List<SubtaskEntity>, reminderOffsets: List<Int>): Long = db.withTransaction {
        val nowMs = dateTimeProvider.nowMillis()
        val taskId = if (task.id == 0L) {
            taskDao.insert(task.copy(createdAt = nowMs, updatedAt = nowMs))
        } else {
            taskDao.update(task.copy(updatedAt = nowMs))
            task.id
        }

        subtaskDao.deleteByTask(taskId)
        subtaskDao.insertAll(subtasks.map { it.copy(taskId = taskId) })

        db.reminderDao().deleteByOwner(ReminderOwner.TASK, taskId)
        for (offset in reminderOffsets) {
            db.reminderDao().insert(
                ReminderEntity(ownerType = ReminderOwner.TASK, ownerId = taskId, offsetMin = offset, isEnabled = true)
            )
        }
        reminderScheduler.rescheduleForOwner(ReminderOwner.TASK, taskId)

        taskId
    }

    suspend fun markDone(taskId: Long) = db.withTransaction {
        val t = taskDao.getById(taskId) ?: return@withTransaction
        val now = dateTimeProvider.now()
        val nowMs = dateTimeProvider.nowMillis()
        taskDao.update(t.copy(status = TaskStatus.DONE, completedAt = nowMs, updatedAt = nowMs))

        if (coinDao.findBySource(CoinSource.TASK, taskId) == null) {
            val monthKey = YearMonth.from(now.toLocalDate()).toString()
            val tier = when (t.priority) {
                Priority.LOW -> CoinTier.BRONZE
                Priority.MEDIUM -> CoinTier.SILVER
                Priority.HIGH -> CoinTier.GOLD
            }
            val defaultGoal = settingsDataStore.jarDefaultGoal.first()
            jarMonthDao.ensure(monthKey, defaultGoal)

            val subject = t.subjectId?.let { db.subjectDao().getById(it) }
            val isOnTime = t.dueAt.isNullOrEmpty() || !now.isAfter(LocalDateTime.parse(t.dueAt))

            coinDao.insert(
                CoinEntity(
                    monthKey = monthKey,
                    earnedAt = now.toString(),
                    tier = tier,
                    sourceType = CoinSource.TASK,
                    sourceId = taskId,
                    label = t.title.take(120),
                    subjectId = t.subjectId,
                    subjectName = subject?.name,
                    subjectColor = subject?.color,
                    isOnTime = isOnTime,
                    seed = Random().nextInt(),
                    isSeen = false
                )
            )
        }
        reminderScheduler.cancelAlarmsForOwner(ReminderOwner.TASK, taskId)
    }

    suspend fun reopen(taskId: Long) = db.withTransaction {
        val t = taskDao.getById(taskId) ?: return@withTransaction
        taskDao.update(t.copy(status = TaskStatus.TODO, completedAt = null, updatedAt = dateTimeProvider.nowMillis()))
        coinDao.deleteBySource(CoinSource.TASK, taskId)
        reminderScheduler.rescheduleForOwner(ReminderOwner.TASK, taskId)
    }

    suspend fun deleteTask(taskId: Long) = db.withTransaction {
        val t = taskDao.getById(taskId) ?: return@withTransaction
        coinDao.detachSource(CoinSource.TASK, taskId)
        reminderScheduler.cancelAlarmsForOwner(ReminderOwner.TASK, taskId)
        taskDao.delete(t)
    }
}

class ExamRepository(
    private val db: AppDatabase,
    private val examDao: ExamDao,
    private val reminderScheduler: ReminderScheduler
) {
    fun observeUpcoming(nowStr: String): Flow<List<ExamEntity>> = examDao.observeUpcoming(nowStr)
    fun observeBySemester(semId: Long): Flow<List<ExamEntity>> = examDao.observeBySemester(semId)
    suspend fun getById(id: Long): ExamEntity? = examDao.getById(id)

    suspend fun saveExam(exam: ExamEntity, reminderOffsets: List<Int>): Long = db.withTransaction {
        val id = if (exam.id == 0L) examDao.insert(exam)
        else {
            examDao.update(exam)
            exam.id
        }

        db.reminderDao().deleteByOwner(ReminderOwner.EXAM, id)
        for (offset in reminderOffsets) {
            db.reminderDao().insert(
                ReminderEntity(ownerType = ReminderOwner.EXAM, ownerId = id, offsetMin = offset, isEnabled = true)
            )
        }
        reminderScheduler.rescheduleForOwner(ReminderOwner.EXAM, id)
        id
    }

    suspend fun deleteExam(exam: ExamEntity) = db.withTransaction {
        reminderScheduler.cancelAlarmsForOwner(ReminderOwner.EXAM, exam.id)
        examDao.delete(exam)
    }
}

class TimeBlockRepository(
    private val timeBlockDao: TimeBlockDao
) {
    fun observeBetween(from: String, to: String): Flow<List<TimeBlockEntity>> = timeBlockDao.observeBetween(from, to)
    suspend fun getById(id: Long): TimeBlockEntity? = timeBlockDao.getById(id)

    suspend fun saveTimeBlock(block: TimeBlockEntity): Long {
        return if (block.id == 0L) timeBlockDao.insert(block)
        else {
            timeBlockDao.update(block)
            block.id
        }
    }

    suspend fun deleteTimeBlock(block: TimeBlockEntity) {
        timeBlockDao.delete(block)
    }
}

class FocusRepository(
    private val db: AppDatabase,
    private val focusSessionDao: FocusSessionDao,
    private val coinDao: CoinDao,
    private val jarMonthDao: JarMonthDao,
    private val settingsDataStore: SettingsDataStore,
    private val dateTimeProvider: DateTimeProvider
) {
    fun observeSessions(fromMs: Long, toMs: Long): Flow<List<FocusSessionEntity>> =
        focusSessionDao.observeBetween(fromMs, toMs)

    fun totalBySubject(fromMs: Long, toMs: Long): Flow<List<SubjectTime>> =
        focusSessionDao.totalBySubject(fromMs, toMs)

    suspend fun saveFocusSession(session: FocusSessionEntity): Long = db.withTransaction {
        if (session.actualSec < 60) return@withTransaction 0L
        val id = focusSessionDao.insert(session)

        if (session.isCompleted && session.actualSec >= 1500) {
            val now = dateTimeProvider.now()
            val monthKey = YearMonth.from(now.toLocalDate()).toString()
            val defaultGoal = settingsDataStore.jarDefaultGoal.first()
            jarMonthDao.ensure(monthKey, defaultGoal)

            val subject = session.subjectId?.let { db.subjectDao().getById(it) }

            coinDao.insert(
                CoinEntity(
                    monthKey = monthKey,
                    earnedAt = now.toString(),
                    tier = CoinTier.FOCUS,
                    sourceType = CoinSource.FOCUS,
                    sourceId = id,
                    label = "Phiên tập trung",
                    subjectId = session.subjectId,
                    subjectName = subject?.name,
                    subjectColor = subject?.color,
                    isOnTime = true,
                    seed = Random().nextInt(),
                    isSeen = false
                )
            )
        }

        id
    }
}

class CoinJarRepository(
    private val coinDao: CoinDao,
    private val jarMonthDao: JarMonthDao
) {
    fun observeCoinsByMonth(monthKey: String): Flow<List<CoinEntity>> = coinDao.observeByMonth(monthKey)
    fun observeJarMonths(): Flow<List<JarMonthEntity>> = jarMonthDao.observeAll()

    suspend fun getJarMonth(monthKey: String): JarMonthEntity? = jarMonthDao.getByMonth(monthKey)

    suspend fun updateGoal(monthKey: String, goal: Int) {
        val existing = jarMonthDao.getByMonth(monthKey)
        if (existing != null) {
            jarMonthDao.insertOrUpdate(existing.copy(goal = goal.coerceIn(1, 200)))
        } else {
            jarMonthDao.insertOrUpdate(JarMonthEntity(monthKey, goal.coerceIn(1, 200)))
        }
    }

    suspend fun markSeen(monthKey: String) {
        coinDao.markSeen(monthKey)
    }
}
