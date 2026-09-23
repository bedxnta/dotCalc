package com.bedanta.dotcalc.logic

import org.junit.Assert.assertEquals
import org.junit.Test
import java.math.BigDecimal

class SettingsManagerTest {

    @Test
    fun testFormattingMatrix() {
        val cases = listOf(
            Triple(BigDecimal("1234567"), "1234567", "Grouping OFF, DOT"),
            Triple(BigDecimal("1234567.89"), "1234567.89", "Grouping OFF, DOT"),
            Triple(BigDecimal("-1234567.89"), "−1234567.89", "Grouping OFF, DOT")
        )

        val settingsBase = CalculatorSettings(
            groupingEnabled = false,
            decimalSeparator = DecimalSeparator.DOT
        )

        for ((value, expected, desc) in cases) {
            assertEquals("$desc failed", expected, SettingsManager.formatDecimal(value, settingsBase))
        }

        // ON + INTERNATIONAL + DOT
        val settingsIntlDot = CalculatorSettings(
            groupingEnabled = true,
            groupingSeparator = GroupingSeparator.INTERNATIONAL,
            decimalSeparator = DecimalSeparator.DOT
        )
        assertEquals("1,234,567", SettingsManager.formatDecimal(BigDecimal("1234567"), settingsIntlDot))
        assertEquals("1,234,567.89", SettingsManager.formatDecimal(BigDecimal("1234567.89"), settingsIntlDot))
        assertEquals("−1,234,567.89", SettingsManager.formatDecimal(BigDecimal("-1234567.89"), settingsIntlDot))

        // ON + INDIAN + DOT
        val settingsIndianDot = CalculatorSettings(
            groupingEnabled = true,
            groupingSeparator = GroupingSeparator.INDIAN,
            decimalSeparator = DecimalSeparator.DOT
        )
        assertEquals("12,34,567", SettingsManager.formatDecimal(BigDecimal("1234567"), settingsIndianDot))
        assertEquals("12,34,567.89", SettingsManager.formatDecimal(BigDecimal("1234567.89"), settingsIndianDot))
        assertEquals("1,23,45,67,890.12", SettingsManager.formatDecimal(BigDecimal("1234567890.12"), settingsIndianDot))

        // OFF + COMMA
        val settingsOffComma = CalculatorSettings(
            groupingEnabled = false,
            decimalSeparator = DecimalSeparator.COMMA
        )
        assertEquals("1234567,89", SettingsManager.formatDecimal(BigDecimal("1234567.89"), settingsOffComma))

        // ON + INTERNATIONAL + COMMA
        val settingsIntlComma = CalculatorSettings(
            groupingEnabled = true,
            groupingSeparator = GroupingSeparator.INTERNATIONAL,
            decimalSeparator = DecimalSeparator.COMMA
        )
        assertEquals("1.234.567,89", SettingsManager.formatDecimal(BigDecimal("1234567.89"), settingsIntlComma))

        // ON + INDIAN + COMMA
        val settingsIndianComma = CalculatorSettings(
            groupingEnabled = true,
            groupingSeparator = GroupingSeparator.INDIAN,
            decimalSeparator = DecimalSeparator.COMMA
        )
        assertEquals("12.34.567,89", SettingsManager.formatDecimal(BigDecimal("1234567.89"), settingsIndianComma))
    }

    @Test
    fun testNormalizeInput() {
        val settingsIntlDot = CalculatorSettings(decimalSeparator = DecimalSeparator.DOT)
        assertEquals("1234.56", SettingsManager.normalizeInput("1,234.56", settingsIntlDot))

        val settingsIntlComma = CalculatorSettings(decimalSeparator = DecimalSeparator.COMMA)
        assertEquals("1234.56", SettingsManager.normalizeInput("1.234,56", settingsIntlComma))

        val settingsIndianComma = CalculatorSettings(decimalSeparator = DecimalSeparator.COMMA, groupingSeparator = GroupingSeparator.INDIAN)
        assertEquals("1234567.89", SettingsManager.normalizeInput("12.34.567,89", settingsIndianComma))
    }

    @Test
    fun testTrailingZeros() {
        val settings = CalculatorSettings(groupingEnabled = true, decimalSeparator = DecimalSeparator.DOT)
        // Expression formatting should preserve trailing zeros
        assertEquals("1.0", SettingsManager.formatExpression("1.0", settings))
        assertEquals("1.00", SettingsManager.formatExpression("1.00", settings))
        assertEquals("5.20", SettingsManager.formatExpression("5.20", settings))
        assertEquals("6,969,696,969.60", SettingsManager.formatExpression("6969696969.60", settings))

        // Result formatting with stripZeros=false should also preserve them (used for preview)
        val bd = BigDecimal("6969696969.60")
        assertEquals("6,969,696,969.60", SettingsManager.formatDecimal(bd, settings, stripZeros = false))
    }

    @Test
    fun testArithmeticPrecision() {
        val evaluator = ExpressionEvaluator()
        val settings = CalculatorSettings(groupingEnabled = true, decimalSeparator = DecimalSeparator.DOT)
        
        // 0.1 + 0.2 should be 0.3, not 0.3000...004
        val res1 = evaluator.evaluate("0.1 + 0.2", AngleMode.DEG, settings = settings)
        assertEquals("0.3", evaluator.formatResult(res1, settings, stripZeros = false))

        // 6969696969.6 + 1 should be 6969696970.6
        val res2 = evaluator.evaluate("6969696969.6 + 1", AngleMode.DEG, settings = settings)
        assertEquals("6,969,696,970.6", evaluator.formatResult(res2, settings, stripZeros = false))
    }
}
