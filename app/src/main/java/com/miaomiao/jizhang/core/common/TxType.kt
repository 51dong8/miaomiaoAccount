package com.miaomiao.jizhang.core.common

/** 账单类型常量。 */
object TxType {
    const val EXPENSE = "EXPENSE"
    const val INCOME = "INCOME"

    fun label(type: String): String = if (type == EXPENSE) "支出" else "收入"
    fun isExpense(type: String): Boolean = type == EXPENSE
}
