package com.uniplan.app.data.local.db.dao

import androidx.room.*
import com.uniplan.app.data.local.db.*
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
    suspend fun getCurrentSync(): SemesterEntity?

    @Query("SELECT * FROM semester WHERE id = :id")
    suspend fun getById(id: Long): SemesterEntity?

    @Query("SELECT COUNT(*) FROM semester WHERE LOWER(name) = LOWER(:name) AND id != :excludeId")
    suspend fun countByName(name: String, excludeId: Long = 0): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(s: SemesterEntity): Long

    @Update
    suspend fun update(s: SemesterEntity): Int

    @Delete
    suspend fun delete(s: SemesterEntity): Int

    @Query("UPDATE semester SET is_current = 0")
    suspend fun clearCurrent(): Int

    @Query("UPDATE semester SET is_current = 1 WHERE id = :id")
    suspend fun setCurrent(id: Long): Int

    @Transaction
    suspend fun setSingleCurrent(id: Long) {
        clearCurrent()
        setCurrent(id)
    }
}

@Dao
interface SubjectDao {
    @Query("SELECT * FROM subject WHERE semester_id = :semId ORDER BY name ASC")
    fun observeBySemester(semId: Long): Flow<List<SubjectEntity>>

    @Query("SELECT * FROM subject WHERE semester_id = :semId ORDER BY name ASC")
    suspend fun getBySemester(semId: Long): List<SubjectEntity>

    @Query("SELECT * FROM subject WHERE id = :id")
    suspend fun getById(id: Long): SubjectEntity?

    @Query("SELECT COUNT(*) FROM subject WHERE semester_id = :semId AND LOWER(name) = LOWER(:name) AND id != :excludeId")
    suspend fun countByName(semId: Long, name: String, excludeId: Long = 0): Int

    @Query("SELECT COUNT(*) FROM subject WHERE semester_id = :semId AND LOWER(code) = LOWER(:code) AND id != :excludeId")
    suspend fun countByCode(semId: Long, code: String, excludeId: Long = 0): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(s: SubjectEntity): Long

    @Update
    suspend fun update(s: SubjectEntity): Int

    @Delete
    suspend fun delete(s: SubjectEntity): Int
}

@Dao
interface ClassSessionDao {
    @Query("SELECT class_session.* FROM class_session JOIN subject ON subject.id = class_session.subject_id WHERE subject.semester_id = :semId AND class_session.is_active = 1")
    fun observeBySemester(semId: Long): Flow<List<ClassSessionEntity>>

    @Query("SELECT class_session.* FROM class_session JOIN subject ON subject.id = class_session.subject_id WHERE subject.semester_id = :semId")
    suspend fun getBySemester(semId: Long): List<ClassSessionEntity>

    @Query("SELECT * FROM class_session WHERE subject_id = :subjectId")
    suspend fun getBySubject(subjectId: Long): List<ClassSessionEntity>

    @Query("SELECT * FROM class_session WHERE id = :id")
    suspend fun getById(id: Long): ClassSessionEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(cs: ClassSessionEntity): Long

    @Update
    suspend fun update(cs: ClassSessionEntity): Int

    @Delete
    suspend fun delete(cs: ClassSessionEntity): Int
}

@Dao
interface ClassExceptionDao {
    @Query("SELECT * FROM class_exception WHERE date BETWEEN :from AND :to")
    fun observeBetween(from: String, to: String): Flow<List<ClassExceptionEntity>>

    @Query("SELECT * FROM class_exception WHERE date = :date")
    suspend fun getByDate(date: String): List<ClassExceptionEntity>

    @Query("SELECT * FROM class_exception WHERE session_id = :sessionId AND date = :date LIMIT 1")
    suspend fun getBySessionAndDate(sessionId: Long, date: String): ClassExceptionEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(e: ClassExceptionEntity): Long

    @Delete
    suspend fun delete(e: ClassExceptionEntity): Int

    @Query("DELETE FROM class_exception WHERE session_id = :sessionId AND date = :date")
    suspend fun deleteBySessionAndDate(sessionId: Long, date: String): Int
}

@Dao
interface TaskDao {
    @Query("SELECT * FROM task WHERE status != 'DONE' ORDER BY (due_at IS NULL), due_at ASC")
    fun observeOpen(): Flow<List<TaskEntity>>

    @Query("SELECT * FROM task WHERE status = 'DONE' ORDER BY completed_at DESC")
    fun observeDone(): Flow<List<TaskEntity>>

    @Query("SELECT * FROM task ORDER BY created_at DESC")
    fun observeAll(): Flow<List<TaskEntity>>

    @Query("SELECT * FROM task WHERE id = :id")
    suspend fun getById(id: Long): TaskEntity?

    @Query("SELECT COUNT(*) FROM task WHERE completed_at >= :fromMs AND completed_at < :toMs")
    fun countCompleted(fromMs: Long, toMs: Long): Flow<Int>

    @Query("SELECT COUNT(*) FROM task WHERE completed_at >= :fromMs AND completed_at < :toMs")
    suspend fun countCompletedSync(fromMs: Long, toMs: Long): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(t: TaskEntity): Long

    @Update
    suspend fun update(t: TaskEntity): Int

    @Delete
    suspend fun delete(t: TaskEntity): Int

    @Query("UPDATE task SET subject_id = NULL WHERE subject_id = :subjectId")
    suspend fun clearSubjectAssociation(subjectId: Long): Int
}

@Dao
interface SubtaskDao {
    @Query("SELECT * FROM subtask WHERE task_id = :taskId ORDER BY position ASC")
    fun observeByTask(taskId: Long): Flow<List<SubtaskEntity>>

    @Query("SELECT * FROM subtask WHERE task_id = :taskId ORDER BY position ASC")
    suspend fun getByTask(taskId: Long): List<SubtaskEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(st: SubtaskEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(subtasks: List<SubtaskEntity>)

    @Update
    suspend fun update(st: SubtaskEntity): Int

    @Delete
    suspend fun delete(st: SubtaskEntity): Int

    @Query("DELETE FROM subtask WHERE task_id = :taskId")
    suspend fun deleteByTask(taskId: Long): Int
}

@Dao
interface ExamDao {
    @Query("SELECT * FROM exam WHERE start_at >= :nowStr ORDER BY start_at ASC")
    fun observeUpcoming(nowStr: String): Flow<List<ExamEntity>>

    @Query("SELECT exam.* FROM exam JOIN subject ON subject.id = exam.subject_id WHERE subject.semester_id = :semId ORDER BY exam.start_at ASC")
    fun observeBySemester(semId: Long): Flow<List<ExamEntity>>

    @Query("SELECT exam.* FROM exam JOIN subject ON subject.id = exam.subject_id WHERE subject.semester_id = :semId ORDER BY exam.start_at ASC")
    suspend fun getBySemester(semId: Long): List<ExamEntity>

    @Query("SELECT * FROM exam WHERE start_at LIKE :datePrefix || '%'")
    suspend fun getByDate(datePrefix: String): List<ExamEntity>

    @Query("SELECT * FROM exam WHERE id = :id")
    suspend fun getById(id: Long): ExamEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(e: ExamEntity): Long

    @Update
    suspend fun update(e: ExamEntity): Int

    @Delete
    suspend fun delete(e: ExamEntity): Int
}

@Dao
interface TimeBlockDao {
    @Query("SELECT * FROM time_block WHERE date BETWEEN :from AND :to ORDER BY start_time ASC")
    fun observeBetween(from: String, to: String): Flow<List<TimeBlockEntity>>

    @Query("SELECT * FROM time_block WHERE date = :date ORDER BY start_time ASC")
    suspend fun getByDate(date: String): List<TimeBlockEntity>

    @Query("SELECT * FROM time_block WHERE id = :id")
    suspend fun getById(id: Long): TimeBlockEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(tb: TimeBlockEntity): Long

    @Update
    suspend fun update(tb: TimeBlockEntity): Int

    @Delete
    suspend fun delete(tb: TimeBlockEntity): Int
}

@Dao
interface ReminderDao {
    @Query("SELECT * FROM reminder WHERE owner_type = :ownerType AND owner_id = :ownerId")
    fun observeByOwner(ownerType: ReminderOwner, ownerId: Long): Flow<List<ReminderEntity>>

    @Query("SELECT * FROM reminder WHERE owner_type = :ownerType AND owner_id = :ownerId")
    suspend fun getByOwner(ownerType: ReminderOwner, ownerId: Long): List<ReminderEntity>

    @Query("SELECT * FROM reminder WHERE is_enabled = 1")
    suspend fun getAllEnabled(): List<ReminderEntity>

    @Query("SELECT * FROM reminder WHERE id = :id")
    suspend fun getById(id: Long): ReminderEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(r: ReminderEntity): Long

    @Update
    suspend fun update(r: ReminderEntity): Int

    @Delete
    suspend fun delete(r: ReminderEntity): Int

    @Query("DELETE FROM reminder WHERE owner_type = :ownerType AND owner_id = :ownerId")
    suspend fun deleteByOwner(ownerType: ReminderOwner, ownerId: Long): Int
}

@Dao
interface ScheduledAlarmDao {
    @Query("SELECT * FROM scheduled_alarm WHERE state = 'SCHEDULED' AND reminder_id IN (:reminderIds)")
    suspend fun scheduledFor(reminderIds: List<Long>): List<ScheduledAlarmEntity>

    @Query("SELECT * FROM scheduled_alarm WHERE state = 'SCHEDULED'")
    suspend fun getScheduledAll(): List<ScheduledAlarmEntity>

    @Query("SELECT * FROM scheduled_alarm WHERE id = :id")
    suspend fun getById(id: Long): ScheduledAlarmEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(sa: ScheduledAlarmEntity): Long

    @Update
    suspend fun update(sa: ScheduledAlarmEntity): Int

    @Delete
    suspend fun delete(sa: ScheduledAlarmEntity): Int

    @Query("DELETE FROM scheduled_alarm WHERE reminder_id = :reminderId")
    suspend fun deleteByReminder(reminderId: Long): Int

    @Query("DELETE FROM scheduled_alarm WHERE state != 'SCHEDULED' AND fire_at < :beforeMs")
    suspend fun purgeOld(beforeMs: Long): Int

    @Query("DELETE FROM scheduled_alarm")
    suspend fun deleteAll(): Int
}

@Dao
interface FocusSessionDao {
    @Query("SELECT subject_id AS subjectId, SUM(actual_sec) AS totalSec FROM focus_session WHERE started_at >= :fromMs AND started_at < :toMs GROUP BY subject_id")
    fun totalBySubject(fromMs: Long, toMs: Long): Flow<List<SubjectTime>>

    @Query("SELECT * FROM focus_session WHERE started_at >= :fromMs AND started_at < :toMs ORDER BY started_at DESC")
    fun observeBetween(fromMs: Long, toMs: Long): Flow<List<FocusSessionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
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

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(c: CoinEntity): Long

    @Query("DELETE FROM coin WHERE source_type = :sourceType AND source_id = :sourceId")
    suspend fun deleteBySource(sourceType: CoinSource, sourceId: Long): Int

    @Query("UPDATE coin SET source_id = NULL WHERE source_type = :sourceType AND source_id = :sourceId")
    suspend fun detachSource(sourceType: CoinSource, sourceId: Long): Int

    @Query("UPDATE coin SET is_seen = 1 WHERE month_key = :monthKey")
    suspend fun markSeen(monthKey: String): Int
}

@Dao
interface JarMonthDao {
    @Query("SELECT * FROM jar_month ORDER BY month_key DESC")
    fun observeAll(): Flow<List<JarMonthEntity>>

    @Query("SELECT * FROM jar_month WHERE month_key = :monthKey LIMIT 1")
    suspend fun getByMonth(monthKey: String): JarMonthEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(jm: JarMonthEntity)

    @Transaction
    suspend fun ensure(monthKey: String, defaultGoal: Int) {
        if (getByMonth(monthKey) == null) {
            insertOrUpdate(JarMonthEntity(monthKey, defaultGoal))
        }
    }
}
