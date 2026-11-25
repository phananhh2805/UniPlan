package com.uniplan.app.data.repository

import androidx.room.withTransaction
import com.uniplan.app.data.local.db.AppDatabase
import com.uniplan.app.data.local.db.ClassExceptionEntity
import com.uniplan.app.data.local.db.ClassSessionEntity
import com.uniplan.app.data.local.db.SemesterEntity
import com.uniplan.app.domain.model.Occurrence
import com.uniplan.app.domain.usecase.OccurrenceGenerator
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ScheduleRepository @Inject constructor(
    private val db: AppDatabase,
    private val occurrenceGenerator: OccurrenceGenerator
) {
    fun observeSessions(semesterId: Long): Flow<List<ClassSessionEntity>> =
        db.classSessionDao().observeBySemester(semesterId)

    suspend fun getSessionsBySemester(semesterId: Long): List<ClassSessionEntity> =
        db.classSessionDao().getBySemester(semesterId)

    suspend fun getSessionById(id: Long): ClassSessionEntity? =
        db.classSessionDao().getById(id)

    suspend fun saveSession(session: ClassSessionEntity): Long = db.withTransaction {
        if (session.id == 0L) {
            db.classSessionDao().insert(session)
        } else {
            db.classSessionDao().update(session)
            session.id
        }
    }

    suspend fun deleteSession(session: ClassSessionEntity) = db.withTransaction {
        db.classSessionDao().delete(session)
    }

    suspend fun saveException(exception: ClassExceptionEntity): Long = db.withTransaction {
        db.classExceptionDao().upsert(exception)
    }

    suspend fun deleteException(exception: ClassExceptionEntity) = db.withTransaction {
        db.classExceptionDao().delete(exception)
    }

    fun observeOccurrencesForDate(sem: SemesterEntity, date: LocalDate): Flow<List<Occurrence>> {
        val sessionsFlow = db.classSessionDao().observeBySemester(sem.id)
        val exceptionsFlow = db.classExceptionDao().observeBetween(date.toString(), date.toString())
        val subjectsFlow = db.subjectDao().observeBySemester(sem.id)

        return combine(sessionsFlow, exceptionsFlow, subjectsFlow) { sessions, exceptions, subjects ->
            val subjectsMap = subjects.associateBy { it.id }
            occurrenceGenerator.forDate(sem, sessions, exceptions, subjectsMap, date)
        }
    }

    fun observeOccurrencesBetween(sem: SemesterEntity, fromDate: LocalDate, toDate: LocalDate): Flow<Map<LocalDate, List<Occurrence>>> {
        val sessionsFlow = db.classSessionDao().observeBySemester(sem.id)
        val exceptionsFlow = db.classExceptionDao().observeBetween(fromDate.toString(), toDate.toString())
        val subjectsFlow = db.subjectDao().observeBySemester(sem.id)

        return combine(sessionsFlow, exceptionsFlow, subjectsFlow) { sessions, exceptions, subjects ->
            val subjectsMap = subjects.associateBy { it.id }
            val map = mutableMapOf<LocalDate, List<Occurrence>>()
            var curr = fromDate
            while (!curr.isAfter(toDate)) {
                val occs = occurrenceGenerator.forDate(sem, sessions, exceptions, subjectsMap, curr)
                if (occs.isNotEmpty()) {
                    map[curr] = occs
                }
                curr = curr.plusDays(1)
            }
            map
        }
    }
}
