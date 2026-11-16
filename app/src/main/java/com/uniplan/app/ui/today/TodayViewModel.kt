package com.uniplan.app.ui.today

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.uniplan.app.UniPlanApp
import com.uniplan.app.data.local.db.CoinEntity
import com.uniplan.app.data.local.db.SemesterEntity
import com.uniplan.app.data.local.db.TaskEntity
import com.uniplan.app.domain.model.Occurrence
import com.uniplan.app.util.VietnameseText
import com.uniplan.app.util.WeekCalculator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth

data class TodayUiState(
    val dateText: String = "",
    val weekText: String = "",
    val currentSemester: SemesterEntity? = null,
    val todayOccurrences: List<Occurrence> = emptyList(),
    val upcomingTasks: List<TaskEntity> = emptyList(),
    val coinsThisMonth: List<CoinEntity> = emptyList(),
    val jarGoal: Int = 30,
    val nextClassMessage: String = "Chưa có lịch học sắp tới"
)

class TodayViewModel : ViewModel() {

    private val semesterRepository = UniPlanApp.instance.semesterRepository
    private val scheduleRepository = UniPlanApp.instance.scheduleRepository
    private val taskRepository = UniPlanApp.instance.taskRepository
    private val coinJarRepository = UniPlanApp.instance.coinJarRepository
    private val dateTimeProvider = UniPlanApp.instance.dateTimeProvider

    private val _uiState = MutableStateFlow(TodayUiState())
    val uiState: StateFlow<TodayUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    fun loadData() {
        viewModelScope.launch {
            val today = dateTimeProvider.today()
            val dateText = VietnameseText.fullDate(today)

            val currentSem = semesterRepository.observeCurrent().firstOrNull()
            var weekText = "Ngoài học kỳ"
            if (currentSem != null) {
                val start = LocalDate.parse(currentSem.startDate)
                val end = LocalDate.parse(currentSem.endDate)
                val weekIdx = WeekCalculator.weekIndex(start, end, today)
                if (weekIdx != null) {
                    weekText = "Tuần $weekIdx của ${currentSem.name}"
                }
            }

            val occurrences = if (currentSem != null) {
                scheduleRepository.observeOccurrencesForDate(currentSem.id, today).firstOrNull() ?: emptyList()
            } else emptyList()

            val openTasks = taskRepository.observeOpen().firstOrNull() ?: emptyList()
            val upcomingTasks = openTasks.take(5)

            val monthKey = YearMonth.from(today).toString()
            val coins = coinJarRepository.observeCoinsByMonth(monthKey).firstOrNull() ?: emptyList()
            val jarMonth = coinJarRepository.getJarMonth(monthKey)
            val goal = jarMonth?.goal ?: 30

            val nowTime = dateTimeProvider.nowTime()
            val activeOccs = occurrences.filter { it.status == null }
            val currentOcc = activeOccs.find { nowTime.isAfter(it.startTime) && nowTime.isBefore(it.endTime) }
            val nextOcc = activeOccs.find { it.startTime.isAfter(nowTime) }

            val nextMsg = when {
                currentOcc != null -> "Đang học: ${currentOcc.subjectName} · Phòng ${currentOcc.room ?: "---"}"
                nextOcc != null -> "Tiết kế tiếp: ${nextOcc.subjectName} lúc ${nextOcc.startTime}"
                activeOccs.isNotEmpty() -> "Hôm nay đã hết tiết học"
                else -> "Chưa có lịch học sắp tới"
            }

            _uiState.value = TodayUiState(
                dateText = dateText,
                weekText = weekText,
                currentSemester = currentSem,
                todayOccurrences = occurrences,
                upcomingTasks = upcomingTasks,
                coinsThisMonth = coins,
                jarGoal = goal,
                nextClassMessage = nextMsg
            )
        }
    }
}
