package com.uniplan.app.data.local.prefs

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

val Context.settingsDataStore by preferencesDataStore(name = "settings")

@Singleton
class SettingsDataStore @Inject constructor(
    @ApplicationContext private val context: Context,
    private val gson: Gson
) {
    companion object {
        val KEY_ONBOARDING_DONE = booleanPreferencesKey("onboarding_done")
        val KEY_THEME_MODE = stringPreferencesKey("theme_mode")
        val KEY_LANGUAGE = stringPreferencesKey("language")
        val KEY_REM_CLASS_ENABLED = booleanPreferencesKey("rem_class_enabled")
        val KEY_REM_CLASS_OFFSET = intPreferencesKey("rem_class_offset_min")
        val KEY_REM_TASK_ENABLED = booleanPreferencesKey("rem_task_enabled")
        val KEY_REM_TASK_OFFSETS = stringPreferencesKey("rem_task_offsets_min")
        val KEY_REM_EXAM_ENABLED = booleanPreferencesKey("rem_exam_enabled")
        val KEY_REM_EXAM_OFFSETS = stringPreferencesKey("rem_exam_offsets_min")
        val KEY_DND_ENABLED = booleanPreferencesKey("dnd_enabled")
        val KEY_DND_START = stringPreferencesKey("dnd_start")
        val KEY_DND_END = stringPreferencesKey("dnd_end")
        val KEY_JAR_ENABLED = booleanPreferencesKey("jar_enabled")
        val KEY_JAR_DEFAULT_GOAL = intPreferencesKey("jar_default_goal")
        val KEY_JAR_HAPTIC = booleanPreferencesKey("jar_haptic")
        val KEY_JAR_SOUND = booleanPreferencesKey("jar_sound")
        val KEY_SCHEDULE_VIEW_MODE = stringPreferencesKey("schedule_view_mode")
    }

    val onboardingDone: Flow<Boolean> = context.settingsDataStore.data.map { it[KEY_ONBOARDING_DONE] ?: false }
    val themeMode: Flow<String> = context.settingsDataStore.data.map { it[KEY_THEME_MODE] ?: "SYSTEM" }
    val language: Flow<String> = context.settingsDataStore.data.map { it[KEY_LANGUAGE] ?: "SYSTEM" }

    val remClassEnabled: Flow<Boolean> = context.settingsDataStore.data.map { it[KEY_REM_CLASS_ENABLED] ?: true }
    val remClassOffsetMin: Flow<Int> = context.settingsDataStore.data.map { it[KEY_REM_CLASS_OFFSET] ?: 30 }

    val remTaskEnabled: Flow<Boolean> = context.settingsDataStore.data.map { it[KEY_REM_TASK_ENABLED] ?: true }
    val remTaskOffsetsMin: Flow<List<Int>> = context.settingsDataStore.data.map { prefs ->
        val json = prefs[KEY_REM_TASK_OFFSETS]
        if (json.isNullOrBlank()) listOf(1440, 60) // 1 day, 1 hour
        else {
            val type = object : TypeToken<List<Int>>() {}.type
            gson.fromJson(json, type) ?: listOf(1440, 60)
        }
    }

    val remExamEnabled: Flow<Boolean> = context.settingsDataStore.data.map { it[KEY_REM_EXAM_ENABLED] ?: true }
    val remExamOffsetsMin: Flow<List<Int>> = context.settingsDataStore.data.map { prefs ->
        val json = prefs[KEY_REM_EXAM_OFFSETS]
        if (json.isNullOrBlank()) listOf(4320, 1440, 60) // 3 days, 1 day, 1 hour
        else {
            val type = object : TypeToken<List<Int>>() {}.type
            gson.fromJson(json, type) ?: listOf(4320, 1440, 60)
        }
    }

    val dndEnabled: Flow<Boolean> = context.settingsDataStore.data.map { it[KEY_DND_ENABLED] ?: false }
    val dndStart: Flow<String> = context.settingsDataStore.data.map { it[KEY_DND_START] ?: "23:00" }
    val dndEnd: Flow<String> = context.settingsDataStore.data.map { it[KEY_DND_END] ?: "06:00" }

    val jarEnabled: Flow<Boolean> = context.settingsDataStore.data.map { it[KEY_JAR_ENABLED] ?: true }
    val jarDefaultGoal: Flow<Int> = context.settingsDataStore.data.map { it[KEY_JAR_DEFAULT_GOAL] ?: 30 }
    val jarHaptic: Flow<Boolean> = context.settingsDataStore.data.map { it[KEY_JAR_HAPTIC] ?: true }
    val jarSound: Flow<Boolean> = context.settingsDataStore.data.map { it[KEY_JAR_SOUND] ?: false }

    val scheduleViewMode: Flow<String> = context.settingsDataStore.data.map { it[KEY_SCHEDULE_VIEW_MODE] ?: "WEEK" }

    suspend fun setOnboardingDone(done: Boolean) {
        context.settingsDataStore.edit { it[KEY_ONBOARDING_DONE] = done }
    }

    suspend fun setThemeMode(mode: String) {
        context.settingsDataStore.edit { it[KEY_THEME_MODE] = mode }
    }

    suspend fun setLanguage(lang: String) {
        context.settingsDataStore.edit { it[KEY_LANGUAGE] = lang }
    }

    suspend fun setRemClassEnabled(enabled: Boolean) {
        context.settingsDataStore.edit { it[KEY_REM_CLASS_ENABLED] = enabled }
    }

    suspend fun setRemClassOffset(min: Int) {
        context.settingsDataStore.edit { it[KEY_REM_CLASS_OFFSET] = min }
    }

    suspend fun setRemTaskEnabled(enabled: Boolean) {
        context.settingsDataStore.edit { it[KEY_REM_TASK_ENABLED] = enabled }
    }

    suspend fun setRemTaskOffsets(offsets: List<Int>) {
        context.settingsDataStore.edit { it[KEY_REM_TASK_OFFSETS] = gson.toJson(offsets) }
    }

    suspend fun setRemExamEnabled(enabled: Boolean) {
        context.settingsDataStore.edit { it[KEY_REM_EXAM_ENABLED] = enabled }
    }

    suspend fun setRemExamOffsets(offsets: List<Int>) {
        context.settingsDataStore.edit { it[KEY_REM_EXAM_OFFSETS] = gson.toJson(offsets) }
    }

    suspend fun setDnd(enabled: Boolean, start: String, end: String) {
        context.settingsDataStore.edit {
            it[KEY_DND_ENABLED] = enabled
            it[KEY_DND_START] = start
            it[KEY_DND_END] = end
        }
    }

    suspend fun setJarSettings(enabled: Boolean, goal: Int, haptic: Boolean, sound: Boolean) {
        context.settingsDataStore.edit {
            it[KEY_JAR_ENABLED] = enabled
            it[KEY_JAR_DEFAULT_GOAL] = goal
            it[KEY_JAR_HAPTIC] = haptic
            it[KEY_JAR_SOUND] = sound
        }
    }

    suspend fun setScheduleViewMode(mode: String) {
        context.settingsDataStore.edit { it[KEY_SCHEDULE_VIEW_MODE] = mode }
    }
}
