package com.uniplan.app.data.repository

import androidx.room.withTransaction
import com.uniplan.app.data.local.db.AppDatabase
import com.uniplan.app.data.local.db.CoinEntity
import com.uniplan.app.data.local.db.JarMonthEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CoinJarRepository @Inject constructor(
    private val db: AppDatabase
) {
    fun observeCoinsByMonth(monthKey: String): Flow<List<CoinEntity>> =
        db.coinDao().observeByMonth(monthKey)

    suspend fun getCoinsByMonth(monthKey: String): List<CoinEntity> =
        db.coinDao().getByMonth(monthKey)

    fun observeJarMonths(): Flow<List<JarMonthEntity>> =
        db.jarMonthDao().observeAll()

    suspend fun getJarMonth(monthKey: String, defaultGoal: Int = 30): JarMonthEntity = db.withTransaction {
        val jm = db.jarMonthDao().getByMonth(monthKey)
        if (jm != null) jm
        else {
            val newJm = JarMonthEntity(monthKey, defaultGoal)
            db.jarMonthDao().upsert(newJm)
            newJm
        }
    }

    suspend fun updateGoal(monthKey: String, goal: Int) = db.withTransaction {
        db.jarMonthDao().upsert(JarMonthEntity(monthKey, goal.coerceIn(1, 200)))
    }

    suspend fun markCoinsSeen(monthKey: String) = db.withTransaction {
        db.coinDao().markAllSeen(monthKey)
    }
}
