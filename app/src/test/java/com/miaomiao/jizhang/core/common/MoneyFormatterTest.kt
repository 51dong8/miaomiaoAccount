package com.miaomiao.jizhang.core.common

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class MoneyFormatterTest {

    @Test
    fun `format 分转元 带千分位与两位小数`() {
        assertEquals("¥0.00", MoneyFormatter.format(0))
        assertEquals("¥1.23", MoneyFormatter.format(123))
        assertEquals("¥1,234.56", MoneyFormatter.format(123456))
        assertEquals("¥12,345.00", MoneyFormatter.format(1234500))
        assertEquals("$1.00", MoneyFormatter.format(100, "$"))
    }

    @Test
    fun `formatPlain 无符号`() {
        assertEquals("1,234.56", MoneyFormatter.formatPlain(123456))
        assertEquals("0.05", MoneyFormatter.formatPlain(5))
    }

    @Test
    fun `parseToFen 正常输入`() {
        assertEquals(1234L, MoneyFormatter.parseToFen("12.34"))
        assertEquals(1200L, MoneyFormatter.parseToFen("12"))
        assertEquals(1200L, MoneyFormatter.parseToFen("12."))
        assertEquals(100L, MoneyFormatter.parseToFen("1"))
        assertEquals(5L, MoneyFormatter.parseToFen("0.05"))
        assertEquals(100L, MoneyFormatter.parseToFen("¥1"))
        assertEquals(100000L, MoneyFormatter.parseToFen("1,000"))
    }

    @Test
    fun `parseToFen 非法与越界`() {
        assertNull(MoneyFormatter.parseToFen(""))
        assertNull(MoneyFormatter.parseToFen("abc"))
        assertNull(MoneyFormatter.parseToFen("-5"))
        assertNull(MoneyFormatter.parseToFen("1.2.3"))
        // 超过两位小数四舍五入
        assertEquals(1235L, MoneyFormatter.parseToFen("12.345"))
    }

    @Test
    fun `roundTrip 保持一致`() {
        val fen = 9876543L
        val input = MoneyFormatter.formatPlain(fen).replace(",", "")
        assertEquals(fen, MoneyFormatter.parseToFen(input))
    }
}
