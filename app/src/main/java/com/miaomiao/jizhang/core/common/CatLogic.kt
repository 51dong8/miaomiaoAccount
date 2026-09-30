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

/**
 * 删除账单后的猫咪状态回滚（纯函数，便于测试）。
 * @param lastRemainingDate 删除后当天仍存在其他账单时传当天日期；否则传最近一条账单日期（无则 null）
 * @return 新的 (连续天数, 最后记账日)
 */
fun computeDeleteRollback(
    streakDays: Int,
    lastRecordDate: String,
    deletedDate: String,
    lastRemainingDate: String?
): Pair<Int, String> {
    // 只回滚"最后记账日"被删的情况；删更早的账单不影响连续记录
    if (lastRecordDate == deletedDate) {
        return (streakDays - 1).coerceAtLeast(0) to (lastRemainingDate ?: "")
    }
    return streakDays to lastRecordDate
}
