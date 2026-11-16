package com.uniplan.app.ui.period

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.uniplan.app.UniPlanApp
import com.uniplan.app.domain.model.PeriodInfo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

class PeriodConfigViewModel : ViewModel() {

    private val periodConfigStore = UniPlanApp.instance.periodConfigStore

    private val _periods = MutableStateFlow<List<PeriodInfo>>(emptyList())
    val periods: StateFlow<List<PeriodInfo>> = _periods.asStateFlow()

    init {
        loadPeriods()
    }

    fun loadPeriods() {
        viewModelScope.launch {
            val list = periodConfigStore.periods.firstOrNull() ?: emptyList()
            _periods.value = list
        }
    }

    fun resetDefault() {
        viewModelScope.launch {
            periodConfigStore.resetToDefault()
            loadPeriods()
        }
    }
}
