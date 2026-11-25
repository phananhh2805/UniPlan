package com.uniplan.app.domain.usecase

import com.uniplan.app.data.local.db.TaskEntity
import com.uniplan.app.domain.model.Priority
import com.uniplan.app.domain.model.TaskStatus
import java.time.Duration
import java.time.LocalDateTime
import javax.inject.Inject

object PriorityRules {
    val URGENT_WITHIN: Duration = Duration.ofHours(48)

    fun isUrgent(t: TaskEntity, now: LocalDateTime): Boolean {
        if (t.dueAt == null) return false
        val due = LocalDateTime.parse(t.dueAt)
        return due <= now.plus(URGENT_WITHIN)
    }

    fun isImportant(t: TaskEntity): Boolean = t.priority == Priority.HIGH
}

data class PriorityGroup(
    val title: String,
    val tasks: List<TaskEntity>
)

data class PriorityMatrix(
    val doFirst: PriorityGroup,    // Làm ngay
    val plan: PriorityGroup,       // Lên kế hoạch
    val quickAction: PriorityGroup, // Xử lý nhanh
    val defer: PriorityGroup       // Để sau
)

class GroupTasksByPriorityUseCase @Inject constructor() {
    fun execute(tasks: List<TaskEntity>, now: LocalDateTime): PriorityMatrix {
        val openTasks = tasks.filter { it.status != TaskStatus.DONE }

        val doFirstList = mutableListOf<TaskEntity>()
        val planList = mutableListOf<TaskEntity>()
        val quickActionList = mutableListOf<TaskEntity>()
        val deferList = mutableListOf<TaskEntity>()

        for (t in openTasks) {
            val urgent = PriorityRules.isUrgent(t, now)
            val important = PriorityRules.isImportant(t)

            when {
                urgent && important -> doFirstList.add(t)
                !urgent && important -> planList.add(t)
                urgent && !important -> quickActionList.add(t)
                else -> deferList.add(t)
            }
        }

        val comparator = Comparator<TaskEntity> { a, b ->
            when {
                a.dueAt != null && b.dueAt != null -> a.dueAt.compareTo(b.dueAt)
                a.dueAt != null -> -1
                b.dueAt != null -> 1
                else -> b.priority.ordinal.compareTo(a.priority.ordinal)
            }
        }

        return PriorityMatrix(
            doFirst = PriorityGroup("Làm ngay", doFirstList.sortedWith(comparator)),
            plan = PriorityGroup("Lên kế hoạch", planList.sortedWith(comparator)),
            quickAction = PriorityGroup("Xử lý nhanh", quickActionList.sortedWith(comparator)),
            defer = PriorityGroup("Để sau", deferList.sortedWith(comparator))
        )
    }
}
