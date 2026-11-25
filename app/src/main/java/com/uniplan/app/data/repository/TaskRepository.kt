package com.uniplan.app.data.repository

import androidx.room.withTransaction
import com.uniplan.app.data.local.db.*
import com.uniplan.app.domain.model.CoinSource
import com.uniplan.app.domain.model.CoinTier
import com.uniplan.app.domain.model.Priority
import com.uniplan.app.domain.model.TaskStatus
import com.uniplan.app.util.DateTimeProvider
import kotlinx.coroutines.flow.Flow
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlin.random.Random
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TaskRepository @Inject constructor(
    private val db: AppDatabase,
    private val dateTimeProvider: DateTimeProvider
) {
    val openTasksFlow: Flow<List<TaskEntity>> = db.taskDao().observeOpen()
    val doneTasksFlow: Flow<List<TaskEntity>> = db.taskDao().observeDone()
    val allTasksFlow: Flow<List<TaskEntity>> = db.taskDao().observeAll()

    fun countCompleted(fromMs: Long, toMs: Long): Flow<Int> = db.taskDao().countCompleted(fromMs, toMs)

    suspend fun getTaskById(id: Long): TaskEntity? = db.taskDao().getById(id)

    fun observeSubtasks(taskId: Long): Flow<List<SubtaskEntity>> = db.subtaskDao().observeByTask(taskId)
    suspend fun getSubtasks(taskId: Long): List<SubtaskEntity> = db.subtaskDao().getByTask(taskId)

    suspend fun saveTask(task: TaskEntity, subtasks: List<SubtaskEntity>): Long = db.withTransaction {
        val now = dateTimeProvider.nowMillis()
        val taskId = if (task.id == 0L) {
            val newTask = task.copy(createdAt = now, updatedAt = now)
            db.taskDao().insert(newTask)
        } else {
            val updatedTask = task.copy(updatedAt = now)
            db.taskDao().update(updatedTask)
            task.id
        }

        // Replace subtasks
        db.subtaskDao().deleteByTask(taskId)
        if (subtasks.isNotEmpty()) {
            val withTaskId = subtasks.mapIndexed { idx, st ->
                st.copy(id = 0, taskId = taskId, position = idx)
            }
            db.subtaskDao().insertAll(withTaskId)
        }

        taskId
    }

    suspend fun markTaskDone(taskId: Long) = db.withTransaction {
        val task = db.taskDao().getById(taskId) ?: return@withTransaction
        val now = dateTimeProvider.now()
        val nowMs = dateTimeProvider.nowMillis()

        val updated = task.copy(
            status = TaskStatus.DONE,
            completedAt = nowMs,
            updatedAt = nowMs
        )
        db.taskDao().update(updated)

        // Coin Jar Logic (BR-J01)
        if (db.coinDao().findBySource(CoinSource.TASK, taskId) == null) {
            val monthKey = now.format(DateTimeFormatter.ofPattern("yyyy-MM"))
            val tier = when (task.priority) {
                Priority.LOW -> CoinTier.BRONZE
                Priority.MEDIUM -> CoinTier.SILVER
                Priority.HIGH -> CoinTier.GOLD
            }
            val subject = task.subjectId?.let { db.subjectDao().getById(it) }
            val isOnTime = task.dueAt == null || !now.isAfter(LocalDateTime.parse(task.dueAt))

            val coin = CoinEntity(
                monthKey = monthKey,
                earnedAt = now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm")),
                tier = tier,
                sourceType = CoinSource.TASK,
                sourceId = taskId,
                label = task.title.take(120),
                subjectId = task.subjectId,
                subjectName = subject?.name,
                subjectColor = subject?.color,
                isOnTime = isOnTime,
                seed = Random.nextInt(),
                isSeen = false
            )
            db.coinDao().insert(coin)

            // Ensure jar_month exists
            if (db.jarMonthDao().getByMonth(monthKey) == null) {
                db.jarMonthDao().upsert(JarMonthEntity(monthKey, 30))
            }
        }
    }

    suspend fun reopenTask(taskId: Long) = db.withTransaction {
        val task = db.taskDao().getById(taskId) ?: return@withTransaction
        val updated = task.copy(
            status = TaskStatus.TODO,
            completedAt = null,
            updatedAt = dateTimeProvider.nowMillis()
        )
        db.taskDao().update(updated)

        // Remove coin (BR-J03)
        db.coinDao().deleteBySource(CoinSource.TASK, taskId)
    }

    suspend fun updateTaskStatus(taskId: Long, status: TaskStatus) = db.withTransaction {
        if (status == TaskStatus.DONE) {
            markTaskDone(taskId)
        } else {
            val task = db.taskDao().getById(taskId) ?: return@withTransaction
            val updated = task.copy(
                status = status,
                completedAt = null,
                updatedAt = dateTimeProvider.nowMillis()
            )
            db.taskDao().update(updated)
            db.coinDao().deleteBySource(CoinSource.TASK, taskId)
        }
    }

    suspend fun deleteTask(taskId: Long) = db.withTransaction {
        // Detach coin source before deleting task (BR-J04)
        db.coinDao().detachSource(CoinSource.TASK, taskId)
        val task = db.taskDao().getById(taskId)
        if (task != null) {
            db.taskDao().delete(task)
        }
    }

    suspend fun updateSubtask(subtask: SubtaskEntity) = db.withTransaction {
        db.subtaskDao().update(subtask)
    }
}
