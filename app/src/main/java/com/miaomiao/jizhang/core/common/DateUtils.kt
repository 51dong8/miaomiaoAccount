package com.miaomiao.jizhang.core.common

import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.YearMonth
import java.time.ZoneId
import java.time.format.DateTimeFormatter

/**
 * 日期工具。核心逻辑均为纯函数，便于 JVM 单元测试。
 */
object DateUtils {

    private val dayFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    private val timeFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm")

    fun todayStr(): String = LocalDate.now().format(dayFormatter)

    /** 判断 [prev]（yyyy-MM-dd）是否为 [today] 的前一天。 */
    fun isYesterday(prev: String, today: String): Boolean {
        if (prev.isBlank() || today.isBlank()) return false
        return try {
            LocalDate.parse(prev, dayFormatter).plusDays(1) == LocalDate.parse(today, dayFormatter)
        } catch (e: Exception) {
            false
        }
    }

    fun dayStartMillis(date: LocalDate): Long =
        date.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()

    /** 某天 [start, endExclusive) 的毫秒范围。 */
    fun dayRangeMillis(date: LocalDate): Pair<Long, Long> =
        dayStartMillis(date) to dayStartMillis(date.plusDays(1))

    /** 自然月 [start, endExclusive) 的毫秒范围。 */
    fun monthRangeMillis(yearMonth: YearMonth): Pair<Long, Long> =
        dayStartMillis(yearMonth.atDay(1)) to dayStartMillis(yearMonth.plusMonths(1).atDay(1))

    fun toLocalDate(dateTime: Long): LocalDate =
        Instant.ofEpochMilli(dateTime).atZone(ZoneId.systemDefault()).toLocalDate()

    /** 列表分组日期头：今天 / 昨天 / 9月28日 周一 / 2025年12月31日 周三 */
    fun formatDayHeader(dateTime: Long): String {
        val date = toLocalDate(dateTime)
        val today = LocalDate.now()
        val week = arrayOf("周一", "周二", "周三", "周四", "周五", "周六", "周日")
        val weekText = week[(date.dayOfWeek.value + 6) % 7]
        val md = "${date.monthValue}月${date.dayOfMonth}日"
        return when (date) {
            today -> "今天 $md $weekText"
            today.minusDays(1) -> "昨天 $md $weekText"
            else -> {
                if (date.year == today.year) "$md $weekText" else "${date.year}年$md $weekText"
            }
        }
    }

    fun formatMonth(yearMonth: YearMonth): String = "${yearMonth.year}年${yearMonth.monthValue}月"

    fun formatTime(dateTime: Long): String =
        LocalDateTime.ofInstant(Instant.ofEpochMilli(dateTime), ZoneId.systemDefault())
            .format(timeFormatter)

    /**
     * 预算周期：按「每月起始日」计算。
     * 例如起始日 15，今天 9/30 → 周期 9/15 - 10/14；今天 9/10 → 8/15 - 9/14。
     */
    fun budgetPeriod(now: LocalDate, startDay: Int): BudgetPeriod {
        val sd = startDay.coerceIn(1, 28)
        val currentMonth = YearMonth.from(now)
        val start = if (now.dayOfMonth >= sd) {
            currentMonth.atDay(sd)
        } else {
            currentMonth.minusMonths(1).atDay(sd)
        }
        val endExclusive = start.plusMonths(1)
        val monthKey = YearMonth.from(start).format(DateTimeFormatter.ofPattern("yyyy-MM"))
        return BudgetPeriod(start = start, endExclusive = endExclusive, monthKey = monthKey)
    }
}

data class BudgetPeriod(
    val start: LocalDate,
    val endExclusive: LocalDate,
    /** 预算记录落库的月份 key（起始日所在自然月 yyyy-MM） */
    val monthKey: String
) {
    val label: String
        get() {
            val last = endExclusive.minusDays(1)
            return "${start.monthValue}月${start.dayOfMonth}日 - ${last.monthValue}月${last.dayOfMonth}日"
        }
}
