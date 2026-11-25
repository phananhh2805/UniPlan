package com.uniplan.app.data.repository

import androidx.room.withTransaction
import com.uniplan.app.data.local.db.*
import com.uniplan.app.domain.model.CoinSource
import com.uniplan.app.domain.model.CoinTier
import com.uniplan.app.util.DateTimeProvider
import kotlinx.coroutines.flow.Flow
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.random.Random

@Singleton
class FocusRepository @Inject constructor(
    private val db: AppDatabase,
    private val dateTimeProvider: DateTimeProvider
) {
    fun observeTotalBySubject(fromMs: Long, toMs: Long): Flow<List<SubjectTime>> =
        db.focusSessionDao().totalBySubject(fromMs, toMs)

    fun observeTotalFocusTime(fromMs: Long, toMs: Long): Flow<Int?> =
        db.focusSessionDao().totalFocusTime(fromMs, toMs)

    suspend fun saveFocusSession(
        taskId: Long?,
        subjectId: Long?,
        startedAt: Long,
        plannedMin: Int,
        actualSec: Int,
        isCompleted: Boolean
    ): Long = db.withTransaction {
        val fs = FocusSessionEntity(
            taskId = taskId,
            subjectId = subjectId,
            startedAt = startedAt,
            plannedMin = plannedMin,
            actualSec = actualSec,
            isCompleted = isCompleted
        )
        val id = db.focusSessionDao().insert(fs)

        // Award FOCUS coin if completed and actualSec >= 1500 (25 min) (FR-40)
        if (isCompleted && actualSec >= 1500) {
            val now = dateTimeProvider.now()
            val monthKey = now.format(DateTimeFormatter.ofPattern("yyyy-MM"))
            val subject = subjectId?.let { db.subjectDao().getById(it) }

            val coin = CoinEntity(
                monthKey = monthKey,
                earnedAt = now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm")),
                tier = CoinTier.FOCUS,
                sourceType = CoinSource.FOCUS,
                sourceId = id,
                label = "Phiên tập trung",
                subjectId = subjectId,
                subjectName = subject?.name,
                subjectColor = subject?.color,
                isOnTime = true,
                seed = Random.nextInt(),
                isSeen = false
            )
            db.coinDao().insert(coin)

            if (db.jarMonthDao().getByMonth(monthKey) == null) {
                db.jarMonthDao().upsert(JarMonthEntity(monthKey, 30))
            }
        }

        id
    }
}
