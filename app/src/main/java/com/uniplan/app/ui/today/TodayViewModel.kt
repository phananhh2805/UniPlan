package com.uniplan.app.ui.today

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.uniplan.app.data.local.db.*
import com.uniplan.app.data.repository.*
import com.uniplan.app.domain.model.Occurrence
import com.uniplan.app.domain.model.TaskStatus
import com.uniplan.app.domain.usecase.NextClassState
import com.uniplan.app.domain.usecase.NextClassUseCase
import com.uniplan.app.util.DateTimeProvider
import com.uniplan.app.util.WeekCalculator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject

data class TodayUiState(
    val currentSemester: SemesterEntity? = null,
    val weekLabel: String = "",
    val todayDate: LocalDate = LocalDate.now(),
    val nextClassState: NextClassState = NextClassState.NoUpcoming,
    val todayOccurrences: List<Occurrence> = emptyList(),
    val upcomingTasks: List<TaskEntity> = emptyList(),
    val upcomingExams: List<ExamEntity> = emptyList(),
    val coinCount: Int = 0,
    val isLoading: Boolean = true
)

@HiltViewModel
class TodayViewModel @Inject constructor(
    private val semesterRepository: SemesterRepository,
    private val scheduleRepository: ScheduleRepository,
    private val taskRepository: TaskRepository,
    private val examRepository: ExamRepository,
    private val coinJarRepository: CoinJarRepository,
    private val nextClassUseCase: NextClassUseCase,
    private val dateTimeProvider: DateTimeProvider
) : ViewModel() {

    private val _uiState = MutableStateFlow(TodayUiState())
    val uiState: StateFlow<TodayUiState> = _uiState.asStateFlow()

    init {
        loadData()
        startMinuteTicker()
    }

    private fun loadData() {
        viewModelScope.launch {
            semesterRepository.currentSemesterFlow.collect { sem ->
                val today = dateTimeProvider.today()
                val now = dateTimeProvider.now()

                if (sem == null) {
                    _uiState.value = TodayUiState(isLoading = false)
                    return@collect
                }

                val weekIndex = WeekCalculator.weekIndex(
                    LocalDate.parse(sem.startDate),
                    LocalDate.parse(sem.endDate),
                    today
                )
                val weekLabel = if (weekIndex != null) "Tuần $weekIndex của ${sem.name}" else "Ngoài học kỳ"

                combine(
                    scheduleRepository.observeOccurrencesForDate(sem, today),
                    taskRepository.openTasksFlow,
                    examRepository.allExamsFlow
                ) { occurrences, tasks, exams ->
                    val nextClass = nextClassUseCase.calculate(occurrences, now)
                    val topTasks = tasks.filter { it.status != TaskStatus.DONE }.take(5)

                    val monthKey = now.format(DateTimeFormatter.ofPattern("yyyy-MM"))
                    val coins = coinJarRepository.getCoinsByMonth(monthKey)

                    TodayUiState(
                        currentSemester = sem,
                        weekLabel = weekLabel,
                        todayDate = today,
                        nextClassState = nextClass,
                        todayOccurrences = occurrences,
                        upcomingTasks = topTasks,
                        upcomingExams = exams.take(2),
                        coinCount = coins.size,
                        isLoading = false
                    )
                }.collect { state ->
                    _uiState.value = state
                }
            }
        }
    }

    private fun startMinuteTicker() {
        viewModelScope.launch {
            while (true) {
                delay(60000L)
                val state = _uiState.value
                if (state.currentSemester != null) {
                    val nextClass = nextClassUseCase.calculate(state.todayOccurrences, dateTimeProvider.now())
                    _uiState.value = state.copy(nextClassState = nextClass)
                }
            }
        }
    }

    fun markTaskDone(taskId: Long) {
        viewModelScope.launch {
            taskRepository.markTaskDone(taskId)
        }
    }
}
