package com.uniplan.app.data.repository

import androidx.room.withTransaction
import com.uniplan.app.data.local.db.AppDatabase
import com.uniplan.app.data.local.db.ExamEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ExamRepository @Inject constructor(
    private val db: AppDatabase
) {
    val allExamsFlow: Flow<List<ExamEntity>> = db.examDao().observeAll()

    suspend fun getExamById(id: Long): ExamEntity? = db.examDao().getById(id)

    fun observeExamsBetween(from: String, to: String): Flow<List<ExamEntity>> =
        db.examDao().observeBetween(from, to)

    suspend fun getExamsBetween(from: String, to: String): List<ExamEntity> =
        db.examDao().getBetween(from, to)

    suspend fun saveExam(exam: ExamEntity): Long = db.withTransaction {
        if (exam.id == 0L) {
            db.examDao().insert(exam)
        } else {
            db.examDao().update(exam)
            exam.id
        }
    }

    suspend fun deleteExam(exam: ExamEntity) = db.withTransaction {
        db.examDao().delete(exam)
    }
}
