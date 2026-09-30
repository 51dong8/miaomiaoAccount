package com.miaomiao.jizhang.feature.recurring

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.miaomiao.jizhang.core.data.entity.AccountEntity
import com.miaomiao.jizhang.core.data.entity.CategoryEntity
import com.miaomiao.jizhang.core.data.entity.RecurringRuleEntity
import com.miaomiao.jizhang.core.data.repository.AccountRepository
import com.miaomiao.jizhang.core.data.repository.CategoryRepository
import com.miaomiao.jizhang.core.data.repository.RecurringRepository
import com.miaomiao.jizhang.core.data.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RecurringViewModel @Inject constructor(
    private val recurringRepository: RecurringRepository,
    categoryRepository: CategoryRepository,
    accountRepository: AccountRepository,
    settingsRepository: SettingsRepository
) : ViewModel() {

    val currencySymbol: StateFlow<String> = settingsRepository.currencySymbol
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "¥")

    val rules: StateFlow<List<RecurringRuleEntity>> = recurringRepository.observeAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val categories: StateFlow<Map<Long, CategoryEntity>> =
        categoryRepository.observeAll()
            .map { it.associateBy { c -> c.id } }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

    val accounts: StateFlow<Map<Long, AccountEntity>> =
        accountRepository.observeAll()
            .map { it.associateBy { a -> a.id } }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

    fun setActive(rule: RecurringRuleEntity, active: Boolean) {
        viewModelScope.launch { recurringRepository.setActive(rule, active) }
    }

    fun delete(rule: RecurringRuleEntity) {
        viewModelScope.launch { recurringRepository.delete(rule) }
    }
}
