package com.uniplan.app.ui.timeblock

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.uniplan.app.UniPlanApp
import com.uniplan.app.data.local.db.TimeBlockEntity
import com.uniplan.app.domain.model.BlockCategory
import com.uniplan.app.domain.model.RepeatRule
import kotlinx.coroutines.launch

class TimeBlockViewModel : ViewModel() {

    private val timeBlockRepository = UniPlanApp.instance.timeBlockRepository
    private val dateTimeProvider = UniPlanApp.instance.dateTimeProvider

    fun saveTimeBlock(title: String, onComplete: () -> Unit) {
        viewModelScope.launch {
            val today = dateTimeProvider.today()
            val block = TimeBlockEntity(
                taskId = null,
                title = title,
                date = today.toString(),
                startTime = "09:00",
                endTime = "10:00",
                category = BlockCategory.STUDY,
                color = 0xFF43A047.toInt(),
                icon = "pencil",
                repeatRule = RepeatRule.NONE,
                repeatUntil = null
            )
            timeBlockRepository.saveTimeBlock(block)
            onComplete()
        }
    }
}
