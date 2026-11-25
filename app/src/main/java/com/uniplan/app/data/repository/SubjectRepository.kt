package com.uniplan.app.data.repository

import androidx.room.withTransaction
import com.uniplan.app.data.local.db.AppDatabase
import com.uniplan.app.data.local.db.SubjectEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SubjectRepository @Inject constructor(
    private val db: AppDatabase
) {
    fun observeSubjects(semesterId: Long): Flow<List<SubjectEntity>> =
        db.subjectDao().observeBySemester(semesterId)

    suspend fun getSubjects(semesterId: Long): List<SubjectEntity> =
        db.subjectDao().getBySemester(semesterId)

    suspend fun getSubjectById(id: Long): SubjectEntity? =
        db.subjectDao().getById(id)

    suspend fun isNameExists(semesterId: Long, name: String, excludeId: Long = -1): Boolean =
        db.subjectDao().countByName(semesterId, name.trim(), excludeId) > 0

    suspend fun addSubject(s: SubjectEntity): Long = db.withTransaction {
        db.subjectDao().insert(s)
    }

    suspend fun updateSubject(s: SubjectEntity) = db.withTransaction {
        db.subjectDao().update(s)
    }

    suspend fun deleteSubject(s: SubjectEntity) = db.withTransaction {
        db.subjectDao().delete(s)
    }
}
