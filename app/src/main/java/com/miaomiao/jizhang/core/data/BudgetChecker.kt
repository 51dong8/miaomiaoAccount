package com.miaomiao.jizhang.core.data

import com.miaomiao.jizhang.core.common.DateUtils
import com.miaomiao.jizhang.core.common.TxType
import com.miaomiao.jizhang.core.data.repository.BudgetRepository
import com.miaomiao.jizhang.core.data.repository.TransactionRepository
import java.time.YearMonth
import javax.inject.Inject
import javax.inject.Singleton

/** 预算超支检查结果。 */
data class BudgetCheckResult(
    val month: YearMonth,
    /** 月总预算（分），0 表示未设置 */
    val monthlyBudgetFen: Long,
    /** 当月支出（分） */
    val monthlySpentFen: Long,
    val overMonthly: Boolean,
    /** 超支的分类 id 列表 */
    val overCategoryIds: List<Long>
)

/** 预算超支检查（供每日任务与启动时调用）。 */
@Singleton
class BudgetChecker @Inject constructor(
    private val budgetRepository: BudgetRepository,
    private val transactionRepository: TransactionRepository
) {

    suspend fun check(month: YearMonth): BudgetCheckResult {
        val monthStr = month.toString()
        val budgets = budgetRepository.getByMonth(monthStr)
        val (start, end) = DateUtils.monthRangeMillis(month)
        val expenses = transactionRepository.getBetween(start, end)
            .filter { it.type == TxType.EXPENSE }

        val monthlyBudget = budgets.firstOrNull { it.categoryId == null }?.amount ?: 0L
        val monthlySpent = expenses.sumOf { it.amount }

        val overCategories = budgets
            .filter { it.categoryId != null && it.amount > 0 }
            .map { budget ->
                val spent = expenses.filter { it.categoryId == budget.categoryId }.sumOf { it.amount }
                budget to spent
            }
            .filter { (budget, spent) -> spent > budget.amount }
            .map { (budget, _) -> budget.categoryId!! }

        return BudgetCheckResult(
            month = month,
            monthlyBudgetFen = monthlyBudget,
            monthlySpentFen = monthlySpent,
            overMonthly = monthlyBudget > 0 && monthlySpent > monthlyBudget,
            overCategoryIds = overCategories
        )
    }
}
