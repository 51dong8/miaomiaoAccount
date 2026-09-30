package com.miaomiao.jizhang.core.data.repository

import com.miaomiao.jizhang.core.data.dao.BudgetDao
import com.miaomiao.jizhang.core.data.entity.BudgetEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BudgetRepository @Inject constructor(
    private val dao: BudgetDao
) {
    fun observeMonthly(month: String): Flow<BudgetEntity?> = dao.observeMonthly(month)

    fun observeCategoryBudgets(month: String): Flow<List<BudgetEntity>> =
        dao.observeCategoryBudgets(month)

    suspend fun getByMonth(month: String): List<BudgetEntity> = dao.getByMonth(month)

    suspend fun get(month: String, categoryId: Long): BudgetEntity? = dao.get(month, categoryId)

    suspend fun setMonthly(month: String, amount: Long) =
        dao.upsert(BudgetEntity(month = month, categoryId = null, amount = amount))

    suspend fun setCategoryBudget(month: String, categoryId: Long, amount: Long) =
        dao.upsert(BudgetEntity(month = month, categoryId = categoryId, amount = amount))

    suspend fun removeMonthly(month: String) = dao.deleteMonthly(month)

    suspend fun removeCategoryBudget(month: String, categoryId: Long) =
        dao.deleteCategoryBudget(month, categoryId)
}
