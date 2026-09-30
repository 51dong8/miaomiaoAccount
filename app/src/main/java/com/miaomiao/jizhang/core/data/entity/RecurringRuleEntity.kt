package com.miaomiao.jizhang.core.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/** 周期账单规则。 */
@Entity(tableName = "recurring_rules")
data class RecurringRuleEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    /** EXPENSE / INCOME */
    val type: String,
    /** 金额，分 */
    val amount: Long,
    val categoryId: Long,
    val accountId: Long,
    val note: String,
    /** DAILY / WEEKLY / MONTHLY */
    val frequency: String,
    /** WEEKLY 时使用：1(周一)..7(周日) */
    val dayOfWeek: Int? = null,
    /** MONTHLY 时使用：1..31（当月无此日则取月末最后一天） */
    val dayOfMonth: Int? = null,
    /** 规则创建日期 "yyyy-MM-dd" */
    val startDate: String,
    /** 已生成账单的最后日期 "yyyy-MM-dd"，下次从次日开始 */
    val lastGeneratedDate: String,
    val active: Boolean = true,
    val createdAt: Long
)
