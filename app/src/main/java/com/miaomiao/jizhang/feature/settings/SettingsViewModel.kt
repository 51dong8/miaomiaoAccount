package com.miaomiao.jizhang.feature.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.miaomiao.jizhang.core.data.BackupManager
import com.miaomiao.jizhang.core.data.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val backupManager: BackupManager
) : ViewModel() {

    val themeMode: StateFlow<String> = settingsRepository.themeMode
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SettingsRepository.THEME_SYSTEM)

    val currencySymbol: StateFlow<String> = settingsRepository.currencySymbol
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "¥")

    val monthStartDay: StateFlow<Int> = settingsRepository.monthStartDay
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 1)

    fun setThemeMode(mode: String) {
        viewModelScope.launch { settingsRepository.setThemeMode(mode) }
    }

    fun setCurrencySymbol(symbol: String) {
        viewModelScope.launch { settingsRepository.setCurrencySymbol(symbol) }
    }

    fun setMonthStartDay(day: Int) {
        viewModelScope.launch { settingsRepository.setMonthStartDay(day) }
    }

    suspend fun exportCsv(): String = backupManager.exportCsv()

    suspend fun exportJson(): String = backupManager.exportJson()

    suspend fun importJson(json: String): Boolean = backupManager.importJson(json)
}
