package com.miaomiao.jizhang.core.common

import org.junit.Assert.assertEquals
import org.junit.Test

class CatLogicTest {

    @Test
    fun `删除最后记账日 - 连续天数回退并更新最后日期`() {
        val (streak, last) = computeDeleteRollback(
            streakDays = 5,
            lastRecordDate = "2026-09-30",
            deletedDate = "2026-09-30",
            lastRemainingDate = "2026-09-29"
        )
        assertEquals(4, streak)
        assertEquals("2026-09-29", last)
    }

    @Test
    fun `删除最后记账日且无其他账单 - 最后日期清空`() {
        val (streak, last) = computeDeleteRollback(
            streakDays = 3,
            lastRecordDate = "2026-09-30",
            deletedDate = "2026-09-30",
            lastRemainingDate = null
        )
        assertEquals(2, streak)
        assertEquals("", last)
    }

    @Test
    fun `连续天数不低于 0`() {
        val (streak, _) = computeDeleteRollback(
            streakDays = 1,
            lastRecordDate = "2026-09-30",
            deletedDate = "2026-09-30",
            lastRemainingDate = null
        )
        assertEquals(0, streak)
    }

    @Test
    fun `删除非最后记账日 - 连续记录不受影响`() {
        val (streak, last) = computeDeleteRollback(
            streakDays = 7,
            lastRecordDate = "2026-09-30",
            deletedDate = "2026-09-25",
            lastRemainingDate = "2026-09-25"
        )
        assertEquals(7, streak)
        assertEquals("2026-09-30", last)
    }
}
