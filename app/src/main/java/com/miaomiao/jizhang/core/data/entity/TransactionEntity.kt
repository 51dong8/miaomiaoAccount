package com.miaomiao.jizhang.core.data.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * 账单记录。金额以「分」为单位（Long），避免浮点误差。
 */
@Entity(
    tableName = "transactions",
    indices = [Index("dateTime"), Index("categoryId"), Index("accountId")]
)
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    /** EXPENSE / INCOME，见 [com.miaomiao.jizhang.core.common.TxType] */
    val type: String,
    /** 金额，单位：分 */
    val amount: Long,
    val categoryId: Long,
    val accountId: Long,
    val note: String = "",
    /** 记账时间，epoch millis */
    val dateTime: Long,
    val createdAt: Long = System.currentTimeMillis()
)
