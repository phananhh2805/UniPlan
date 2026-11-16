package com.uniplan.app.data.local.prefs

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.google.gson.Gson
import com.uniplan.app.domain.model.PeriodInfo
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalTime

private val Context.periodDataStore by preferencesDataStore(name = "uniplan_periods")

data class PeriodDto(
    val period: Int,
    val start: String,
    val end: String
)

class PeriodConfigStore(
    private val context: Context,
    private val gson: Gson
) {
    private val PERIODS_JSON = stringPreferencesKey("periods_json")

    val periods: Flow<List<PeriodInfo>> = context.periodDataStore.data.map { prefs ->
        val json = prefs[PERIODS_JSON]
        val dtos: List<PeriodDto> = if (json.isNullOrEmpty()) {
            defaultPeriods()
        } else {
            try {
                gson.fromJson(json, Array<PeriodDto>::class.java).toList()
            } catch (e: Exception) {
                defaultPeriods()
            }
        }
        dtos.map {
            PeriodInfo(
                period = it.period,
                startTime = LocalTime.parse(it.start),
                endTime = LocalTime.parse(it.end)
            )
        }
    }

    suspend fun savePeriods(periods: List<PeriodInfo>) {
        val dtos = periods.map {
            PeriodDto(
                period = it.period,
                start = it.startTime.toString(),
                end = it.endTime.toString()
            )
        }
        context.periodDataStore.edit {
            it[PERIODS_JSON] = gson.toJson(dtos)
        }
    }

    suspend fun resetToDefault() {
        savePeriods(defaultPeriods().map {
            PeriodInfo(it.period, LocalTime.parse(it.start), LocalTime.parse(it.end))
        })
    }

    companion object {
        fun defaultPeriods(): List<PeriodDto> = listOf(
            PeriodDto(1, "07:00", "07:50"),
            PeriodDto(2, "07:55", "08:45"),
            PeriodDto(3, "08:50", "09:40"),
            PeriodDto(4, "09:45", "10:35"),
            PeriodDto(5, "10:40", "11:30"),
            PeriodDto(6, "11:35", "12:25"),
            PeriodDto(7, "13:00", "13:50"),
            PeriodDto(8, "13:55", "14:45"),
            PeriodDto(9, "14:50", "15:40"),
            PeriodDto(10, "15:45", "16:35"),
            PeriodDto(11, "16:40", "17:30"),
            PeriodDto(12, "17:35", "18:25")
        )
    }
}
