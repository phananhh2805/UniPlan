package com.uniplan.app

import com.uniplan.app.data.local.db.TaskEntity
import com.uniplan.app.domain.model.Priority
import com.uniplan.app.domain.model.TaskStatus
import com.uniplan.app.domain.model.TaskType
import com.uniplan.app.domain.usecase.GroupTasksByPriorityUseCase
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDateTime

class GroupTasksByPriorityUseCaseTest {

    @Test
    fun testPriorityGroups_TC24() {
        val now = LocalDateTime.of(2026, 11, 13, 10, 0)
        val useCase = GroupTasksByPriorityUseCase()

        val task1 = TaskEntity(1, null, "Task 1", null, now.plusHours(24).toString(), Priority.HIGH, TaskStatus.TODO, TaskType.ASSIGNMENT, 30, 0, 0, null)
        val task2 = TaskEntity(2, null, "Task 2", null, now.plusDays(5).toString(), Priority.HIGH, TaskStatus.TODO, TaskType.ASSIGNMENT, 30, 0, 0, null)
        val task3 = TaskEntity(3, null, "Task 3", null, now.plusHours(24).toString(), Priority.MEDIUM, TaskStatus.TODO, TaskType.ASSIGNMENT, 30, 0, 0, null)
        val task4 = TaskEntity(4, null, "Task 4", null, null, Priority.LOW, TaskStatus.TODO, TaskType.ASSIGNMENT, 30, 0, 0, null)

        val groups = useCase.execute(listOf(task1, task2, task3, task4), now)

        assertEquals(1, groups.doNow.size)
        assertEquals(1, groups.doNow.first().id)

        assertEquals(1, groups.plan.size)
        assertEquals(2, groups.plan.first().id)

        assertEquals(1, groups.quick.size)
        assertEquals(3, groups.quick.first().id)

        assertEquals(1, groups.delegateOrLater.size)
        assertEquals(4, groups.delegateOrLater.first().id)
    }
}
