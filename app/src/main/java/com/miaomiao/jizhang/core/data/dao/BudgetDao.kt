package com.miaomiao.jizhang.core.data.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.miaomiao.jizhang.core.data.entity.BudgetEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BudgetDao {

    @Upsert
    suspend fun upsert(budget: BudgetEntity): Long

    @Query("SELECT * FROM budgets WHERE month = :month AND categoryId IS NULL")
    fun observeMonthly(month: String): Flow<BudgetEntity?>

    @Query("SELECT * FROM budgets WHERE month = :month AND categoryId IS NOT NULL")
    fun observeCategoryBudgets(month: String): Flow<List<BudgetEntity>>

    @Query("SELECT * FROM budgets WHERE month = :month")
    suspend fun getByMonth(month: String): List<BudgetEntity>

    @Query("SELECT * FROM budgets")
    suspend fun getAll(): List<BudgetEntity>

    @Query("SELECT * FROM budgets WHERE month = :month AND categoryId = :categoryId")
    suspend fun get(month: String, categoryId: Long): BudgetEntity?

    @Query("DELETE FROM budgets WHERE month = :month AND categoryId IS NULL")
    suspend fun deleteMonthly(month: String)

    @Query("DELETE FROM budgets WHERE month = :month AND categoryId = :categoryId")
    suspend fun deleteCategoryBudget(month: String, categoryId: Long)

    @Query("DELETE FROM budgets")
    suspend fun deleteAll()
}
