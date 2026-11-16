package com.uniplan.app.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.uniplan.app.UniPlanApp
import kotlinx.coroutines.launch

class BackupViewModel : ViewModel() {

    private val backupManager = UniPlanApp.instance.backupManager

    fun exportBackup(onResult: (String) -> Unit) {
        viewModelScope.launch {
            val json = backupManager.exportJson()
            onResult(json)
        }
    }

    fun importBackup(jsonStr: String, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            val success = backupManager.importJson(jsonStr)
            onResult(success)
        }
    }
}
