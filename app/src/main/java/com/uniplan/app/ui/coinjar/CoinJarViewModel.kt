package com.uniplan.app.ui.coinjar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.uniplan.app.data.local.db.CoinEntity
import com.uniplan.app.data.repository.CoinJarRepository
import com.uniplan.app.util.DateTimeProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.format.DateTimeFormatter
import javax.inject.Inject

data class CoinJarUiState(
    val currentMonthKey: String = "",
    val coins: List<CoinEntity> = emptyList(),
    val goal: Int = 30,
    val selectedCoin: CoinEntity? = null
)

@HiltViewModel
class CoinJarViewModel @Inject constructor(
    private val repository: CoinJarRepository,
    private val dateTimeProvider: DateTimeProvider
) : ViewModel() {

    private val _uiState = MutableStateFlow(CoinJarUiState())
    val uiState: StateFlow<CoinJarUiState> = _uiState.asStateFlow()

    init {
        val currentMonth = dateTimeProvider.now().format(DateTimeFormatter.ofPattern("yyyy-MM"))
        loadMonth(currentMonth)
    }

    fun loadMonth(monthKey: String) {
        viewModelScope.launch {
            val jarMonth = repository.getJarMonth(monthKey)
            _uiState.value = _uiState.value.copy(
                currentMonthKey = monthKey,
                goal = jarMonth.goal
            )

            repository.observeCoinsByMonth(monthKey).collect { list ->
                _uiState.value = _uiState.value.copy(coins = list)
            }
        }
    }

    fun updateGoal(newGoal: Int) {
        viewModelScope.launch {
            val month = _uiState.value.currentMonthKey
            repository.updateGoal(month, newGoal)
            _uiState.value = _uiState.value.copy(goal = newGoal)
        }
    }

    fun selectCoin(coin: CoinEntity?) {
        _uiState.value = _uiState.value.copy(selectedCoin = coin)
    }
}
