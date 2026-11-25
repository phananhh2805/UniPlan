package com.uniplan.app.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.uniplan.app.backup.BackupManager
import com.uniplan.app.backup.RestoreResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class BackupUiState(
    val exportedJson: String? = null,
    val restoreMessage: String? = null,
    val isSuccess: Boolean = false,
    val isLoading: Boolean = false
)

@HiltViewModel
class BackupViewModel @Inject constructor(
    private val backupManager: BackupManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(BackupUiState())
    val uiState: StateFlow<BackupUiState> = _uiState.asStateFlow()

    fun exportData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val json = backupManager.exportJson()
            _uiState.value = _uiState.value.copy(
                exportedJson = json,
                isLoading = false,
                restoreMessage = "Đã tạo dữ liệu sao lưu"
            )
        }
    }

    fun restoreData(jsonContent: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            when (val res = backupManager.restoreJson(jsonContent)) {
                is RestoreResult.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isSuccess = true,
                        restoreMessage = "Đã khôi phục thành công ${res.itemCount} mục"
                    )
                }
                is RestoreResult.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isSuccess = false,
                        restoreMessage = res.message
                    )
                }
            }
        }
    }
}
