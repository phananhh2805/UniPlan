package com.uniplan.app.domain.usecase

import com.uniplan.app.domain.model.PlacedTimelineItem
import com.uniplan.app.domain.model.TimelineItem
import java.time.LocalTime

class GetDayTimelineUseCase {

    fun layoutColumns(items: List<TimelineItem>): List<PlacedTimelineItem> {
        val timedItems = items.filter { it !is TimelineItem.AllDayOverdue && it !is TimelineItem.DeadlineItem }
            .sortedWith(compareBy({ it.start }, { it.end }))

        val result = mutableListOf<PlacedTimelineItem>()

        var cluster = mutableListOf<TimelineItem>()
        var clusterEnd = LocalTime.MIN

        fun flush() {
            if (cluster.isEmpty()) return
            val columns = mutableListOf<MutableList<TimelineItem>>()
            for (item in cluster) {
                var placed = false
                for (col in columns) {
                    val lastInCol = col.last()
                    if (!item.start.isBefore(lastInCol.end)) {
                        col.add(item)
                        placed = true
                        break
                    }
                }
                if (!placed) {
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

        for (item in timedItems) {
            if (cluster.isNotEmpty() && !item.start.isBefore(clusterEnd)) {
                flush()
                cluster = mutableListOf()
                clusterEnd = LocalTime.MIN
            }
            cluster.add(item)
            if (item.end.isAfter(clusterEnd)) {
                clusterEnd = item.end
            }
        }
        flush()

        items.filter { it is TimelineItem.DeadlineItem || it is TimelineItem.AllDayOverdue }
            .forEach { result.add(PlacedTimelineItem(it, 0, 1)) }

        return result
    }
}
