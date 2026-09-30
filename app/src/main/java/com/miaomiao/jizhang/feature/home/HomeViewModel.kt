package com.miaomiao.jizhang.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.miaomiao.jizhang.core.common.DateUtils
import com.miaomiao.jizhang.core.common.TxType
import com.miaomiao.jizhang.core.data.entity.CatStateEntity
import com.miaomiao.jizhang.core.data.entity.CategoryEntity
import com.miaomiao.jizhang.core.data.entity.TransactionEntity
import com.miaomiao.jizhang.core.data.repository.AccountRepository
import com.miaomiao.jizhang.core.data.repository.CatRepository
import com.miaomiao.jizhang.core.data.repository.CategoryRepository
import com.miaomiao.jizhang.core.data.repository.SettingsRepository
import com.miaomiao.jizhang.core.data.repository.TransactionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    transactionRepository: TransactionRepository,
    catRepository: CatRepository,
    settingsRepository: SettingsRepository,
    categoryRepository: CategoryRepository,
    accountRepository: AccountRepository
) : ViewModel() {

    private val todayRange = DateUtils.dayRangeMillis(LocalDate.now())

    val todayTransactions: StateFlow<List<TransactionEntity>> =
        transactionRepository.observeBetween(todayRange.first, todayRange.second)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    /** (今日支出, 今日收入)，单位分 */
    val todaySummary: StateFlow<Pair<Long, Long>> =
        todayTransactions
            .map { list ->
                val expense = list.filter { it.type == TxType.EXPENSE }.sumOf { it.amount }
                val income = list.filter { it.type == TxType.INCOME }.sumOf { it.amount }
                expense to income
            }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0L to 0L)

    val recentTransactions: StateFlow<List<TransactionEntity>> =
        transactionRepository.observeAll()
            .map { it.take(5) }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val catState: StateFlow<CatStateEntity> = catRepository.observe()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), CatStateEntity())

    val currencySymbol: StateFlow<String> = settingsRepository.currencySymbol
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "¥")

    val categories: StateFlow<Map<Long, CategoryEntity>> =
        categoryRepository.observeAll()
            .map { it.associateBy { c -> c.id } }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

    val accounts: StateFlow<Map<Long, com.miaomiao.jizhang.core.data.entity.AccountEntity>> =
        accountRepository.observeAll()
            .map { it.associateBy { a -> a.id } }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())
}
