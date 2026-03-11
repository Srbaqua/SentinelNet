package com.example.sentinelai.settings

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "sentinel_settings")

enum class ThemeMode { System, Light, Dark }

class SettingsStore(private val context: Context) {
    private object Keys {
        val themeMode = stringPreferencesKey("theme_mode")
        val showScanTips = booleanPreferencesKey("show_scan_tips")
        val quickScanOnOpen = booleanPreferencesKey("quick_scan_on_open")
        val virusTotalApiKey = stringPreferencesKey("virustotal_api_key")
    }

    val themeMode: Flow<ThemeMode> =
        context.dataStore.data.map { prefs -> prefs.themeModeOrDefault() }

    val showScanTips: Flow<Boolean> =
        context.dataStore.data.map { prefs -> prefs[Keys.showScanTips] ?: true }

    val quickScanOnOpen: Flow<Boolean> =
        context.dataStore.data.map { prefs -> prefs[Keys.quickScanOnOpen] ?: false }

    val virusTotalApiKey: Flow<String> =
        context.dataStore.data.map { prefs -> prefs[Keys.virusTotalApiKey] ?: "" }

    suspend fun setThemeMode(mode: ThemeMode) {
        context.dataStore.edit { it[Keys.themeMode] = mode.name.lowercase() }
    }

    suspend fun setShowScanTips(enabled: Boolean) {
        context.dataStore.edit { it[Keys.showScanTips] = enabled }
    }

    suspend fun setQuickScanOnOpen(enabled: Boolean) {
        context.dataStore.edit { it[Keys.quickScanOnOpen] = enabled }
    }

    suspend fun setVirusTotalApiKey(apiKey: String) {
        context.dataStore.edit { it[Keys.virusTotalApiKey] = apiKey.trim() }
    }

    private fun Preferences.themeModeOrDefault(): ThemeMode {
        return when (this[Keys.themeMode]) {
            "light" -> ThemeMode.Light
            "dark" -> ThemeMode.Dark
            else -> ThemeMode.System
        }
    }
}

