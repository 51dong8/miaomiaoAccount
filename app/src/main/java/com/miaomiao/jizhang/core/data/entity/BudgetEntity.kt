package com.miaomiao.jizhang.core.data.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * 预算。[categoryId] 为 null 表示月总预算；否则为分类预算。
 * [month] 为自然月 "yyyy-MM"。
 */
@Entity(
    tableName = "budgets",
    indices = [Index("month"), Index("categoryId")]
)
data class BudgetEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    /** "yyyy-MM"，如 2026-09 */
    val month: String,
    /** null = 月总预算 */
    val categoryId: Long? = null,
    /** 预算金额，单位：分 */
    val amount: Long,
    val createdAt: Long = System.currentTimeMillis()
)
