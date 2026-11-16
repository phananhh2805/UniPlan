package com.uniplan.app.data.local.prefs

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.google.gson.Gson
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "uniplan_settings")

class SettingsDataStore(
    private val context: Context,
    private val gson: Gson
) {
    private val ONBOARDING_DONE = booleanPreferencesKey("onboarding_done")
    private val THEME_MODE = stringPreferencesKey("theme_mode")
    private val LANGUAGE = stringPreferencesKey("language")
    private val SCHEDULE_MODE = stringPreferencesKey("schedule_mode")

    private val REM_CLASS_ENABLED = booleanPreferencesKey("rem_class_enabled")
    private val REM_CLASS_OFFSET = intPreferencesKey("rem_class_offset_min")

    private val REM_TASK_ENABLED = booleanPreferencesKey("rem_task_enabled")
    private val REM_TASK_OFFSETS = stringPreferencesKey("rem_task_offsets_min")

    private val REM_EXAM_ENABLED = booleanPreferencesKey("rem_exam_enabled")
    private val REM_EXAM_OFFSETS = stringPreferencesKey("rem_exam_offsets_min")

    private val DND_ENABLED = booleanPreferencesKey("dnd_enabled")
    private val DND_START = stringPreferencesKey("dnd_start")
    private val DND_END = stringPreferencesKey("dnd_end")

    private val JAR_ENABLED = booleanPreferencesKey("jar_enabled")
    private val JAR_DEFAULT_GOAL = intPreferencesKey("jar_default_goal")
    private val JAR_HAPTIC = booleanPreferencesKey("jar_haptic")
    private val JAR_SOUND = booleanPreferencesKey("jar_sound")

    val isOnboardingDone: Flow<Boolean> = context.dataStore.data.map { it[ONBOARDING_DONE] ?: false }
    val themeMode: Flow<String> = context.dataStore.data.map { it[THEME_MODE] ?: "SYSTEM" }
    val language: Flow<String> = context.dataStore.data.map { it[LANGUAGE] ?: "SYSTEM" }
    val scheduleMode: Flow<String> = context.dataStore.data.map { it[SCHEDULE_MODE] ?: "WEEK" }

    val remClassEnabled: Flow<Boolean> = context.dataStore.data.map { it[REM_CLASS_ENABLED] ?: true }
    val remClassOffsetMin: Flow<Int> = context.dataStore.data.map { it[REM_CLASS_OFFSET] ?: 30 }

    val remTaskEnabled: Flow<Boolean> = context.dataStore.data.map { it[REM_TASK_ENABLED] ?: true }
    val remTaskOffsetsMin: Flow<List<Int>> = context.dataStore.data.map { prefs ->
        val json = prefs[REM_TASK_OFFSETS]
        if (json.isNullOrEmpty()) listOf(1440, 60)
        else gson.fromJson(json, Array<Int>::class.java).toList()
    }

    val remExamEnabled: Flow<Boolean> = context.dataStore.data.map { it[REM_EXAM_ENABLED] ?: true }
    val remExamOffsetsMin: Flow<List<Int>> = context.dataStore.data.map { prefs ->
        val json = prefs[REM_EXAM_OFFSETS]
        if (json.isNullOrEmpty()) listOf(4320, 1440, 60)
        else gson.fromJson(json, Array<Int>::class.java).toList()
    }

    val dndEnabled: Flow<Boolean> = context.dataStore.data.map { it[DND_ENABLED] ?: false }
    val dndStart: Flow<String> = context.dataStore.data.map { it[DND_START] ?: "23:00" }
    val dndEnd: Flow<String> = context.dataStore.data.map { it[DND_END] ?: "06:00" }

    val jarEnabled: Flow<Boolean> = context.dataStore.data.map { it[JAR_ENABLED] ?: true }
    val jarDefaultGoal: Flow<Int> = context.dataStore.data.map { it[JAR_DEFAULT_GOAL] ?: 30 }
    val jarHaptic: Flow<Boolean> = context.dataStore.data.map { it[JAR_HAPTIC] ?: true }
    val jarSound: Flow<Boolean> = context.dataStore.data.map { it[JAR_SOUND] ?: false }

    suspend fun setOnboardingDone(done: Boolean) {
        context.dataStore.edit { it[ONBOARDING_DONE] = done }
    }

    suspend fun setThemeMode(mode: String) {
        context.dataStore.edit { it[THEME_MODE] = mode }
    }

    suspend fun setLanguage(lang: String) {
        context.dataStore.edit { it[LANGUAGE] = lang }
    }

    suspend fun setScheduleMode(mode: String) {
        context.dataStore.edit { it[SCHEDULE_MODE] = mode }
    }

    suspend fun setRemClassEnabled(enabled: Boolean) {
        context.dataStore.edit { it[REM_CLASS_ENABLED] = enabled }
    }

    suspend fun setRemClassOffset(min: Int) {
        context.dataStore.edit { it[REM_CLASS_OFFSET] = min }
    }

    suspend fun setRemTaskEnabled(enabled: Boolean) {
        context.dataStore.edit { it[REM_TASK_ENABLED] = enabled }
    }

    suspend fun setRemTaskOffsets(offsets: List<Int>) {
        context.dataStore.edit { it[REM_TASK_OFFSETS] = gson.toJson(offsets) }
    }

    suspend fun setRemExamEnabled(enabled: Boolean) {
        context.dataStore.edit { it[REM_EXAM_ENABLED] = enabled }
    }

    suspend fun setRemExamOffsets(offsets: List<Int>) {
        context.dataStore.edit { it[REM_EXAM_OFFSETS] = gson.toJson(offsets) }
    }

    suspend fun setDnd(enabled: Boolean, start: String, end: String) {
        context.dataStore.edit {
            it[DND_ENABLED] = enabled
            it[DND_START] = start
            it[DND_END] = end
        }
    }

    suspend fun setJarSettings(enabled: Boolean, defaultGoal: Int, haptic: Boolean, sound: Boolean) {
        context.dataStore.edit {
            it[JAR_ENABLED] = enabled
            it[JAR_DEFAULT_GOAL] = defaultGoal
            it[JAR_HAPTIC] = haptic
            it[JAR_SOUND] = sound
        }
    }
}
