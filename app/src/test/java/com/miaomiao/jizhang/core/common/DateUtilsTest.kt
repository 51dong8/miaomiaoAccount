package com.miaomiao.jizhang.core.common

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate
import java.time.YearMonth

class DateUtilsTest {

    @Test
    fun `isYesterday 判断前一天`() {
        assertTrue(DateUtils.isYesterday("2026-09-29", "2026-09-30"))
        assertFalse(DateUtils.isYesterday("2026-09-28", "2026-09-30"))
        assertFalse(DateUtils.isYesterday("", "2026-09-30"))
        assertFalse(DateUtils.isYesterday("bad-date", "2026-09-30"))
        // 跨月
        assertTrue(DateUtils.isYesterday("2026-08-31", "2026-09-01"))
        // 跨年
        assertTrue(DateUtils.isYesterday("2025-12-31", "2026-01-01"))
    }

    @Test
    fun `computeStreak 规则`() {
        // 从未记账
        assertEquals(1, computeStreak(0, "", "2026-09-30"))
        // 同日重复记账不增加
        assertEquals(3, computeStreak(3, "2026-09-30", "2026-09-30"))
        // 昨天记账 → +1
        assertEquals(4, computeStreak(3, "2026-09-29", "2026-09-30"))
        // 中断 → 重置 1
        assertEquals(1, computeStreak(5, "2026-09-20", "2026-09-30"))
    }

    @Test
    fun `budgetPeriod 起始日 1 即自然月`() {
        val p = DateUtils.budgetPeriod(LocalDate.of(2026, 9, 30), 1)
        assertEquals(LocalDate.of(2026, 9, 1), p.start)
        assertEquals(LocalDate.of(2026, 10, 1), p.endExclusive)
        assertEquals("2026-09", p.monthKey)
    }

    @Test
    fun `budgetPeriod 起始日 15 跨月周期`() {
        // 9/30 >= 15 → 周期 9/15 - 10/14
        val p1 = DateUtils.budgetPeriod(LocalDate.of(2026, 9, 30), 15)
        assertEquals(LocalDate.of(2026, 9, 15), p1.start)
        assertEquals(LocalDate.of(2026, 10, 15), p1.endExclusive)
        assertEquals("2026-09", p1.monthKey)

        // 9/10 < 15 → 周期 8/15 - 9/14
        val p2 = DateUtils.budgetPeriod(LocalDate.of(2026, 9, 10), 15)
        assertEquals(LocalDate.of(2026, 8, 15), p2.start)
        assertEquals(LocalDate.of(2026, 9, 15), p2.endExclusive)
        assertEquals("2026-08", p2.monthKey)
    }

    @Test
    fun `budgetPeriod 起始日 28`() {
        val p = DateUtils.budgetPeriod(LocalDate.of(2026, 9, 30), 28)
        assertEquals(LocalDate.of(2026, 9, 28), p.start)
        assertEquals(LocalDate.of(2026, 10, 28), p.endExclusive)
    }

    @Test
    fun `budgetPeriod 起始日越界被限制`() {
        val p = DateUtils.budgetPeriod(LocalDate.of(2026, 9, 30), 99)
        assertEquals(LocalDate.of(2026, 9, 28), p.start)
    }

    @Test
    fun `monthRangeMillis 覆盖当月首日与次月首日`() {
        val (start, end) = DateUtils.monthRangeMillis(YearMonth.of(2026, 9))
        val firstDay = DateUtils.dayStartMillis(LocalDate.of(2026, 9, 1))
        val nextFirstDay = DateUtils.dayStartMillis(LocalDate.of(2026, 10, 1))
        assertEquals(firstDay, start)
        assertEquals(nextFirstDay, end)
        assertTrue(end > start)
    }
}
