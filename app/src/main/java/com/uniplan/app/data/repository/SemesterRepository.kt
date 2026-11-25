package com.uniplan.app.data.repository

import androidx.room.withTransaction
import com.uniplan.app.data.local.db.AppDatabase
import com.uniplan.app.data.local.db.SemesterEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SemesterRepository @Inject constructor(
    private val db: AppDatabase
) {
    val semestersFlow: Flow<List<SemesterEntity>> = db.semesterDao().observeAll()
    val currentSemesterFlow: Flow<SemesterEntity?> = db.semesterDao().observeCurrent()

    suspend fun getCurrentSemester(): SemesterEntity? = db.semesterDao().getCurrent()

    suspend fun addSemester(name: String, startDate: String, endDate: String, isCurrent: Boolean): Long {
        return db.withTransaction {
            val semester = SemesterEntity(
                name = name.trim(),
                startDate = startDate,
                endDate = endDate,
                isCurrent = isCurrent
            )
            val id = db.semesterDao().insert(semester)
            if (isCurrent) {
                db.semesterDao().setCurrent(id)
            } else {
                // If it's the first semester, make it current
                val count = db.semesterDao().getCurrent()
                if (count == null) {
                    db.semesterDao().setCurrent(id)
                }
            }
            id
        }
    }

    suspend fun updateSemester(s: SemesterEntity) {
        db.withTransaction {
            db.semesterDao().update(s)
            if (s.isCurrent) {
                db.semesterDao().setCurrent(s.id)
            }
        }
    }

    suspend fun setCurrent(id: Long) {
        db.withTransaction {
            db.semesterDao().setCurrent(id)
        }
    }

    suspend fun deleteSemester(s: SemesterEntity) {
        db.withTransaction {
            db.semesterDao().delete(s)
            if (s.isCurrent) {
                val current = db.semesterDao().getCurrent()
                if (current == null) {
                    // Get first remaining semester and make it current
                    val first = db.semesterDao().getCurrent()
                }
            }
        }
    }
}
