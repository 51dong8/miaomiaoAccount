package com.miaomiao.jizhang.feature.records

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.miaomiao.jizhang.core.common.DateUtils
import com.miaomiao.jizhang.core.common.TxType
import com.miaomiao.jizhang.core.data.entity.AccountEntity
import com.miaomiao.jizhang.core.data.entity.CategoryEntity
import com.miaomiao.jizhang.core.data.entity.TransactionEntity
import com.miaomiao.jizhang.core.data.repository.AccountRepository
import com.miaomiao.jizhang.core.data.repository.CategoryRepository
import com.miaomiao.jizhang.core.data.repository.SettingsRepository
import com.miaomiao.jizhang.core.data.repository.TransactionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import java.time.LocalDate
import javax.inject.Inject

/** 按日分组的一组账单。 */
data class DayGroup(
    val date: LocalDate,
    val transactions: List<TransactionEntity>
) {
    val expense: Long get() = transactions.filter { it.type == TxType.EXPENSE }.sumOf { it.amount }
    val income: Long get() = transactions.filter { it.type == TxType.INCOME }.sumOf { it.amount }
}

@HiltViewModel
class RecordsViewModel @Inject constructor(
    transactionRepository: TransactionRepository,
    categoryRepository: CategoryRepository,
    accountRepository: AccountRepository,
    settingsRepository: SettingsRepository
) : ViewModel() {

    private val allTransactions: StateFlow<List<TransactionEntity>> =
        transactionRepository.observeAll()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val categories: StateFlow<Map<Long, CategoryEntity>> =
        categoryRepository.observeAll()
            .map { it.associateBy { c -> c.id } }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

    val accounts: StateFlow<Map<Long, AccountEntity>> =
        accountRepository.observeAll()
            .map { it.associateBy { a -> a.id } }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

    val currencySymbol: StateFlow<String> = settingsRepository.currencySymbol
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "¥")

    var keyword by mutableStateOf("")
        private set

    /** null = 全部，否则 EXPENSE / INCOME */
    var typeFilter by mutableStateOf<String?>(null)
        private set

    private val keywordFlow = MutableStateFlow("")
    private val typeFilterFlow = MutableStateFlow<String?>(null)

    val filteredTransactions: StateFlow<List<TransactionEntity>> =
        combine(allTransactions, keywordFlow, typeFilterFlow) { list, kw, type ->
            list.filter { t ->
                (type == null || t.type == type) &&
                    (kw.isBlank() || t.note.contains(kw.trim(), ignoreCase = true))
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val grouped: StateFlow<List<DayGroup>> =
        filteredTransactions
            .map { list ->
                list.groupBy { DateUtils.toLocalDate(it.dateTime) }
                    .map { (date, txs) -> DayGroup(date, txs) }
            }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun updateKeyword(value: String) {
        keyword = value
        keywordFlow.value = value
    }

    fun updateTypeFilter(type: String?) {
        typeFilter = type
        typeFilterFlow.value = type
    }
}
