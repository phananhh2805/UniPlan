package com.uniplan.app.data.local.db

import androidx.room.*
import com.uniplan.app.domain.model.AlarmState
import com.uniplan.app.domain.model.CoinSource
import com.uniplan.app.domain.model.ReminderOwner
import kotlinx.coroutines.flow.Flow

@Dao
interface SemesterDao {
    @Query("SELECT * FROM semester ORDER BY start_date DESC")
    fun observeAll(): Flow<List<SemesterEntity>>

    @Query("SELECT * FROM semester WHERE is_current = 1 LIMIT 1")
    fun observeCurrent(): Flow<SemesterEntity?>

    @Query("SELECT * FROM semester WHERE is_current = 1 LIMIT 1")
    suspend fun getCurrent(): SemesterEntity?

    @Query("SELECT * FROM semester WHERE id = :id")
    suspend fun getById(id: Long): SemesterEntity?

    @Insert
    suspend fun insert(s: SemesterEntity): Long

    @Update
    suspend fun update(s: SemesterEntity)

    @Delete
    suspend fun delete(s: SemesterEntity)

    @Query("UPDATE semester SET is_current = (id = :id)")
    suspend fun setCurrent(id: Long)

    @Query("UPDATE semester SET is_current = 0")
    suspend fun clearCurrent()
}

@Dao
interface SubjectDao {
    @Query("SELECT * FROM subject WHERE semester_id = :semId ORDER BY name")
    fun observeBySemester(semId: Long): Flow<List<SubjectEntity>>

    @Query("SELECT * FROM subject WHERE semester_id = :semId ORDER BY name")
    suspend fun getBySemester(semId: Long): List<SubjectEntity>

    @Query("SELECT * FROM subject WHERE id = :id")
    suspend fun getById(id: Long): SubjectEntity?

    @Query("SELECT COUNT(*) FROM subject WHERE semester_id = :semId AND LOWER(name) = LOWER(:name) AND id != :excludeId")
    suspend fun countByName(semId: Long, name: String, excludeId: Long = -1): Int

    @Insert
    suspend fun insert(s: SubjectEntity): Long

    @Update
    suspend fun update(s: SubjectEntity)

    @Delete
    suspend fun delete(s: SubjectEntity)
}

@Dao
interface ClassSessionDao {
    @Query("SELECT cs.* FROM class_session cs JOIN subject s ON s.id = cs.subject_id WHERE s.semester_id = :semId AND cs.is_active = 1")
    fun observeBySemester(semId: Long): Flow<List<ClassSessionEntity>>

    @Query("SELECT cs.* FROM class_session cs JOIN subject s ON s.id = cs.subject_id WHERE s.semester_id = :semId AND cs.is_active = 1")
    suspend fun getBySemester(semId: Long): List<ClassSessionEntity>

    @Query("SELECT * FROM class_session WHERE subject_id = :subjectId")
    suspend fun getBySubject(subjectId: Long): List<ClassSessionEntity>

    @Query("SELECT * FROM class_session WHERE id = :id")
    suspend fun getById(id: Long): ClassSessionEntity?

    @Insert
    suspend fun insert(s: ClassSessionEntity): Long

    @Update
    suspend fun update(s: ClassSessionEntity)

    @Delete
    suspend fun delete(s: ClassSessionEntity)
}

@Dao
interface ClassExceptionDao {
    @Query("SELECT * FROM class_exception WHERE date BETWEEN :from AND :to")
    fun observeBetween(from: String, to: String): Flow<List<ClassExceptionEntity>>

    @Query("SELECT * FROM class_exception WHERE date BETWEEN :from AND :to")
    suspend fun getBetween(from: String, to: String): List<ClassExceptionEntity>

    @Query("SELECT * FROM class_exception WHERE session_id = :sessionId AND date = :date LIMIT 1")
    suspend fun getBySessionAndDate(sessionId: Long, date: String): ClassExceptionEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(e: ClassExceptionEntity): Long

    @Delete
    suspend fun delete(e: ClassExceptionEntity)
}

@Dao
interface TaskDao {
    @Query("SELECT * FROM task WHERE status != 'DONE' ORDER BY (due_at IS NULL), due_at")
    fun observeOpen(): Flow<List<TaskEntity>>

    @Query("SELECT * FROM task WHERE status = 'DONE' ORDER BY completed_at DESC")
    fun observeDone(): Flow<List<TaskEntity>>

    @Query("SELECT * FROM task ORDER BY created_at DESC")
    fun observeAll(): Flow<List<TaskEntity>>

    @Query("SELECT * FROM task WHERE id = :id")
    suspend fun getById(id: Long): TaskEntity?

    @Query("SELECT COUNT(*) FROM task WHERE completed_at >= :fromMs AND completed_at < :toMs")
    fun countCompleted(fromMs: Long, toMs: Long): Flow<Int>

    @Query("SELECT * FROM task WHERE completed_at >= :fromMs AND completed_at < :toMs")
    suspend fun getCompletedBetween(fromMs: Long, toMs: Long): List<TaskEntity>

    @Insert
    suspend fun insert(t: TaskEntity): Long

    @Update
    suspend fun update(t: TaskEntity)

    @Delete
    suspend fun delete(t: TaskEntity)
}

@Dao
interface SubtaskDao {
    @Query("SELECT * FROM subtask WHERE task_id = :taskId ORDER BY position")
    fun observeByTask(taskId: Long): Flow<List<SubtaskEntity>>

    @Query("SELECT * FROM subtask WHERE task_id = :taskId ORDER BY position")
    suspend fun getByTask(taskId: Long): List<SubtaskEntity>

    @Insert
    suspend fun insert(st: SubtaskEntity): Long

    @Insert
    suspend fun insertAll(list: List<SubtaskEntity>)

    @Update
    suspend fun update(st: SubtaskEntity)

    @Delete
    suspend fun delete(st: SubtaskEntity)

    @Query("DELETE FROM subtask WHERE task_id = :taskId")
    suspend fun deleteByTask(taskId: Long)
}

@Dao
interface ExamDao {
    @Query("SELECT * FROM exam ORDER BY start_at")
    fun observeAll(): Flow<List<ExamEntity>>

    @Query("SELECT * FROM exam WHERE id = :id")
    suspend fun getById(id: Long): ExamEntity?

    @Query("SELECT * FROM exam WHERE start_at BETWEEN :from AND :to ORDER BY start_at")
    fun observeBetween(from: String, to: String): Flow<List<ExamEntity>>

    @Query("SELECT * FROM exam WHERE start_at BETWEEN :from AND :to ORDER BY start_at")
    suspend fun getBetween(from: String, to: String): List<ExamEntity>

    @Insert
    suspend fun insert(e: ExamEntity): Long

    @Update
    suspend fun update(e: ExamEntity)

    @Delete
    suspend fun delete(e: ExamEntity)
}

@Dao
interface TimeBlockDao {
    @Query("SELECT * FROM time_block WHERE date BETWEEN :from AND :to ORDER BY start_time")
    fun observeBetween(from: String, to: String): Flow<List<TimeBlockEntity>>

    @Query("SELECT * FROM time_block WHERE date BETWEEN :from AND :to ORDER BY start_time")
    suspend fun getBetween(from: String, to: String): List<TimeBlockEntity>

    @Query("SELECT * FROM time_block WHERE id = :id")
    suspend fun getById(id: Long): TimeBlockEntity?

    @Insert
    suspend fun insert(tb: TimeBlockEntity): Long

    @Update
    suspend fun update(tb: TimeBlockEntity)

    @Delete
    suspend fun delete(tb: TimeBlockEntity)
}

@Dao
interface ReminderDao {
    @Query("SELECT * FROM reminder WHERE owner_type = :ownerType AND owner_id = :ownerId")
    suspend fun getByOwner(ownerType: ReminderOwner, ownerId: Long): List<ReminderEntity>

    @Query("SELECT * FROM reminder WHERE is_enabled = 1")
    suspend fun getAllEnabled(): List<ReminderEntity>

    @Query("SELECT * FROM reminder WHERE owner_type = :ownerType AND is_enabled = 1")
    suspend fun getEnabledByOwnerType(ownerType: ReminderOwner): List<ReminderEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(r: ReminderEntity): Long

    @Query("DELETE FROM reminder WHERE owner_type = :ownerType AND owner_id = :ownerId")
    suspend fun deleteByOwner(ownerType: ReminderOwner, ownerId: Long)

    @Delete
    suspend fun delete(r: ReminderEntity)
}

@Dao
interface ScheduledAlarmDao {
    @Query("SELECT * FROM scheduled_alarm WHERE state = 'SCHEDULED' AND reminder_id IN (:reminderIds)")
    suspend fun scheduledFor(reminderIds: List<Long>): List<ScheduledAlarmEntity>

    @Query("SELECT * FROM scheduled_alarm WHERE id = :id")
    suspend fun getById(id: Long): ScheduledAlarmEntity?

    @Insert
    suspend fun insert(alarm: ScheduledAlarmEntity): Long

    @Update
    suspend fun update(alarm: ScheduledAlarmEntity)

    @Query("UPDATE scheduled_alarm SET state = :state WHERE id = :id")
    suspend fun updateState(id: Long, state: AlarmState)

    @Query("DELETE FROM scheduled_alarm WHERE state != 'SCHEDULED' AND fire_at < :beforeMs")
    suspend fun purgeOld(beforeMs: Long)

    @Query("SELECT * FROM scheduled_alarm WHERE state = 'SCHEDULED'")
    suspend fun getAllScheduled(): List<ScheduledAlarmEntity>
}

@Dao
interface FocusSessionDao {
    @Query("SELECT subject_id AS subjectId, SUM(actual_sec) AS totalSec FROM focus_session WHERE started_at >= :fromMs AND started_at < :toMs GROUP BY subject_id")
    fun totalBySubject(fromMs: Long, toMs: Long): Flow<List<SubjectTime>>

    @Query("SELECT SUM(actual_sec) FROM focus_session WHERE started_at >= :fromMs AND started_at < :toMs")
    fun totalFocusTime(fromMs: Long, toMs: Long): Flow<Int?>

    @Insert
    suspend fun insert(fs: FocusSessionEntity): Long
}

@Dao
interface CoinDao {
    @Query("SELECT * FROM coin WHERE month_key = :monthKey ORDER BY earned_at ASC, id ASC")
    fun observeByMonth(monthKey: String): Flow<List<CoinEntity>>

    @Query("SELECT * FROM coin WHERE month_key = :monthKey ORDER BY earned_at ASC, id ASC")
    suspend fun getByMonth(monthKey: String): List<CoinEntity>

    @Query("SELECT * FROM coin WHERE source_type = :sourceType AND source_id = :sourceId LIMIT 1")
    suspend fun findBySource(sourceType: CoinSource, sourceId: Long): CoinEntity?

    @Query("DELETE FROM coin WHERE source_type = :sourceType AND source_id = :sourceId")
    suspend fun deleteBySource(sourceType: CoinSource, sourceId: Long)

    @Query("UPDATE coin SET source_id = NULL WHERE source_type = :sourceType AND source_id = :sourceId")
    suspend fun detachSource(sourceType: CoinSource, sourceId: Long)

    @Query("UPDATE coin SET is_seen = 1 WHERE month_key = :monthKey")
    suspend fun markAllSeen(monthKey: String)

    @Insert
    suspend fun insert(c: CoinEntity): Long
}

@Dao
interface JarMonthDao {
    @Query("SELECT * FROM jar_month WHERE month_key = :monthKey")
    suspend fun getByMonth(monthKey: String): JarMonthEntity?

    @Query("SELECT * FROM jar_month ORDER BY month_key DESC")
    fun observeAll(): Flow<List<JarMonthEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(jm: JarMonthEntity)
}
