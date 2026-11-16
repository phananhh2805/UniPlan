package com.uniplan.app.ui.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.uniplan.app.UniPlanApp
import com.uniplan.app.data.local.db.SemesterEntity
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.temporal.TemporalAdjusters

class OnboardingViewModel : ViewModel() {

    private val semesterRepository = UniPlanApp.instance.semesterRepository
    private val settingsDataStore = UniPlanApp.instance.settingsDataStore
    private val dateTimeProvider = UniPlanApp.instance.dateTimeProvider

    fun completeOnboarding(semesterName: String, onComplete: () -> Unit) {
        viewModelScope.launch {
            val name = semesterName.ifBlank { "Học kỳ này" }
            val today = dateTimeProvider.today()
            val start = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
            val end = start.plusWeeks(18)

            val semester = SemesterEntity(
                name = name,
                startDate = start.toString(),
                endDate = end.toString(),
                isCurrent = true
            )
            semesterRepository.saveSemester(semester)
            settingsDataStore.setOnboardingDone(true)
            onComplete()
        }
    }
}
