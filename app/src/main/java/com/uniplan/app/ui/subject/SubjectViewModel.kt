package com.uniplan.app.ui.subject

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.uniplan.app.UniPlanApp
import com.uniplan.app.data.local.db.SubjectEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

class SubjectViewModel : ViewModel() {

    private val subjectRepository = UniPlanApp.instance.subjectRepository
    private val semesterRepository = UniPlanApp.instance.semesterRepository

    private val _subjects = MutableStateFlow<List<SubjectEntity>>(emptyList())
    val subjects: StateFlow<List<SubjectEntity>> = _subjects.asStateFlow()

    init {
        loadSubjects()
    }

    fun loadSubjects() {
        viewModelScope.launch {
            val sem = semesterRepository.observeCurrent().firstOrNull()
            if (sem != null) {
                val list = subjectRepository.observeBySemester(sem.id).firstOrNull() ?: emptyList()
                _subjects.value = list
            }
        }
    }

    fun saveSubject(name: String, code: String?, credits: Int?, onComplete: () -> Unit) {
        viewModelScope.launch {
            val sem = semesterRepository.observeCurrent().firstOrNull() ?: return@launch
            val subject = SubjectEntity(
                semesterId = sem.id,
                name = name,
                code = code,
                credits = credits,
                teacher = null,
                color = 0xFF1E88E5.toInt(),
                icon = "book",
                note = null
            )
            subjectRepository.saveSubject(subject)
            loadSubjects()
            onComplete()
        }
    }
}
