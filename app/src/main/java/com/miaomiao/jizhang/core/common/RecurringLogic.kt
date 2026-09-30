package com.miaomiao.jizhang.core.common

import com.miaomiao.jizhang.core.data.entity.RecurringRuleEntity
import java.time.LocalDate
import java.time.temporal.TemporalAdjusters

/** 周期频率。 */
object RecurringFrequency {
    const val NONE = "NONE"
    const val DAILY = "DAILY"
    const val WEEKLY = "WEEKLY"
    const val MONTHLY = "MONTHLY"
}

/**
 * 周期账单生成逻辑（纯函数，便于测试）。
 */
object RecurringLogic {

    /**
     * 计算规则在 (fromDate, toDate] 区间内应生成的日期。
     * 只生成 fromDate 之后、toDate 及之前的日期；若规则未到首个周期则不生成。
     */
    fun dueDatesForRange(
        frequency: String,
        dayOfWeek: Int?,
        dayOfMonth: Int?,
        fromDate: LocalDate,
        toDate: LocalDate
    ): List<LocalDate> {
        if (toDate <= fromDate) return emptyList()
        val result = mutableListOf<LocalDate>()
        var cursor = fromDate.plusDays(1)
        while (!cursor.isAfter(toDate)) {
            if (matches(frequency, dayOfWeek, dayOfMonth, cursor)) {
                result.add(cursor)
            }
            cursor = cursor.plusDays(1)
        }
        return result
    }

    private fun matches(frequency: String, dayOfWeek: Int?, dayOfMonth: Int?, date: LocalDate): Boolean = when (frequency) {
        RecurringFrequency.DAILY -> true
        RecurringFrequency.WEEKLY -> date.dayOfWeek.value == (dayOfWeek ?: date.dayOfWeek.value)
        RecurringFrequency.MONTHLY -> {
            val target = (dayOfMonth ?: date.dayOfMonth).coerceIn(1, 31)
            if (target <= date.lengthOfMonth()) {
                date.dayOfMonth == target
            } else {
                // 当月无该日（如 31 日），取当月最后一天
                date == date.with(TemporalAdjusters.lastDayOfMonth())
            }
        }
        else -> false
    }

    /** 周期描述文案，如 "每天"、"每周三"、"每月15日"。 */
    fun describe(frequency: String, dayOfWeek: Int?, dayOfMonth: Int?): String = when (frequency) {
        RecurringFrequency.DAILY -> "每天"
        RecurringFrequency.WEEKLY -> "每周${weekNames[(dayOfWeek ?: 1) - 1]}"
        RecurringFrequency.MONTHLY -> "每月${dayOfMonth ?: 1}日"
        else -> "仅一次"
    }

    private val weekNames = arrayOf("一", "二", "三", "四", "五", "六", "日")

    /** 生成某条规则的账单应落库的时间戳（当天 0 点 + 固定 8 小时，避免跨日歧义）。 */
    fun transactionTime(date: LocalDate): Long =
        DateUtils.dayStartMillis(date) + 8 * 60 * 60 * 1000L
}
