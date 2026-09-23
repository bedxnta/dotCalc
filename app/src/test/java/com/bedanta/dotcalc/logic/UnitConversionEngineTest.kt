package com.bedanta.dotcalc.logic

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.PI

class UnitConversionEngineTest {
    private val engine = UnitConversionEngine()

    private fun assertApprox(expected: Double, actual: Double?, tolerance: Double = 1e-9) {
        assertTrue(
            "expected=$expected actual=${actual ?: "null"} delta=${kotlin.math.abs((actual ?: 0.0) - expected)}",
            actual != null && kotlin.math.abs(actual - expected) <= tolerance
        )
    }

    private fun assertRoundTrip(value: Double, fromId: String, toId: String, category: UnitCategory, tolerance: Double = 1e-8) {
        val from = engine.unitsFor(category).first { it.id == fromId }
        val to = engine.unitsFor(category).first { it.id == toId }
        val converted = engine.convert(value, from, to)
        assertTrue("Conversion failed for $category $fromId -> $toId: ${converted.error}", converted.value != null)
        val roundTrip = engine.convert(converted.value!!, to, from)
        assertTrue("Round trip failed for $category $fromId -> $toId: ${roundTrip.error}", roundTrip.value != null)
        assertApprox(value, roundTrip.value, tolerance)
    }

    @Test
    fun lengthConversions() {
        val km = engine.unitsFor(UnitCategory.LENGTH).first { it.id == "km" }
        val m = engine.unitsFor(UnitCategory.LENGTH).first { it.id == "m" }
        val mile = engine.unitsFor(UnitCategory.LENGTH).first { it.id == "mi" }
        val inch = engine.unitsFor(UnitCategory.LENGTH).first { it.id == "in" }

        assertApprox(1000.0, engine.convert(1.0, km, m).value)
        assertApprox(1.609344, engine.convert(1.0, mile, km).value)
        assertApprox(0.0254, engine.convert(1.0, inch, m).value)
        assertRoundTrip(12.5, "in", "cm", UnitCategory.LENGTH)
    }

    @Test
    fun areaConversions() {
        val m2 = engine.unitsFor(UnitCategory.AREA).first { it.id == "m2" }
        val cm2 = engine.unitsFor(UnitCategory.AREA).first { it.id == "cm2" }
        val acre = engine.unitsFor(UnitCategory.AREA).first { it.id == "acre" }

        assertApprox(10000.0, engine.convert(1.0, m2, cm2).value)
        assertApprox(4046.8564224, engine.convert(1.0, acre, m2).value)
        assertRoundTrip(2.5, "acre", "m2", UnitCategory.AREA)
    }

    @Test
    fun volumeConversions() {
        val l = engine.unitsFor(UnitCategory.VOLUME).first { it.id == "l" }
        val ml = engine.unitsFor(UnitCategory.VOLUME).first { it.id == "ml" }
        val usGal = engine.unitsFor(UnitCategory.VOLUME).first { it.id == "us_gal" }
        val impGal = engine.unitsFor(UnitCategory.VOLUME).first { it.id == "imp_gal" }

        assertApprox(1000.0, engine.convert(1.0, l, ml).value)
        assertApprox(3.785411784, engine.convert(1.0, usGal, l).value)
        assertApprox(4.54609, engine.convert(1.0, impGal, l).value)
        assertRoundTrip(1.5, "us_gal", "l", UnitCategory.VOLUME)
    }

    @Test
    fun massConversions() {
        val kg = engine.unitsFor(UnitCategory.MASS).first { it.id == "kg" }
        val g = engine.unitsFor(UnitCategory.MASS).first { it.id == "g" }
        val lb = engine.unitsFor(UnitCategory.MASS).first { it.id == "lb" }
        val stone = engine.unitsFor(UnitCategory.MASS).first { it.id == "st" }

        assertApprox(1000.0, engine.convert(1.0, kg, g).value)
        assertApprox(0.45359237, engine.convert(1.0, lb, kg).value)
        assertApprox(6.35029318, engine.convert(1.0, stone, kg).value)
        assertRoundTrip(3.2, "lb", "kg", UnitCategory.MASS)
    }

    @Test
    fun temperatureConversions() {
        val c = engine.unitsFor(UnitCategory.TEMPERATURE).first { it.id == "c" }
        val f = engine.unitsFor(UnitCategory.TEMPERATURE).first { it.id == "f" }
        val k = engine.unitsFor(UnitCategory.TEMPERATURE).first { it.id == "k" }
        val rankine = engine.unitsFor(UnitCategory.TEMPERATURE).first { it.id == "rankine" }
        val reaumur = engine.unitsFor(UnitCategory.TEMPERATURE).first { it.id == "reaumur" }
        val romer = engine.unitsFor(UnitCategory.TEMPERATURE).first { it.id == "romer" }
        val newton = engine.unitsFor(UnitCategory.TEMPERATURE).first { it.id == "newton" }
        val delisle = engine.unitsFor(UnitCategory.TEMPERATURE).first { it.id == "delisle" }

        assertApprox(32.0, engine.convert(0.0, c, f).value)
        assertApprox(212.0, engine.convert(100.0, c, f).value)
        assertApprox(273.15, engine.convert(0.0, c, k).value)
        assertApprox(491.67, engine.convert(0.0, c, rankine).value)
        assertApprox(64.0, engine.convert(80.0, c, reaumur).value)
        assertApprox(60.0, engine.convert(100.0, c, romer).value)
        assertApprox(33.0, engine.convert(100.0, c, newton).value)
        assertApprox(150.0, engine.convert(0.0, c, delisle).value)
        assertRoundTrip(25.0, "c", "f", UnitCategory.TEMPERATURE, 1e-7)
    }

    @Test
    fun timeConversions() {
        val s = engine.unitsFor(UnitCategory.TIME).first { it.id == "s" }
        val min = engine.unitsFor(UnitCategory.TIME).first { it.id == "min" }
        val h = engine.unitsFor(UnitCategory.TIME).first { it.id == "h" }
        val fortnight = engine.unitsFor(UnitCategory.TIME).first { it.id == "fortnight" }

        assertApprox(1.0, engine.convert(60.0, s, min).value)
        assertApprox(1.0, engine.convert(60.0, min, h).value)
        assertApprox(1209600.0, engine.convert(1.0, fortnight, s).value)
        assertRoundTrip(1.5, "h", "min", UnitCategory.TIME)
    }

    @Test
    fun speedConversions() {
        val ms = engine.unitsFor(UnitCategory.SPEED).first { it.id == "m_s" }
        val kmh = engine.unitsFor(UnitCategory.SPEED).first { it.id == "km_h" }
        val mach = engine.unitsFor(UnitCategory.SPEED).first { it.id == "mach" }

        assertApprox(3.6, engine.convert(1.0, ms, kmh).value)
        assertApprox(343.0, engine.convert(1.0, mach, ms).value)
        assertRoundTrip(2.0, "mach", "m_s", UnitCategory.SPEED)
    }

    @Test
    fun pressureConversions() {
        val bar = engine.unitsFor(UnitCategory.PRESSURE).first { it.id == "bar" }
        val pa = engine.unitsFor(UnitCategory.PRESSURE).first { it.id == "pa" }
        val atm = engine.unitsFor(UnitCategory.PRESSURE).first { it.id == "atm" }
        val at = engine.unitsFor(UnitCategory.PRESSURE).first { it.id == "at" }

        assertApprox(100000.0, engine.convert(1.0, bar, pa).value)
        assertApprox(101325.0, engine.convert(1.0, atm, pa).value)
        assertApprox(98066.5, engine.convert(1.0, at, pa).value)
        assertRoundTrip(1.2, "bar", "pa", UnitCategory.PRESSURE)
    }

    @Test
    fun energyConversions() {
        val kwh = engine.unitsFor(UnitCategory.ENERGY).first { it.id == "kwh" }
        val j = engine.unitsFor(UnitCategory.ENERGY).first { it.id == "j" }
        val therm = engine.unitsFor(UnitCategory.ENERGY).first { it.id == "therm" }

        assertApprox(3600000.0, engine.convert(1.0, kwh, j).value)
        assertApprox(105480400.0, engine.convert(1.0, therm, j).value)
        assertRoundTrip(0.75, "kwh", "j", UnitCategory.ENERGY)
    }

    @Test
    fun powerConversions() {
        val kw = engine.unitsFor(UnitCategory.POWER).first { it.id == "kw" }
        val w = engine.unitsFor(UnitCategory.POWER).first { it.id == "w" }
        val hpMetric = engine.unitsFor(UnitCategory.POWER).first { it.id == "hp_metric" }

        assertApprox(1000.0, engine.convert(1.0, kw, w).value)
        assertApprox(735.49875, engine.convert(1.0, hpMetric, w).value)
        assertRoundTrip(1.5, "kw", "w", UnitCategory.POWER)
    }

    @Test
    fun frequencyConversions() {
        val mhz = engine.unitsFor(UnitCategory.FREQUENCY).first { it.id == "mhz2" }
        val hz = engine.unitsFor(UnitCategory.FREQUENCY).first { it.id == "hz" }
        val rpm = engine.unitsFor(UnitCategory.FREQUENCY).first { it.id == "rpm" }

        assertApprox(1000000.0, engine.convert(1.0, mhz, hz).value)
        assertApprox(1.0, engine.convert(60.0, rpm, hz).value)
        assertRoundTrip(2.5, "hz", "rpm", UnitCategory.FREQUENCY)
    }

    @Test
    fun angleConversions() {
        val deg = engine.unitsFor(UnitCategory.ANGLE).first { it.id == "deg" }
        val rad = engine.unitsFor(UnitCategory.ANGLE).first { it.id == "rad" }
        val rev = engine.unitsFor(UnitCategory.ANGLE).first { it.id == "rev" }

        assertApprox(PI, engine.convert(180.0, deg, rad).value)
        assertApprox(PI / 2.0, engine.convert(90.0, deg, rad).value)
        assertApprox(360.0, engine.convert(1.0, rev, deg).value)
        assertRoundTrip(0.5, "rev", "rad", UnitCategory.ANGLE)
    }

    @Test
    fun dataConversions() {
        val byte = engine.unitsFor(UnitCategory.DATA).first { it.id == "byte" }
        val bit = engine.unitsFor(UnitCategory.DATA).first { it.id == "bit" }
        val kib = engine.unitsFor(UnitCategory.DATA).first { it.id == "kib" }
        val kbyte = engine.unitsFor(UnitCategory.DATA).first { it.id == "kbyte" }
        val gib = engine.unitsFor(UnitCategory.DATA).first { it.id == "gibyte" }

        assertApprox(8.0, engine.convert(1.0, byte, bit).value)
        assertApprox(1024.0, engine.convert(1.0, kib, byte).value)
        assertApprox(1000.0, engine.convert(1.0, kbyte, byte).value)
        assertApprox(1073741824.0, engine.convert(1.0, gib, byte).value)
        assertRoundTrip(2.0, "gibyte", "byte", UnitCategory.DATA)
    }

    @Test
    fun electricAndRadioConversions() {
        val kv = engine.unitsFor(UnitCategory.ELECTRICAL).first { it.id == "kv" }
        val v = engine.unitsFor(UnitCategory.ELECTRICAL).first { it.id == "v" }
        val ah = engine.unitsFor(UnitCategory.ELECTRICAL).first { it.id == "ah" }
        val c = engine.unitsFor(UnitCategory.ELECTRICAL).first { it.id == "c" }
        val dbm = engine.unitsFor(UnitCategory.RADIO).first { it.id == "dbm" }
        val dbw = engine.unitsFor(UnitCategory.RADIO).first { it.id == "dbw" }
        val watt = engine.unitsFor(UnitCategory.POWER).first { it.id == "w" }

        assertApprox(1000.0, engine.convert(1.0, kv, v).value)
        assertApprox(3600.0, engine.convert(1.0, ah, c).value)
        assertApprox(0.001, engine.convert(0.0, dbm, watt).value)
        assertApprox(1.0, engine.convert(30.0, dbm, watt).value)
        assertApprox(1.0, engine.convert(0.0, dbw, watt).value)
        assertRoundTrip(1.0, "ah", "c", UnitCategory.ELECTRICAL)
    }

    @Test
    fun densityAndViscosityAndMagnetism() {
        val gCm3 = engine.unitsFor(UnitCategory.DENSITY).first { it.id == "g_cm3" }
        val kgM3 = engine.unitsFor(UnitCategory.DENSITY).first { it.id == "kg_m3" }
        val poise = engine.unitsFor(UnitCategory.VISCOSITY).first { it.id == "poise" }
        val paS = engine.unitsFor(UnitCategory.VISCOSITY).first { it.id == "pa_s" }
        val tesla = engine.unitsFor(UnitCategory.MAGNETISM).first { it.id == "t" }
        val gauss = engine.unitsFor(UnitCategory.MAGNETISM).first { it.id == "gauss" }

        assertApprox(1000.0, engine.convert(1.0, gCm3, kgM3).value)
        assertApprox(0.1, engine.convert(1.0, poise, paS).value)
        assertApprox(10000.0, engine.convert(1.0, tesla, gauss).value)
    }

    @Test
    fun invalidTemperatureAndInputHandling() {
        val k = engine.unitsFor(UnitCategory.TEMPERATURE).first { it.id == "k" }
        val c = engine.unitsFor(UnitCategory.TEMPERATURE).first { it.id == "c" }
        val error = engine.convert(-300.0, k, c)
        assertTrue(error.error != null)
        assertTrue(engine.parseInput("abc") == null)
        assertNotNull(engine.parseInput("1.5e2"))
    }
}
