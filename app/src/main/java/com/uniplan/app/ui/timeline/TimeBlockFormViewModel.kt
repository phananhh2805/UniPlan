package com.uniplan.app.ui.timeline

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.uniplan.app.data.local.db.TimeBlockEntity
import com.uniplan.app.data.repository.TimeBlockRepository
import com.uniplan.app.domain.model.BlockCategory
import com.uniplan.app.util.DateTimeProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime
import javax.inject.Inject

data class TimeBlockFormUiState(
    val blockId: Long = 0L,
    val title: String = "",
    val date: LocalDate = LocalDate.now(),
    val startTime: LocalTime = LocalTime.of(8, 0),
    val endTime: LocalTime = LocalTime.of(9, 0),
    val category: BlockCategory = BlockCategory.STUDY,
    val isSaved: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class TimeBlockFormViewModel @Inject constructor(
    private val repository: TimeBlockRepository,
    private val dateTimeProvider: DateTimeProvider
) : ViewModel() {

    private val _uiState = MutableStateFlow(TimeBlockFormUiState(date = dateTimeProvider.today()))
    val uiState: StateFlow<TimeBlockFormUiState> = _uiState.asStateFlow()

    fun load(blockId: Long) {
        viewModelScope.launch {
            if (blockId != 0L) {
                val b = repository.getBlockById(blockId)
                if (b != null) {
                    _uiState.value = _uiState.value.copy(
                        blockId = b.id,
                        title = b.title,
                        date = LocalDate.parse(b.date),
                        startTime = LocalTime.parse(b.startTime),
                        endTime = LocalTime.parse(b.endTime),
                        category = b.category
                    )
                }
            }
        }
    }

    fun updateTitle(t: String) { _uiState.value = _uiState.value.copy(title = t) }
    fun updateCategory(c: BlockCategory) { _uiState.value = _uiState.value.copy(category = c) }

    fun save() {
        viewModelScope.launch {
            val state = _uiState.value
            if (state.title.isBlank()) {
                _uiState.value = state.copy(error = "Vui lòng nhập tên khối thời gian")
                return@launch
            }
            if (!state.endTime.isAfter(state.startTime)) {
                _uiState.value = state.copy(error = "Giờ kết thúc phải sau giờ bắt đầu")
                return@launch
            }

            val color = when (state.category) {
                BlockCategory.STUDY -> 0xFF1E88E5.toInt()
                BlockCategory.WORK -> 0xFF43A047.toInt()
                BlockCategory.REST -> 0xFFFB8C00.toInt()
                BlockCategory.EXERCISE -> 0xFFE53935.toInt()
                BlockCategory.OTHER -> 0xFF8E24AA.toInt()
            }

            val block = TimeBlockEntity(
                id = state.blockId,
                title = state.title.trim(),
                date = state.date.toString(),
                startTime = state.startTime.toString(),
                endTime = state.endTime.toString(),
                category = state.category,
                color = color
            )

            repository.saveBlock(block)
            _uiState.value = _uiState.value.copy(isSaved = true)
        }
    }
}
