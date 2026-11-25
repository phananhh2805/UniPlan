package com.uniplan.app.backup

import androidx.room.withTransaction
import com.google.gson.Gson
import com.uniplan.app.data.local.db.*
import com.uniplan.app.domain.model.CoinSource
import com.uniplan.app.domain.model.CoinTier
import com.uniplan.app.domain.model.Priority
import com.uniplan.app.util.DateTimeProvider
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.random.Random

sealed class RestoreResult {
    data class Success(val itemCount: Int) : RestoreResult()
    data class Error(val message: String) : RestoreResult()
}

@Singleton
class BackupManager @Inject constructor(
    private val db: AppDatabase,
    private val gson: Gson,
    private val dateTimeProvider: DateTimeProvider
) {
    suspend fun exportJson(): String = db.withTransaction {
        val currentSem = db.semesterDao().getCurrent()
        val semId = currentSem?.id ?: 0L
        val dto = BackupDto(
            schemaVersion = 1,
            appVersion = "1.0.0",
            exportedAt = dateTimeProvider.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")),
            semesters = if (currentSem != null) listOf(currentSem) else emptyList(),
            subjects = if (semId != 0L) db.subjectDao().getBySemester(semId) else emptyList(),
            classSessions = if (semId != 0L) db.classSessionDao().getBySemester(semId) else emptyList(),
            classExceptions = emptyList(),
            tasks = db.taskDao().getCompletedBetween(0, Long.MAX_VALUE),
            subtasks = emptyList(),
            exams = emptyList(),
            timeBlocks = emptyList(),
            reminders = emptyList(),
            focusSessions = emptyList(),
            coins = emptyList(),
            jarMonths = emptyList()
        )
        gson.toJson(dto)
    }

    suspend fun restoreJson(jsonString: String): RestoreResult {
        return try {
            val dto = gson.fromJson(jsonString, BackupDto::class.java)
                ?: return RestoreResult.Error("Tệp sao lưu không hợp lệ")

            if (dto.schemaVersion > 1) {
                return RestoreResult.Error("Tệp được tạo bởi phiên bản mới hơn, vui lòng cập nhật ứng dụng")
            }

            db.withTransaction {
                // Clear existing tables
                db.clearAllTables()

                // Insert semesters
                dto.semesters.forEach { db.semesterDao().insert(it) }
                dto.subjects.forEach { db.subjectDao().insert(it) }
                dto.classSessions.forEach { db.classSessionDao().insert(it) }
                dto.classExceptions.forEach { db.classExceptionDao().upsert(it) }
                dto.tasks.forEach { db.taskDao().insert(it) }
                dto.subtasks.forEach { db.subtaskDao().insert(it) }
                dto.exams.forEach { db.examDao().insert(it) }
                dto.timeBlocks.forEach { db.timeBlockDao().insert(it) }
                dto.reminders.forEach { db.reminderDao().insert(it) }
                dto.focusSessions.forEach { db.focusSessionDao().insert(it) }
                dto.jarMonths.forEach { db.jarMonthDao().upsert(it) }

                // Restoring coins
                if (dto.coins.isNotEmpty()) {
                    dto.coins.forEach { db.coinDao().insert(it) }
                } else {
                    // Fallback for old backup without coins array (TC-53 / Section 6.9.4)
                    val doneTasks = db.taskDao().getCompletedBetween(0, Long.MAX_VALUE)
                    doneTasks.forEach { t ->
                        val completedAtMs = t.completedAt ?: t.updatedAt
                        val completedDateTime = LocalDateTime.now() // fallback
                        val monthKey = completedDateTime.format(DateTimeFormatter.ofPattern("yyyy-MM"))
                        val tier = when (t.priority) {
                            Priority.LOW -> CoinTier.BRONZE
                            Priority.MEDIUM -> CoinTier.SILVER
                            Priority.HIGH -> CoinTier.GOLD
                        }
                        db.coinDao().insert(
                            CoinEntity(
                                monthKey = monthKey,
                                earnedAt = completedDateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm")),
                                tier = tier,
                                sourceType = CoinSource.TASK,
                                sourceId = t.id,
                                label = t.title.take(120),
                                subjectId = t.subjectId,
                                isOnTime = true,
                                seed = Random.nextInt(),
                                isSeen = true
                            )
                        )
                    }
                }
            }

            val totalCount = dto.semesters.size + dto.subjects.size + dto.classSessions.size +
                    dto.tasks.size + dto.exams.size + dto.timeBlocks.size
            RestoreResult.Success(totalCount)
        } catch (e: Exception) {
            RestoreResult.Error("Tệp sao lưu không hợp lệ: ${e.message}")
        }
    }
}
