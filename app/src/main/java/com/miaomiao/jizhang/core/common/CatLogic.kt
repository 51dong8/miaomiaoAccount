package com.miaomiao.jizhang.core.common

/**
 * 连续记账天数计算（纯函数，便于测试）。
 * @param currentStreak 当前连续天数
 * @param lastRecordDate 上次记账日期 "yyyy-MM-dd"（空串表示从未记账）
 * @param today 本次记账日期 "yyyy-MM-dd"
 */
fun computeStreak(currentStreak: Int, lastRecordDate: String, today: String): Int = when {
    lastRecordDate == today -> currentStreak
    DateUtils.isYesterday(lastRecordDate, today) -> currentStreak + 1
    else -> 1
}
