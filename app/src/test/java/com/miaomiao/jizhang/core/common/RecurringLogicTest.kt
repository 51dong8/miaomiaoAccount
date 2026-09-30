package com.miaomiao.jizhang.core.common

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class RecurringLogicTest {

    @Test
    fun `每天 - 只生成 fromDate 之后的日期`() {
        val dates = RecurringLogic.dueDatesForRange(
            RecurringFrequency.DAILY, null, null,
            LocalDate.of(2026, 9, 1), LocalDate.of(2026, 9, 3)
        )
        assertEquals(listOf(LocalDate.of(2026, 9, 2), LocalDate.of(2026, 9, 3)), dates)
    }

    @Test
    fun `每天 - fromDate 等于 toDate 不生成`() {
        val dates = RecurringLogic.dueDatesForRange(
            RecurringFrequency.DAILY, null, null,
            LocalDate.of(2026, 9, 3), LocalDate.of(2026, 9, 3)
        )
        assertTrue(dates.isEmpty())
    }

    @Test
    fun `每周 - 只落在指定星期几`() {
        // 2026-09-01 是周二(2)，周三为 2026-09-02
        val dates = RecurringLogic.dueDatesForRange(
            RecurringFrequency.WEEKLY, 3, null,
            LocalDate.of(2026, 9, 1), LocalDate.of(2026, 9, 15)
        )
        assertEquals(listOf(LocalDate.of(2026, 9, 2), LocalDate.of(2026, 9, 9)), dates)
        dates.forEach { assertEquals(3, it.dayOfWeek.value) }
    }

    @Test
    fun `每月 - 当月无该日取月末（2月28日）`() {
        val dates = RecurringLogic.dueDatesForRange(
            RecurringFrequency.MONTHLY, null, 31,
            LocalDate.of(2026, 2, 1), LocalDate.of(2026, 4, 30)
        )
        assertEquals(
            listOf(LocalDate.of(2026, 2, 28), LocalDate.of(2026, 3, 31), LocalDate.of(2026, 4, 30)),
            dates
        )
    }

    @Test
    fun `每月 - 正常日号`() {
        val dates = RecurringLogic.dueDatesForRange(
            RecurringFrequency.MONTHLY, null, 15,
            LocalDate.of(2026, 9, 1), LocalDate.of(2026, 10, 1)
        )
        assertEquals(listOf(LocalDate.of(2026, 9, 15)), dates)
    }

    @Test
    fun `描述文案`() {
        assertEquals("每天", RecurringLogic.describe(RecurringFrequency.DAILY, null, null))
        assertEquals("每周三", RecurringLogic.describe(RecurringFrequency.WEEKLY, 3, null))
        assertEquals("每周日", RecurringLogic.describe(RecurringFrequency.WEEKLY, 7, null))
        assertEquals("每月15日", RecurringLogic.describe(RecurringFrequency.MONTHLY, null, 15))
        assertEquals("仅一次", RecurringLogic.describe("NONE", null, null))
    }

    @Test
    fun `transactionTime 为当天 0 点 + 8 小时`() {
        val date = LocalDate.of(2026, 9, 30)
        assertEquals(
            DateUtils.dayStartMillis(date) + 8 * 60 * 60 * 1000L,
            RecurringLogic.transactionTime(date)
        )
    }
}
