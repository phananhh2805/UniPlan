package com.uniplan.app.ui.jar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.uniplan.app.UniPlanApp
import com.uniplan.app.data.local.db.CoinEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import java.time.YearMonth

data class CoinJarUiState(
    val monthKey: String = "",
    val coins: List<CoinEntity> = emptyList(),
    val goal: Int = 30,
    val motivationalText: String = "Lọ còn trống, việc đầu tiên đang chờ bạn"
)

class CoinJarViewModel : ViewModel() {

    private val coinJarRepository = UniPlanApp.instance.coinJarRepository
    private val dateTimeProvider = UniPlanApp.instance.dateTimeProvider

    private val _uiState = MutableStateFlow(CoinJarUiState())
    val uiState: StateFlow<CoinJarUiState> = _uiState.asStateFlow()

    init {
        loadMonthData()
    }

    fun loadMonthData() {
        viewModelScope.launch {
            val today = dateTimeProvider.today()
            val monthKey = YearMonth.from(today).toString()

            val coins = coinJarRepository.observeCoinsByMonth(monthKey).firstOrNull() ?: emptyList()
            val jarMonth = coinJarRepository.getJarMonth(monthKey)
            val goal = jarMonth?.goal ?: 30

            val size = coins.size
            val msg = when {
                size == 0 -> "Lọ còn trống, việc đầu tiên đang chờ bạn"
                size < goal / 2 -> "Đã có $size xu, cứ thế tiến lên"
                size < goal -> "Hơn nửa lọ rồi, tuyệt vời"
                else -> "Đầy lọ! Bạn đã hoàn thành mục tiêu tháng"
            }

            _uiState.value = CoinJarUiState(
                monthKey = monthKey,
                coins = coins,
                goal = goal,
                motivationalText = msg
            )

            coinJarRepository.markSeen(monthKey)
        }
    }
}
