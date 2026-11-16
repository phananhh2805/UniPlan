package com.uniplan.app.domain.usecase

import com.uniplan.app.data.local.db.TaskEntity
import com.uniplan.app.domain.model.Priority
import com.uniplan.app.domain.model.TaskStatus
import java.time.LocalDateTime

data class PriorityGroups(
    val doNow: List<TaskEntity>,       // Urgent & Important
    val plan: List<TaskEntity>,        // Not Urgent & Important
    val quick: List<TaskEntity>,       // Urgent & Not Important
    val delegateOrLater: List<TaskEntity> // Not Urgent & Not Important
)

object PriorityRules {
    val URGENT_HOURS: Long = 48

    fun isUrgent(t: TaskEntity, now: LocalDateTime): Boolean {
        if (t.dueAt == null) return false
        val due = LocalDateTime.parse(t.dueAt)
        return !due.isAfter(now.plusHours(URGENT_HOURS)) // <= now + 48h (includes overdue)
    }

    fun isImportant(t: TaskEntity): Boolean = t.priority == Priority.HIGH
}

class GroupTasksByPriorityUseCase {

    fun execute(tasks: List<TaskEntity>, now: LocalDateTime): PriorityGroups {
        val openTasks = tasks.filter { it.status != TaskStatus.DONE }

        val doNow = mutableListOf<TaskEntity>()
        val plan = mutableListOf<TaskEntity>()
        val quick = mutableListOf<TaskEntity>()
        val later = mutableListOf<TaskEntity>()

        for (task in openTasks) {
            val urgent = PriorityRules.isUrgent(task, now)
            val important = PriorityRules.isImportant(task)

            when {
                urgent && important -> doNow.add(task)
                !urgent && important -> plan.add(task)
                urgent && !important -> quick.add(task)
                else -> later.add(task)
            }
        }

        val comparator = compareBy<TaskEntity>({ it.dueAt == null }, { it.dueAt }, { it.priority })

        return PriorityGroups(
            doNow = doNow.sortedWith(comparator),
            plan = plan.sortedWith(comparator),
            quick = quick.sortedWith(comparator),
            delegateOrLater = later.sortedWith(comparator)
        )
    }
}
