package com.uniplan.app.domain.model

import java.time.LocalTime

sealed class TimelineItem {
    abstract val start: LocalTime
    abstract val end: LocalTime

    data class ClassItem(
        val occurrence: Occurrence
    ) : TimelineItem() {
        override val start: LocalTime get() = occurrence.startTime
        override val end: LocalTime get() = occurrence.endTime
    }

    data class BlockItem(
        val id: Long,
        val title: String,
        override val start: LocalTime,
        override val end: LocalTime,
        val category: BlockCategory,
        val color: Int,
        val icon: String
    ) : TimelineItem()

    data class ExamItem(
        val id: Long,
        val subjectName: String,
        val examType: String,
        override val start: LocalTime,
        override val end: LocalTime,
        val room: String?
    ) : TimelineItem()

    data class DeadlineItem(
        val id: Long,
        val title: String,
        val dueTime: LocalTime,
        val priority: Priority,
        val subjectColor: Int?
    ) : TimelineItem() {
        override val start: LocalTime get() = dueTime
        override val end: LocalTime get() = dueTime
    }
}

data class PlacedTimelineItem(
    val item: TimelineItem,
    val col: Int,
    val colCount: Int
)

object TimelineColumnLayout {
    fun layoutColumns(items: List<TimelineItem>): List<PlacedTimelineItem> {
        // Deadlines do not participate in column layout
        val timedItems = items.filter { it !is TimelineItem.DeadlineItem }
        val deadlines = items.filterIsInstance<TimelineItem.DeadlineItem>()

        val sorted = timedItems.sortedWith(compareBy({ it.start }, { -it.end.toSecondOfDay() }))
        val result = mutableListOf<PlacedTimelineItem>()

        var cluster = mutableListOf<TimelineItem>()
        var clusterEnd = LocalTime.MIN

        fun flush() {
            if (cluster.isEmpty()) return
            val columns = mutableListOf<MutableList<TimelineItem>>()
            for (item in cluster) {
                var placedInCol = false
                for (col in columns) {
                    val last = col.last()
                    if (!last.end.isAfter(item.start)) {
                        col.add(item)
                        placedInCol = true
                        break
                    }
                }
                if (!placedInCol) {
                    columns.add(mutableListOf(item))
                }
            }
            val colCount = columns.size
            for ((colIndex, colList) in columns.withIndex()) {
                for (item in colList) {
                    result.add(PlacedTimelineItem(item, colIndex, colCount))
                }
            }
        }

        for (it in sorted) {
            if (cluster.isNotEmpty() && !it.start.isBefore(clusterEnd)) {
                flush()
                cluster = mutableListOf()
                clusterEnd = LocalTime.MIN
            }
            cluster.add(it)
            if (it.end.isAfter(clusterEnd)) {
                clusterEnd = it.end
            }
        }
        flush()

        // Append deadlines with col=0, colCount=1
        for (d in deadlines) {
            result.add(PlacedTimelineItem(d, 0, 1))
        }

        return result
    }
}
