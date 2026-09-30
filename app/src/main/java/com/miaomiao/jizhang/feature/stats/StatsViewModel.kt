package com.miaomiao.jizhang.feature.stats

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.miaomiao.jizhang.core.common.BudgetPeriod
import com.miaomiao.jizhang.core.common.DateUtils
import com.miaomiao.jizhang.core.common.TxType
import com.miaomiao.jizhang.core.data.entity.BudgetEntity
import com.miaomiao.jizhang.core.data.entity.CategoryEntity
import com.miaomiao.jizhang.core.data.entity.TransactionEntity
import com.miaomiao.jizhang.core.data.repository.BudgetRepository
import com.miaomiao.jizhang.core.data.repository.CategoryRepository
import com.miaomiao.jizhang.core.data.repository.SettingsRepository
import com.miaomiao.jizhang.core.data.repository.TransactionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.YearMonth
import javax.inject.Inject

/** 分类支出汇总。 */
data class CategorySum(
    val categoryId: Long,
    val amount: Long
)

/** 月度概览。 */
data class MonthSummary(
    val expense: Long = 0,
    val income: Long = 0,
    val byCategory: List<CategorySum> = emptyList(),
    val dailyExpense: List<Float> = emptyList()
)

@HiltViewModel
class StatsViewModel @Inject constructor(
    private val transactionRepository: TransactionRepository,
    categoryRepository: CategoryRepository,
    private val budgetRepository: BudgetRepository,
    settingsRepository: SettingsRepository
) : ViewModel() {

    private val selectedMonthFlow = MutableStateFlow(YearMonth.now())
    var selectedMonth by mutableStateOf(YearMonth.now())
        private set

    val categories: StateFlow<Map<Long, CategoryEntity>> =
        categoryRepository.observeAll()
            .map { it.associateBy { c -> c.id } }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

    val currencySymbol: StateFlow<String> = settingsRepository.currencySymbol
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "¥")

    val monthStartDay: StateFlow<Int> = settingsRepository.monthStartDay
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 1)

    private val monthTransactions: StateFlow<List<TransactionEntity>> =
        selectedMonthFlow.flatMapLatest { ym ->
            val (start, end) = DateUtils.monthRangeMillis(ym)
            transactionRepository.observeBetween(start, end)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val monthSummary: StateFlow<MonthSummary> =
        monthTransactions
            .map { list ->
                val expenseList = list.filter { it.type == TxType.EXPENSE }
                val incomeList = list.filter { it.type == TxType.INCOME }
                val byCategory = expenseList.groupBy { it.categoryId }
                    .map { (cid, txs) -> CategorySum(cid, txs.sumOf { it.amount }) }
                    .sortedByDescending { it.amount }
                val daysInMonth = selectedMonthFlow.value.lengthOfMonth()
                val daily = FloatArray(daysInMonth) { 0f }
                expenseList.forEach { t ->
                    val day = DateUtils.toLocalDate(t.dateTime).dayOfMonth
                    if (day in 1..daysInMonth) daily[day - 1] += t.amount.toFloat()
                }
                MonthSummary(
                    expense = expenseList.sumOf { it.amount },
                    income = incomeList.sumOf { it.amount },
                    byCategory = byCategory,
                    dailyExpense = daily.toList()
                )
            }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), MonthSummary())

    /** 预算周期（随选中月份与每月起始日计算）。 */
    val budgetPeriod: StateFlow<BudgetPeriod> =
        combine(selectedMonthFlow, monthStartDay) { ym, startDay ->
            val refDay = ym.atDay(minOf(28, ym.lengthOfMonth()))
            DateUtils.budgetPeriod(refDay, startDay)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), DateUtils.budgetPeriod(java.time.LocalDate.now(), 1))

    private val budgetPeriodTransactions: StateFlow<List<TransactionEntity>> =
        combine(selectedMonthFlow, monthStartDay) { ym, startDay ->
            val refDay = ym.atDay(minOf(28, ym.lengthOfMonth()))
            DateUtils.budgetPeriod(refDay, startDay)
        }.flatMapLatest { period ->
            transactionRepository.observeBetween(
                DateUtils.dayStartMillis(period.start),
                DateUtils.dayStartMillis(period.endExclusive)
            )
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    /** 周期内支出（仅支出）。 */
    val periodExpense: StateFlow<Long> =
        budgetPeriodTransactions
            .map { list -> list.filter { it.type == TxType.EXPENSE }.sumOf { it.amount } }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0L)

    /** 周期内按分类支出。 */
    val periodExpenseByCategory: StateFlow<Map<Long, Long>> =
        budgetPeriodTransactions
            .map { list ->
                list.filter { it.type == TxType.EXPENSE }
                    .groupBy { it.categoryId }
                    .mapValues { (_, txs) -> txs.sumOf { it.amount } }
            }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

    val monthlyBudget: StateFlow<BudgetEntity?> =
        combine(selectedMonthFlow, monthStartDay) { ym, startDay ->
            val refDay = ym.atDay(minOf(28, ym.lengthOfMonth()))
            DateUtils.budgetPeriod(refDay, startDay).monthKey
        }.flatMapLatest { monthKey ->
            budgetRepository.observeMonthly(monthKey)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val categoryBudgets: StateFlow<List<BudgetEntity>> =
        combine(selectedMonthFlow, monthStartDay) { ym, startDay ->
            val refDay = ym.atDay(minOf(28, ym.lengthOfMonth()))
            DateUtils.budgetPeriod(refDay, startDay).monthKey
        }.flatMapLatest { monthKey ->
            budgetRepository.observeCategoryBudgets(monthKey)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun prevMonth() {
        selectedMonthFlow.value = selectedMonthFlow.value.minusMonths(1)
        selectedMonth = selectedMonthFlow.value
    }

    fun nextMonth() {
        selectedMonthFlow.value = selectedMonthFlow.value.plusMonths(1)
        selectedMonth = selectedMonthFlow.value
    }

    private fun currentMonthKey(): String {
        val ym = selectedMonthFlow.value
        val refDay = ym.atDay(minOf(28, ym.lengthOfMonth()))
        val startDay = monthStartDay.value
        return DateUtils.budgetPeriod(refDay, startDay).monthKey
    }

    fun setMonthlyBudget(amountFen: Long) {
        viewModelScope.launch {
            budgetRepository.setMonthly(currentMonthKey(), amountFen)
        }
    }

    fun setCategoryBudget(categoryId: Long, amountFen: Long) {
        viewModelScope.launch {
            budgetRepository.setCategoryBudget(currentMonthKey(), categoryId, amountFen)
        }
    }

    fun clearCategoryBudget(categoryId: Long) {
        viewModelScope.launch {
            budgetRepository.removeCategoryBudget(currentMonthKey(), categoryId)
        }
    }
}
