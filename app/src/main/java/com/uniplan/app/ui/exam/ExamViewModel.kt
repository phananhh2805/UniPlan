package com.uniplan.app.ui.exam

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.uniplan.app.UniPlanApp
import com.uniplan.app.data.local.db.ExamEntity
import kotlinx.coroutines.launch

class ExamViewModel : ViewModel() {

    private val examRepository = UniPlanApp.instance.examRepository
    private val dateTimeProvider = UniPlanApp.instance.dateTimeProvider

    fun saveExam(subjectId: Long, examType: String, onComplete: () -> Unit) {
        viewModelScope.launch {
            val now = dateTimeProvider.now()
            val exam = ExamEntity(
                subjectId = subjectId,
                examType = examType,
                startAt = now.plusDays(7).toString(),
                durationMin = 90,
                room = "A101",
                note = null
            )
            examRepository.saveExam(exam, listOf(4320, 1440, 60))
            onComplete()
        }
    }
}
