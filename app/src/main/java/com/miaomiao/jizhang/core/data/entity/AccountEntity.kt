package com.miaomiao.jizhang.core.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 账户（现金 / 微信 / 支付宝 / 银行卡等）。归档后不再出现在记账选择列表。
 */
@Entity(tableName = "accounts")
data class AccountEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    /** emoji 图标 */
    val icon: String,
    /** ARGB 色值 */
    val color: Long,
    val isArchived: Boolean = false,
    val isDefault: Boolean = false,
    val sortOrder: Int = 0
)
