package com.bedanta.dotcalc.logic

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.math.BigDecimal
import java.math.MathContext

class CommercialMathTest {

    private val evaluator = ExpressionEvaluator()
    private val mc = MathContext(34)

    @Test
    fun testPercentage() {
        // Simple percentage
        assertEquals("0.5", evaluator.formatResult(evaluator.evaluate("50%", AngleMode.DEG)))
        
        // Contextual additive percentage: 100 + 5% should be 100 + (100 * 0.05) = 105
        assertEquals("105", evaluator.formatResult(evaluator.evaluate("100+5%", AngleMode.DEG)))
        
        // Contextual subtractive percentage: 200 - 10% should be 200 - (200 * 0.1) = 180
        assertEquals("180", evaluator.formatResult(evaluator.evaluate("200−10%", AngleMode.DEG)))
        
        // Multiplicative percentage: 100 * 5% should be 100 * 0.05 = 5
        assertEquals("5", evaluator.formatResult(evaluator.evaluate("100×5%", AngleMode.DEG)))

        // Chain percentage: 100 + 10% + 10% should be 110 + 11 = 121
        assertEquals("121", evaluator.formatResult(evaluator.evaluate("100+10%+10%", AngleMode.DEG)))
    }

    @Test
    fun testFractions() {
        val result = evaluator.evaluate("1÷3 + 1÷3", AngleMode.DEG, AppMode.CONVERT)
        assertTrue(result.isExact)
        assertEquals("2/3", result.toExactString())
        
        val product = evaluator.evaluate("3÷4 × 2÷5", AngleMode.DEG, AppMode.CONVERT)
        assertEquals("3/10", product.toExactString())
    }

    @Test
    fun testSpecialTrigValues() {
        // sin(30) = 1/2
        val sin30 = evaluator.evaluate("sin(30)", AngleMode.DEG)
        assertTrue(sin30.isExact)
        assertEquals("1/2", sin30.toExactString())

        // sin(45) = √2/2
        val sin45 = evaluator.evaluate("sin(45)", AngleMode.DEG)
        assertTrue(sin45.isExact)
        assertEquals("√2/2", sin45.toExactString())

        // sin(60) = √3/2
        val sin60 = evaluator.evaluate("sin(60)", AngleMode.DEG)
        assertTrue(sin60.isExact)
        assertEquals("√3/2", sin60.toExactString())

        // cos(90) = 0
        val cos90 = evaluator.evaluate("cos(90)", AngleMode.DEG)
        assertEquals("0", cos90.toExactString())
    }

    @Test
    fun testImplicitMultiplicationAndZeroResults() {
        assertEquals("36", evaluator.formatResult(evaluator.evaluate("9(4)", AngleMode.DEG)))
        assertEquals("14", evaluator.formatResult(evaluator.evaluate("2(3+4)", AngleMode.DEG)))
        assertEquals("0", evaluator.formatResult(evaluator.evaluate("9-9", AngleMode.DEG)))
        assertEquals("0", evaluator.formatResult(evaluator.evaluate("(5-5)×7", AngleMode.DEG)))
        assertEquals("0", evaluator.formatResult(evaluator.evaluate("0", AngleMode.DEG)))
    }

    @Test
    fun testUndefinedExpressionsDoNotProduceLiveZeroPreview() {
        assertEquals(MathValue.Undefined, evaluator.evaluate("1÷0", AngleMode.DEG))
        assertEquals(MathValue.Undefined, evaluator.evaluate("tan(90)", AngleMode.DEG))
        assertEquals(MathValue.Undefined, evaluator.evaluate("log(0)", AngleMode.DEG))
        assertEquals(MathValue.Undefined, evaluator.evaluate("√(-1)", AngleMode.DEG))
    }

    @Test
    fun testDescriptiveErrorMessages() {
        assertEquals("Error: Division by zero", evaluator.getErrorMessage("1÷0", AngleMode.DEG))
        assertEquals("Error: Undefined (tan is undefined at 90°)", evaluator.getErrorMessage("tan(90)", AngleMode.DEG))
        assertEquals("Error: Out of domain (asin only accepts -1 to 1)", evaluator.getErrorMessage("sin⁻¹(2)", AngleMode.DEG))
        assertEquals("Error: Log undefined for zero or negative numbers", evaluator.getErrorMessage("log(-5)", AngleMode.DEG))
        assertEquals("Error: Cannot take square root of a negative number", evaluator.getErrorMessage("√(-4)", AngleMode.DEG))
        assertEquals("Error: Unmatched parentheses", evaluator.getErrorMessage("(1+2", AngleMode.DEG))
    }

    @Test
    fun testExactTrigValuesMatchDecimalResults() {
        val cases = listOf(
            Triple("sin(0)", AngleMode.DEG, 0.0),
            Triple("sin(30)", AngleMode.DEG, Math.sin(Math.PI / 6.0)),
            Triple("sin(45)", AngleMode.DEG, Math.sin(Math.PI / 4.0)),
            Triple("sin(60)", AngleMode.DEG, Math.sin(Math.PI / 3.0)),
            Triple("sin(90)", AngleMode.DEG, 1.0),
            Triple("cos(0)", AngleMode.DEG, 1.0),
            Triple("cos(30)", AngleMode.DEG, Math.cos(Math.PI / 6.0)),
            Triple("cos(45)", AngleMode.DEG, Math.cos(Math.PI / 4.0)),
            Triple("cos(60)", AngleMode.DEG, Math.cos(Math.PI / 3.0)),
            Triple("cos(90)", AngleMode.DEG, 0.0),
            Triple("tan(45)", AngleMode.DEG, 1.0),
            Triple("sin(π/6)", AngleMode.RAD, Math.sin(Math.PI / 6.0)),
            Triple("sin(π/4)", AngleMode.RAD, Math.sin(Math.PI / 4.0)),
            Triple("sin(π/3)", AngleMode.RAD, Math.sin(Math.PI / 3.0)),
            Triple("cos(π/6)", AngleMode.RAD, Math.cos(Math.PI / 6.0)),
            Triple("cos(π/4)", AngleMode.RAD, Math.cos(Math.PI / 4.0)),
            Triple("cos(π/3)", AngleMode.RAD, Math.cos(Math.PI / 3.0)),
            Triple("tan(π/4)", AngleMode.RAD, 1.0)
        )

        for ((expression, mode, expected) in cases) {
            val result = evaluator.evaluate(expression, mode)
            assertTrue("$expression should be exact", result.isExact)
            assertEquals("Exact value mismatch for $expression", expected, result.toNumeric(mc).toDouble(), 1e-10)
        }
    }

    @Test
    fun testRadicals() {
        val sqrt8 = evaluator.evaluate("√8", AngleMode.DEG)
        // √8 is not automatically simplified to 2√2 in this basic implementation
        // but it is kept exact. Let's check numeric.
        assertEquals(BigDecimal.valueOf(8).sqrt(mc).toDouble(), sqrt8.toNumeric(mc).toDouble(), 1e-15)
    }

    @Test
    fun testTrigSingularities() {
        val tan90 = evaluator.evaluate("tan(90)", AngleMode.DEG)
        assertEquals(MathValue.Undefined, tan90)
        
        val tan270 = evaluator.evaluate("tan(270)", AngleMode.DEG)
        assertEquals(MathValue.Undefined, tan270)
    }

    @Test
    fun testDomainErrors() {
        assertEquals(MathValue.Undefined, evaluator.evaluate("1÷0", AngleMode.DEG))
        assertEquals(MathValue.Undefined, evaluator.evaluate("√(−1)", AngleMode.DEG))
        assertEquals(MathValue.Undefined, evaluator.evaluate("ln(0)", AngleMode.DEG))
        assertEquals(MathValue.Undefined, evaluator.evaluate("log(0)", AngleMode.DEG))
    }

    @Test
    fun testConstants() {
        val pi = evaluator.evaluate("π", AngleMode.DEG)
        assertTrue(pi.isExact)
        assertEquals("π", pi.toExactString())

        val twoPi = evaluator.evaluate("2×π", AngleMode.DEG)
        assertEquals("2π", twoPi.toExactString())
    }

    @Test
    fun testLogarithms() {
        assertEquals("1", evaluator.formatResult(evaluator.evaluate("ln(e)", AngleMode.DEG)))
        assertEquals("2", evaluator.formatResult(evaluator.evaluate("log(100)", AngleMode.DEG)))
    }
}
