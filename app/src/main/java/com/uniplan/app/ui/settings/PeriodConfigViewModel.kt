package com.uniplan.app.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.uniplan.app.data.local.prefs.PeriodConfig
import com.uniplan.app.data.local.prefs.PeriodConfigStore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PeriodConfigUiState(
    val periods: List<PeriodConfig> = emptyList(),
    val isSaved: Boolean = false
)

@HiltViewModel
class PeriodConfigViewModel @Inject constructor(
    private val periodConfigStore: PeriodConfigStore
) : ViewModel() {

    private val _uiState = MutableStateFlow(PeriodConfigUiState())
    val uiState: StateFlow<PeriodConfigUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            periodConfigStore.periodsFlow.collect { list ->
                _uiState.value = _uiState.value.copy(periods = list)
            }
        }
    }

    fun restoreDefault() {
        viewModelScope.launch {
            periodConfigStore.savePeriods(periodConfigStore.defaultPeriods)
        }
    }
}
