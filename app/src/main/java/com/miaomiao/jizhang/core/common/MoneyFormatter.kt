package com.miaomiao.jizhang.core.common

import java.math.BigDecimal
import java.math.RoundingMode
import java.text.NumberFormat
import java.util.Locale

/**
 * 金额工具：内部一律以「分」（Long）存储，展示时转元。
 */
object MoneyFormatter {

    private val numberFormat = NumberFormat.getNumberInstance(Locale.CHINA).apply {
        minimumFractionDigits = 2
        maximumFractionDigits = 2
        isGroupingUsed = true
    }

    /** 分 → "¥1,234.56" */
    fun format(amountFen: Long, symbol: String = "¥"): String =
        symbol + numberFormat.format(BigDecimal(amountFen).movePointLeft(2))

    /** 分 → "1,234.56"（无符号） */
    fun formatPlain(amountFen: Long): String =
        numberFormat.format(BigDecimal(amountFen).movePointLeft(2))

    /** 用户输入字符串（如 "12.34"、"1200"）→ 分。非法或负数返回 null。 */
    fun parseToFen(input: String): Long? {
        val cleaned = input.replace(",", "").replace("¥", "").trim()
        if (cleaned.isBlank()) return null
        val bd = cleaned.toBigDecimalOrNull() ?: return null
        if (bd < BigDecimal.ZERO) return null
        // 超过两位小数的部分四舍五入
        return bd.movePointRight(2).setScale(0, RoundingMode.HALF_UP).toLong()
    }
}
