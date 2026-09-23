package com.bedanta.dotcalc

import com.bedanta.dotcalc.logic.CalculatorEngine
import org.junit.Assert.assertEquals
import org.junit.Test

class CalculatorEngineTest {

    private val engine = CalculatorEngine()

    @Test
    fun testBasicAddition() {
        assertEquals("5", engine.calculate("2+3"))
    }

    @Test
    fun testPrecedence() {
        assertEquals("14", engine.calculate("2+3×4"))
    }

    @Test
    fun testDivisionByZero() {
        assertEquals("Error", engine.calculate("5÷0"))
    }

    @Test
    fun testDecimalHandling() {
        assertEquals("0.3", engine.calculate("0.1+0.2"))
    }

    @Test
    fun testPercentage() {
        assertEquals("0.5", engine.calculate("50%"))
        assertEquals("1.05", engine.calculate("1+5%"))
        assertEquals("105", engine.calculate("100+5%"))
        assertEquals("180", engine.calculate("200−10%"))
    }

    @Test
    fun testUnaryMinus() {
        assertEquals("-5", engine.calculate("−5"))
        assertEquals("2", engine.calculate("−3+5"))
        assertEquals("25", engine.calculate("−5×−5"))
        assertEquals("-15", engine.calculate("5×−3"))
    }

    @Test
    fun testParentheses() {
        assertEquals("14", engine.calculate("2×(3+4)"))
        assertEquals("2", engine.calculate("(−5)+7"))
    }

    @Test
    fun testImplicitMultiplication() {
        assertEquals("54", engine.calculate("9(6)"))
        assertEquals("10", engine.calculate("(3+2)(1+1)"))
        assertEquals("5", engine.calculate("50%(10)")) // 0.5 * 10
    }
}