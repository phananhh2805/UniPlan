package com.uniplan.app.data.local.prefs

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Singleton

data class PeriodConfig(
    val period: Int,
    val start: String,
    val end: String
)

val Context.periodDataStore by preferencesDataStore(name = "period_config")

@Singleton
class PeriodConfigStore @Inject constructor(
    @ApplicationContext private val context: Context,
    private val gson: Gson
) {
    private val KEY_PERIODS = stringPreferencesKey("period_list_json")

    val defaultPeriods: List<PeriodConfig> = (1..12).map { p ->
        val startMin = (7 * 60) + (p - 1) * 55
        val endMin = startMin + 50
        val startTime = LocalTime.of(startMin / 60, startMin % 60).format(DateTimeFormatter.ofPattern("HH:mm"))
        val endTime = LocalTime.of(endMin / 60, endMin % 60).format(DateTimeFormatter.ofPattern("HH:mm"))
        PeriodConfig(period = p, start = startTime, end = endTime)
    }

    val periodsFlow: Flow<List<PeriodConfig>> = context.periodDataStore.data.map { prefs ->
        val json = prefs[KEY_PERIODS]
        if (json.isNullOrBlank()) {
            defaultPeriods
        } else {
            val type = object : TypeToken<List<PeriodConfig>>() {}.type
            gson.fromJson<List<PeriodConfig>>(json, type) ?: defaultPeriods
        }
    }

    suspend fun savePeriods(list: List<PeriodConfig>) {
        val json = gson.toJson(list)
        context.periodDataStore.edit { prefs ->
            prefs[KEY_PERIODS] = json
        }
    }
}
