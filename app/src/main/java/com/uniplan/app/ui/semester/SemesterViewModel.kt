package com.uniplan.app.ui.semester

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.uniplan.app.UniPlanApp
import com.uniplan.app.data.local.db.SemesterEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

class SemesterViewModel : ViewModel() {

    private val semesterRepository = UniPlanApp.instance.semesterRepository

    private val _semesters = MutableStateFlow<List<SemesterEntity>>(emptyList())
    val semesters: StateFlow<List<SemesterEntity>> = _semesters.asStateFlow()

    init {
        loadSemesters()
    }

    fun loadSemesters() {
        viewModelScope.launch {
            val list = semesterRepository.observeAll().firstOrNull() ?: emptyList()
            _semesters.value = list
        }
    }

    fun setCurrent(id: Long) {
        viewModelScope.launch {
            semesterRepository.setCurrent(id)
            loadSemesters()
        }
    }
}
