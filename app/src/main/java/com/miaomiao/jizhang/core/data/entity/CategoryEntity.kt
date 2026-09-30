package com.miaomiao.jizhang.core.data.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * 分类（支出 / 收入）。[color] 为 ARGB 色值（Long）。
 */
@Entity(
    tableName = "categories",
    indices = [Index("type")]
)
data class CategoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    /** emoji 图标，如 🍜 */
    val icon: String,
    /** ARGB 色值 */
    val color: Long,
    /** EXPENSE / INCOME */
    val type: String,
    /** 内置分类不可删除 */
    val isDefault: Boolean = false,
    val sortOrder: Int = 0
)
