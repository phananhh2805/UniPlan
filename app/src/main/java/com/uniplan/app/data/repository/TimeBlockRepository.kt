package com.uniplan.app.data.repository

import androidx.room.withTransaction
import com.uniplan.app.data.local.db.AppDatabase
import com.uniplan.app.data.local.db.TimeBlockEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TimeBlockRepository @Inject constructor(
    private val db: AppDatabase
) {
    fun observeBlocksBetween(from: String, to: String): Flow<List<TimeBlockEntity>> =
        db.timeBlockDao().observeBetween(from, to)

    suspend fun getBlocksBetween(from: String, to: String): List<TimeBlockEntity> =
        db.timeBlockDao().getBetween(from, to)

    suspend fun getBlockById(id: Long): TimeBlockEntity? = db.timeBlockDao().getById(id)

    suspend fun saveBlock(tb: TimeBlockEntity): Long = db.withTransaction {
        if (tb.id == 0L) {
            db.timeBlockDao().insert(tb)
        } else {
            db.timeBlockDao().update(tb)
            tb.id
        }
    }

    suspend fun deleteBlock(tb: TimeBlockEntity) = db.withTransaction {
        db.timeBlockDao().delete(tb)
    }
}
