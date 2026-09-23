package com.bedanta.dotcalc.logic

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.math.BigInteger

class BaseCalculatorEngineTest {
    private val engine = BaseCalculatorEngine()

    @Test
    fun conversionTests() {
        // DEC 10 -> BIN 1010, OCT 12, HEX A
        val v = BigInteger("10")
        val map = engine.toBases(v)
        assertEquals("10", map[NumBase.DEC])
        assertEquals("A", map[NumBase.HEX])
        assertEquals("12", map[NumBase.OCT])
        assertEquals("1010", map[NumBase.BIN])

        val v255 = BigInteger("255")
        val m2 = engine.toBases(v255)
        assertEquals("255", m2[NumBase.DEC])
        assertEquals("FF", m2[NumBase.HEX])
        assertEquals("377", m2[NumBase.OCT])
        assertEquals("11111111", m2[NumBase.BIN])
    }

    @Test
    fun parseAndArithmeticDecimal() {
        val r = engine.evaluate("10 + 5")
        assertTrue(r.error == null)
        assertEquals(BigInteger("15"), r.value)
    }

    @Test
    fun binaryArithmetic() {
        engine.base = NumBase.BIN
        val r = engine.evaluate("1010 + 101")
        assertTrue(r.error == null)
        assertEquals(BigInteger("1111", 2), r.value)
    }

    @Test
    fun hexArithmetic() {
        engine.base = NumBase.HEX
        val r = engine.evaluate("A + 5")
        assertTrue(r.error == null)
        assertEquals(BigInteger("F", 16), r.value)

        val r2 = engine.evaluate("FF + 1")
        assertTrue(r2.error == null)
        assertEquals(BigInteger("100", 16), r2.value)
    }

    @Test
    fun octalArithmetic() {
        engine.base = NumBase.OCT
        val r = engine.evaluate("17 + 1")
        assertTrue(r.error == null)
        assertEquals(BigInteger("20", 8), r.value)
    }

    @Test
    fun bitwiseTests() {
        engine.base = NumBase.BIN
        val r = engine.evaluate("1010 AND 1100")
        assertTrue(r.error == null)
        assertEquals(BigInteger("1000", 2), r.value)

        val r2 = engine.evaluate("1010 OR 1100")
        assertEquals(BigInteger("1110", 2), r2.value)

        val r3 = engine.evaluate("1010 XOR 1100")
        println("DEBUG r3: value=${r3.value} error=${r3.error}")
        assertEquals(BigInteger("0110", 2), r3.value)

        // NOT with 8-bit width
        engine.bitWidth = 8
        engine.signed = false
        val r4 = engine.evaluate("~00001111")
        assertTrue(r4.error == null)
        assertEquals(BigInteger("11110000", 2), r4.value)
    }

    @Test
    fun shiftTests() {
        engine.base = NumBase.BIN
        val r = engine.evaluate("1010 << 2")
        assertEquals(BigInteger("101000", 2), r.value)
        val r2 = engine.evaluate("1000 >> 2")
        assertEquals(BigInteger("10", 2), r2.value)
    }

    @Test
    fun signedUnsignedInterpretation() {
        engine.base = NumBase.BIN
        engine.bitWidth = 8
        engine.signed = false
        val r = engine.evaluate("11111111")
        assertEquals(BigInteger("11111111",2), r.value)
        engine.signed = true
        val r2 = engine.evaluate("11111111")
        // with signed interpretation, two's complement -1 should be shown as value -1 in DEC via toBases
        val map = engine.toBases(r2.value!!)
        assertEquals("11111111", map[NumBase.BIN])
    }
}
