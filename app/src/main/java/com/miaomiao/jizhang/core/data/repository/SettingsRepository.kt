package com.miaomiao.jizhang.core.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SettingsRepository @Inject constructor(
    private val dataStore: DataStore<Preferences>
) {
    val themeMode: Flow<String> = dataStore.data.map { it[KEY_THEME] ?: THEME_SYSTEM }

    val currencySymbol: Flow<String> = dataStore.data.map { it[KEY_CURRENCY] ?: "¥" }

    val monthStartDay: Flow<Int> = dataStore.data.map { it[KEY_MONTH_START] ?: 1 }

    suspend fun setThemeMode(mode: String) {
        dataStore.edit { it[KEY_THEME] = mode }
    }

    suspend fun setCurrencySymbol(symbol: String) {
        dataStore.edit { it[KEY_CURRENCY] = symbol.trim().ifBlank { "¥" } }
    }

    suspend fun setMonthStartDay(day: Int) {
        dataStore.edit { it[KEY_MONTH_START] = day.coerceIn(1, 28) }
    }

    companion object {
        const val THEME_SYSTEM = "system"
        const val THEME_LIGHT = "light"
        const val THEME_DARK = "dark"

        private val KEY_THEME = stringPreferencesKey("theme_mode")
        private val KEY_CURRENCY = stringPreferencesKey("currency_symbol")
        private val KEY_MONTH_START = intPreferencesKey("month_start_day")
    }
}
