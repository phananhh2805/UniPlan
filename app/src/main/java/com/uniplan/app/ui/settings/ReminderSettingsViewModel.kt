package com.uniplan.app.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.uniplan.app.data.local.prefs.SettingsDataStore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ReminderSettingsUiState(
    val remClassEnabled: Boolean = true,
    val remTaskEnabled: Boolean = true,
    val remExamEnabled: Boolean = true,
    val dndEnabled: Boolean = false,
    val dndStart: String = "23:00",
    val dndEnd: String = "06:00"
)

@HiltViewModel
class ReminderSettingsViewModel @Inject constructor(
    private val settingsDataStore: SettingsDataStore
) : ViewModel() {

    private val _uiState = MutableStateFlow(ReminderSettingsUiState())
    val uiState: StateFlow<ReminderSettingsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            settingsDataStore.remClassEnabled.collect { e -> _uiState.value = _uiState.value.copy(remClassEnabled = e) }
        }
        viewModelScope.launch {
            settingsDataStore.remTaskEnabled.collect { e -> _uiState.value = _uiState.value.copy(remTaskEnabled = e) }
        }
        viewModelScope.launch {
            settingsDataStore.remExamEnabled.collect { e -> _uiState.value = _uiState.value.copy(remExamEnabled = e) }
        }
        viewModelScope.launch {
            settingsDataStore.dndEnabled.collect { e -> _uiState.value = _uiState.value.copy(dndEnabled = e) }
        }
    }

    fun setRemClass(e: Boolean) { viewModelScope.launch { settingsDataStore.setRemClassEnabled(e) } }
    fun setRemTask(e: Boolean) { viewModelScope.launch { settingsDataStore.setRemTaskEnabled(e) } }
    fun setRemExam(e: Boolean) { viewModelScope.launch { settingsDataStore.setRemExamEnabled(e) } }
    fun setDnd(e: Boolean) { viewModelScope.launch { settingsDataStore.setDnd(e, _uiState.value.dndStart, _uiState.value.dndEnd) } }
}
