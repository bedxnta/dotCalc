package com.bedanta.dotcalc.logic

import java.lang.Math.PI
import java.math.BigDecimal
import java.util.Locale
import kotlin.math.abs

enum class AppMode {
    BASIC,
    CONVERT,
    BASE
}

enum class UnitCategory {
    LENGTH,
    AREA,
    VOLUME,
    MASS,
    TEMPERATURE,
    TIME,
    SPEED,
    PRESSURE,
    ENERGY,
    POWER,
    FREQUENCY,
    ANGLE,
    DATA,
    ILLUMINATION,
    ELECTRICAL,
    RADIOACTIVITY,
    TORQUE,
    FORCE,
    DENSITY,
    VISCOSITY,
    MAGNETISM,
    RADIO,
    INFORMATION_RATE
}

data class UnitDefinition(
    val id: String,
    val category: UnitCategory,
    val label: String,
    val symbol: String,
    val aliases: Set<String>,
    val toBase: (Double) -> Double,
    val fromBase: (Double) -> Double,
    val isTemperature: Boolean = false,
    val description: String? = null
)

data class ConversionResult(
    val value: Double?,
    val error: String? = null,
    val isValid: Boolean = error == null && value != null && value.isFinite()
)

class UnitConversionEngine {
    fun categories(): List<UnitCategory> = listOf(
        UnitCategory.LENGTH,
        UnitCategory.AREA,
        UnitCategory.VOLUME,
        UnitCategory.MASS,
        UnitCategory.TEMPERATURE,
        UnitCategory.TIME,
        UnitCategory.SPEED,
        UnitCategory.PRESSURE,
        UnitCategory.ENERGY,
        UnitCategory.POWER,
        UnitCategory.FREQUENCY,
        UnitCategory.ANGLE,
        UnitCategory.DATA,
        UnitCategory.ILLUMINATION,
        UnitCategory.ELECTRICAL,
        UnitCategory.RADIOACTIVITY,
        UnitCategory.TORQUE,
        UnitCategory.FORCE,
        UnitCategory.DENSITY,
        UnitCategory.VISCOSITY,
        UnitCategory.MAGNETISM,
        UnitCategory.RADIO,
        UnitCategory.INFORMATION_RATE
    )

    fun unitsFor(category: UnitCategory): List<UnitDefinition> = when (category) {
        UnitCategory.LENGTH -> listOf(
            UnitDefinition("pm", UnitCategory.LENGTH, "Picometer", "pm", setOf("pm", "picometer", "picometre"), { it * 1e-12 }, { it / 1e-12 }),
            UnitDefinition("nm", UnitCategory.LENGTH, "Nanometer", "nm", setOf("nm", "nanometer", "nanometre"), { it * 1e-9 }, { it / 1e-9 }),
            UnitDefinition("um", UnitCategory.LENGTH, "Micrometer", "µm", setOf("um", "µm", "micrometer", "micrometre"), { it * 1e-6 }, { it / 1e-6 }),
            UnitDefinition("mm", UnitCategory.LENGTH, "Millimeter", "mm", setOf("mm", "millimeter", "millimetre"), { it * 1e-3 }, { it / 1e-3 }),
            UnitDefinition("cm", UnitCategory.LENGTH, "Centimeter", "cm", setOf("cm", "centimeter", "centimetre"), { it * 1e-2 }, { it / 1e-2 }),
            UnitDefinition("dm", UnitCategory.LENGTH, "Decimeter", "dm", setOf("dm", "decimeter", "decimetre"), { it * 1e-1 }, { it / 1e-1 }),
            UnitDefinition("m", UnitCategory.LENGTH, "Meter", "m", setOf("m", "meter", "metre"), { it }, { it }),
            UnitDefinition("dam", UnitCategory.LENGTH, "Decameter", "dam", setOf("dam", "decameter", "decametre"), { it * 1e1 }, { it / 1e1 }),
            UnitDefinition("hm", UnitCategory.LENGTH, "Hectometer", "hm", setOf("hm", "hectometer", "hectometre"), { it * 1e2 }, { it / 1e2 }),
            UnitDefinition("km", UnitCategory.LENGTH, "Kilometer", "km", setOf("km", "kilometer", "kilometre"), { it * 1e3 }, { it / 1e3 }),
            UnitDefinition("Mm", UnitCategory.LENGTH, "Megameter", "Mm", setOf("Mm", "megameter"), { it * 1e6 }, { it / 1e6 }),
            UnitDefinition("in", UnitCategory.LENGTH, "Inch", "in", setOf("in", "inch", "\""), { it * 0.0254 }, { it / 0.0254 }),
            UnitDefinition("ft", UnitCategory.LENGTH, "Foot", "ft", setOf("ft", "foot", "'"), { it * 0.3048 }, { it / 0.3048 }),
            UnitDefinition("yd", UnitCategory.LENGTH, "Yard", "yd", setOf("yd", "yard"), { it * 0.9144 }, { it / 0.9144 }),
            UnitDefinition("mi", UnitCategory.LENGTH, "Mile", "mi", setOf("mi", "mile"), { it * 1609.344 }, { it / 1609.344 }),
            UnitDefinition("nmi", UnitCategory.LENGTH, "Nautical mile", "nmi", setOf("nmi", "nautical mile", "nm"), { it * 1852.0 }, { it / 1852.0 }),
            UnitDefinition("fathom", UnitCategory.LENGTH, "Fathom", "ftm", setOf("fathom", "ftm"), { it * 1.8288 }, { it / 1.8288 }),
            UnitDefinition("rod", UnitCategory.LENGTH, "Rod", "rd", setOf("rod", "rd"), { it * 5.0292 }, { it / 5.0292 }),
            UnitDefinition("chain", UnitCategory.LENGTH, "Chain", "ch", setOf("chain", "ch"), { it * 20.1168 }, { it / 20.1168 }),
            UnitDefinition("furlong", UnitCategory.LENGTH, "Furlong", "fur", setOf("furlong", "fur"), { it * 201.168 }, { it / 201.168 }),
            UnitDefinition("ly", UnitCategory.LENGTH, "Light-year", "ly", setOf("ly", "light year", "light-year"), { it * 9.4607304725808e15 }, { it / 9.4607304725808e15 }),
            UnitDefinition("au", UnitCategory.LENGTH, "Astronomical unit", "AU", setOf("au", "astronomical unit"), { it * 149597870700.0 }, { it / 149597870700.0 }),
            UnitDefinition("pc", UnitCategory.LENGTH, "Parsec", "pc", setOf("pc", "parsec"), { it * 3.085677581491367e16 }, { it / 3.085677581491367e16 })
        )
        UnitCategory.AREA -> listOf(
            UnitDefinition("um2", UnitCategory.AREA, "Square micrometer", "µm²", setOf("um2", "µm²", "sq um"), { it * 1e-12 }, { it / 1e-12 }),
            UnitDefinition("mm2", UnitCategory.AREA, "Square millimeter", "mm²", setOf("mm2", "mm²", "sq mm"), { it * 1e-6 }, { it / 1e-6 }),
            UnitDefinition("cm2", UnitCategory.AREA, "Square centimeter", "cm²", setOf("cm2", "cm²"), { it * 1e-4 }, { it / 1e-4 }),
            UnitDefinition("dm2", UnitCategory.AREA, "Square decimeter", "dm²", setOf("dm2", "dm²"), { it * 1e-2 }, { it / 1e-2 }),
            UnitDefinition("m2", UnitCategory.AREA, "Square meter", "m²", setOf("m2", "m²", "sqm", "sq m"), { it }, { it }),
            UnitDefinition("km2", UnitCategory.AREA, "Square kilometer", "km²", setOf("km2", "km²"), { it * 1e6 }, { it / 1e6 }),
            UnitDefinition("in2", UnitCategory.AREA, "Square inch", "in²", setOf("in2", "in²", "sq in"), { it * 0.00064516 }, { it / 0.00064516 }),
            UnitDefinition("ft2", UnitCategory.AREA, "Square foot", "ft²", setOf("ft2", "ft²", "sq ft"), { it * 0.09290304 }, { it / 0.09290304 }),
            UnitDefinition("yd2", UnitCategory.AREA, "Square yard", "yd²", setOf("yd2", "yd²", "sq yd"), { it * 0.83612736 }, { it / 0.83612736 }),
            UnitDefinition("mi2", UnitCategory.AREA, "Square mile", "mi²", setOf("mi2", "mi²", "sq mi"), { it * 2589988.110336 }, { it / 2589988.110336 }),
            UnitDefinition("acre", UnitCategory.AREA, "Acre", "acre", setOf("acre", "ac"), { it * 4046.8564224 }, { it / 4046.8564224 }),
            UnitDefinition("ha", UnitCategory.AREA, "Hectare", "ha", setOf("ha", "hectare"), { it * 10000.0 }, { it / 10000.0 }),
            UnitDefinition("rod2", UnitCategory.AREA, "Square rod", "rd²", setOf("rod2", "square rod", "rd²"), { it * 25.29285264 }, { it / 25.29285264 }),
            UnitDefinition("chain2", UnitCategory.AREA, "Square chain", "ch²", setOf("chain2", "square chain", "ch²"), { it * 404.68564224 }, { it / 404.68564224 }),
            UnitDefinition("fur2", UnitCategory.AREA, "Square furlong", "fur²", setOf("fur2", "square furlong", "fur²"), { it * 404685.64224 }, { it / 404685.64224 }),
            UnitDefinition("are", UnitCategory.AREA, "Are", "a", setOf("are", "a"), { it * 100.0 }, { it / 100.0 })
        )
        UnitCategory.VOLUME -> listOf(
            UnitDefinition("mm3", UnitCategory.VOLUME, "Cubic millimeter", "mm³", setOf("mm3", "cubic millimeter", "cubic millimetre"), { it * 1e-9 }, { it / 1e-9 }),
            UnitDefinition("cm3", UnitCategory.VOLUME, "Cubic centimeter", "cm³", setOf("cm3", "cc", "cubic centimeter", "cubic centimetre"), { it * 1e-6 }, { it / 1e-6 }),
            UnitDefinition("dm3", UnitCategory.VOLUME, "Cubic decimeter", "dm³", setOf("dm3", "litre", "liter", "litre"), { it * 1e-3 }, { it / 1e-3 }),
            UnitDefinition("m3", UnitCategory.VOLUME, "Cubic meter", "m³", setOf("m3", "cubic meter", "cubic metre"), { it }, { it }),
            UnitDefinition("km3", UnitCategory.VOLUME, "Cubic kilometer", "km³", setOf("km3", "cubic kilometer"), { it * 1e9 }, { it / 1e9 }),
            UnitDefinition("ml", UnitCategory.VOLUME, "Milliliter", "mL", setOf("ml", "milliliter", "millilitre"), { it * 1e-6 }, { it / 1e-6 }),
            UnitDefinition("cl", UnitCategory.VOLUME, "Centiliter", "cL", setOf("cl", "centiliter", "centilitre"), { it * 1e-5 }, { it / 1e-5 }),
            UnitDefinition("dl", UnitCategory.VOLUME, "Deciliter", "dL", setOf("dl", "deciliter", "decilitre"), { it * 1e-4 }, { it / 1e-4 }),
            UnitDefinition("l", UnitCategory.VOLUME, "Liter", "L", setOf("l", "liter", "litre"), { it * 1e-3 }, { it / 1e-3 }),
            UnitDefinition("kl", UnitCategory.VOLUME, "Kiloliter", "kL", setOf("kl", "kiloliter", "kilolitre"), { it * 1.0 }, { it * 1.0 }),
            UnitDefinition("us_tsp", UnitCategory.VOLUME, "US teaspoon", "tsp", setOf("us tsp", "us teaspoon", "teaspoon"), { it * 4.92892159375e-6 }, { it / 4.92892159375e-6 }),
            UnitDefinition("us_tbsp", UnitCategory.VOLUME, "US tablespoon", "tbsp", setOf("us tbsp", "us tablespoon", "tablespoon"), { it * 1.478676478125e-5 }, { it / 1.478676478125e-5 }),
            UnitDefinition("us_fl_oz", UnitCategory.VOLUME, "US fluid ounce", "fl oz", setOf("us fl oz", "us fluid ounce"), { it * 2.95735295625e-5 }, { it / 2.95735295625e-5 }),
            UnitDefinition("us_cup", UnitCategory.VOLUME, "US cup", "cup", setOf("us cup", "cup"), { it * 2.365882365e-4 }, { it / 2.365882365e-4 }),
            UnitDefinition("us_pint", UnitCategory.VOLUME, "US pint", "pt", setOf("us pint", "pint", "pt"), { it * 4.73176473e-4 }, { it / 4.73176473e-4 }),
            UnitDefinition("us_quart", UnitCategory.VOLUME, "US quart", "qt", setOf("us quart", "quart", "qt"), { it * 9.46352946e-4 }, { it / 9.46352946e-4 }),
            UnitDefinition("us_gal", UnitCategory.VOLUME, "US gallon", "gal", setOf("us gallon", "gallon", "gal"), { it * 3.785411784e-3 }, { it / 3.785411784e-3 }),
            UnitDefinition("imp_tsp", UnitCategory.VOLUME, "Imperial teaspoon", "tsp imp", setOf("imp tsp", "imperial teaspoon"), { it * 5.919388020833333e-6 }, { it / 5.919388020833333e-6 }),
            UnitDefinition("imp_tbsp", UnitCategory.VOLUME, "Imperial tablespoon", "tbsp imp", setOf("imp tbsp", "imperial tablespoon"), { it * 1.77581640625e-5 }, { it / 1.77581640625e-5 }),
            UnitDefinition("imp_fl_oz", UnitCategory.VOLUME, "Imperial fluid ounce", "fl oz imp", setOf("imp fl oz", "imperial fluid ounce"), { it * 2.84130625e-5 }, { it / 2.84130625e-5 }),
            UnitDefinition("imp_cup", UnitCategory.VOLUME, "Imperial cup", "cup imp", setOf("imp cup", "imperial cup"), { it * 2.84130625e-4 }, { it / 2.84130625e-4 }),
            UnitDefinition("imp_pint", UnitCategory.VOLUME, "Imperial pint", "pt imp", setOf("uk pint", "imperial pint", "pt imp"), { it * 5.6826125e-4 }, { it / 5.6826125e-4 }),
            UnitDefinition("imp_quart", UnitCategory.VOLUME, "Imperial quart", "qt imp", setOf("imp quart", "imperial quart"), { it * 1.1365225e-3 }, { it / 1.1365225e-3 }),
            UnitDefinition("imp_gal", UnitCategory.VOLUME, "Imperial gallon", "gal imp", setOf("uk gallon", "imperial gallon", "gal imp"), { it * 4.54609e-3 }, { it / 4.54609e-3 }),
            UnitDefinition("in3", UnitCategory.VOLUME, "Cubic inch", "in³", setOf("in3", "in³", "cubic inch"), { it * 1.6387064e-5 }, { it / 1.6387064e-5 }),
            UnitDefinition("ft3", UnitCategory.VOLUME, "Cubic foot", "ft³", setOf("ft3", "ft³", "cubic foot"), { it * 0.028316846592 }, { it / 0.028316846592 }),
            UnitDefinition("yd3", UnitCategory.VOLUME, "Cubic yard", "yd³", setOf("yd3", "yd³", "cubic yard"), { it * 0.764554857984 }, { it / 0.764554857984 })
        )
        UnitCategory.MASS -> listOf(
            UnitDefinition("ug", UnitCategory.MASS, "Microgram", "µg", setOf("ug", "µg", "microgram"), { it * 1e-9 }, { it / 1e-9 }),
            UnitDefinition("mg", UnitCategory.MASS, "Milligram", "mg", setOf("mg", "milligram"), { it * 1e-6 }, { it / 1e-6 }),
            UnitDefinition("cg", UnitCategory.MASS, "Centigram", "cg", setOf("cg", "centigram"), { it * 1e-5 }, { it / 1e-5 }),
            UnitDefinition("dg", UnitCategory.MASS, "Decigram", "dg", setOf("dg", "decigram"), { it * 1e-4 }, { it / 1e-4 }),
            UnitDefinition("g", UnitCategory.MASS, "Gram", "g", setOf("g", "gram"), { it * 1e-3 }, { it / 1e-3 }),
            UnitDefinition("dag", UnitCategory.MASS, "Decagram", "dag", setOf("dag", "decagram"), { it * 1e-2 }, { it / 1e-2 }),
            UnitDefinition("hg", UnitCategory.MASS, "Hectogram", "hg", setOf("hg", "hectogram"), { it * 1e-1 }, { it / 1e-1 }),
            UnitDefinition("kg", UnitCategory.MASS, "Kilogram", "kg", setOf("kg", "kilogram"), { it }, { it }),
            UnitDefinition("t", UnitCategory.MASS, "Metric ton", "t", setOf("t", "metric ton", "tonne"), { it * 1000.0 }, { it / 1000.0 }),
            UnitDefinition("mt", UnitCategory.MASS, "Megaton", "Mt", setOf("mt", "megaton"), { it * 1_000_000.0 }, { it / 1_000_000.0 }),
            UnitDefinition("grain", UnitCategory.MASS, "Grain", "gr", setOf("grain", "gr"), { it * 6.479891e-5 }, { it / 6.479891e-5 }),
            UnitDefinition("dram", UnitCategory.MASS, "Dram", "dr", setOf("dram", "dr"), { it * 0.0017718451953125 }, { it / 0.0017718451953125 }),
            UnitDefinition("oz", UnitCategory.MASS, "Ounce", "oz", setOf("oz", "ounce"), { it * 0.028349523125 }, { it / 0.028349523125 }),
            UnitDefinition("lb", UnitCategory.MASS, "Pound", "lb", setOf("lb", "pound"), { it * 0.45359237 }, { it / 0.45359237 }),
            UnitDefinition("st", UnitCategory.MASS, "Stone", "st", setOf("st", "stone"), { it * 6.35029318 }, { it / 6.35029318 }),
            UnitDefinition("cwt", UnitCategory.MASS, "Hundredweight", "cwt", setOf("cwt", "hundredweight"), { it * 45.359237 }, { it / 45.359237 }),
            UnitDefinition("us_ton", UnitCategory.MASS, "US ton", "ton", setOf("us ton", "ton"), { it * 907.18474 }, { it / 907.18474 }),
            UnitDefinition("imp_ton", UnitCategory.MASS, "Imperial ton", "ton imp", setOf("imp ton", "imperial ton", "tonne imp"), { it * 1016.0469088 }, { it / 1016.0469088 })
        )
        UnitCategory.TEMPERATURE -> listOf(
            UnitDefinition("c", UnitCategory.TEMPERATURE, "Celsius", "°C", setOf("c", "celsius", "°c", "deg c"), { it }, { it }, true),
            UnitDefinition("f", UnitCategory.TEMPERATURE, "Fahrenheit", "°F", setOf("f", "fahrenheit", "°f", "deg f"), { (it - 32.0) * 5.0 / 9.0 }, { (it * 9.0 / 5.0) + 32.0 }, true),
            UnitDefinition("k", UnitCategory.TEMPERATURE, "Kelvin", "K", setOf("k", "kelvin"), { it - 273.15 }, { it + 273.15 }, true),
            UnitDefinition("rankine", UnitCategory.TEMPERATURE, "Rankine", "°R", setOf("rankine", "°r", "deg r"), { (it - 491.67) * 5.0 / 9.0 }, { (it * 9.0 / 5.0) + 491.67 }, true),
            UnitDefinition("reaumur", UnitCategory.TEMPERATURE, "Réaumur", "°Ré", setOf("reaumur", "réaumur", "deg re"), { it * 5.0 / 4.0 }, { it * 4.0 / 5.0 }, true),
            UnitDefinition("romer", UnitCategory.TEMPERATURE, "Rømer", "°Rø", setOf("romer", "rømer", "deg ro"), { (it - 7.5) * 40.0 / 21.0 }, { it * 21.0 / 40.0 + 7.5 }, true),
            UnitDefinition("newton", UnitCategory.TEMPERATURE, "Newton", "°N", setOf("newton", "deg n"), { it * 100.0 / 33.0 }, { it * 33.0 / 100.0 }, true),
            UnitDefinition("delisle", UnitCategory.TEMPERATURE, "Delisle", "°D", setOf("delisle", "deg d"), { 100.0 - it * 2.0 / 3.0 }, { (100.0 - it) * 3.0 / 2.0 }, true)
        )
        UnitCategory.TIME -> listOf(
            UnitDefinition("ns", UnitCategory.TIME, "Nanosecond", "ns", setOf("ns", "nanosecond"), { it * 1e-9 }, { it / 1e-9 }),
            UnitDefinition("us", UnitCategory.TIME, "Microsecond", "µs", setOf("us", "μs", "microsecond"), { it * 1e-6 }, { it / 1e-6 }),
            UnitDefinition("ms", UnitCategory.TIME, "Millisecond", "ms", setOf("ms", "millisecond"), { it * 1e-3 }, { it / 1e-3 }),
            UnitDefinition("s", UnitCategory.TIME, "Second", "s", setOf("s", "second"), { it }, { it }),
            UnitDefinition("min", UnitCategory.TIME, "Minute", "min", setOf("min", "minute"), { it * 60.0 }, { it / 60.0 }),
            UnitDefinition("h", UnitCategory.TIME, "Hour", "h", setOf("h", "hour"), { it * 3600.0 }, { it / 3600.0 }),
            UnitDefinition("day", UnitCategory.TIME, "Day", "day", setOf("day", "d"), { it * 86400.0 }, { it / 86400.0 }),
            UnitDefinition("week", UnitCategory.TIME, "Week", "wk", setOf("week", "wk"), { it * 604800.0 }, { it / 604800.0 }),
            UnitDefinition("fortnight", UnitCategory.TIME, "Fortnight", "fortnight", setOf("fortnight", "fortnights"), { it * 1209600.0 }, { it / 1209600.0 }),
            UnitDefinition("month", UnitCategory.TIME, "Month (conventional approx. 30 days)", "mo", setOf("month", "mo"), { it * 2592000.0 }, { it / 2592000.0 }, description = "Conventional: 30 days"),
            UnitDefinition("year", UnitCategory.TIME, "Year (conventional approx. 365 days)", "yr", setOf("year", "yr"), { it * 31536000.0 }, { it / 31536000.0 }, description = "Conventional: 365 days"),
            UnitDefinition("decade", UnitCategory.TIME, "Decade", "dec", setOf("decade", "dec"), { it * 315360000.0 }, { it / 315360000.0 }),
            UnitDefinition("century", UnitCategory.TIME, "Century", "cent", setOf("century", "cent"), { it * 3153600000.0 }, { it / 3153600000.0 }),
            UnitDefinition("millennium", UnitCategory.TIME, "Millennium", "ky", setOf("millennium", "ky"), { it * 31536000000.0 }, { it / 31536000000.0 })
        )
        UnitCategory.SPEED -> listOf(
            UnitDefinition("m_s", UnitCategory.SPEED, "Meter/second", "m/s", setOf("m/s", "mps", "meter per second"), { it }, { it }),
            UnitDefinition("km_h", UnitCategory.SPEED, "Kilometer/hour", "km/h", setOf("km/h", "kph", "kilometer per hour"), { it * (1000.0 / 3600.0) }, { it / (1000.0 / 3600.0) }),
            UnitDefinition("mph", UnitCategory.SPEED, "Mile/hour", "mph", setOf("mph", "mile per hour"), { it * 0.44704 }, { it / 0.44704 }),
            UnitDefinition("mile_min", UnitCategory.SPEED, "Mile/minute", "mi/min", setOf("mile per minute", "mi/min"), { it * 26.8224 }, { it / 26.8224 }),
            UnitDefinition("ft_s", UnitCategory.SPEED, "Foot/second", "ft/s", setOf("ft/s", "fps", "foot per second"), { it * 0.3048 }, { it / 0.3048 }),
            UnitDefinition("ft_min", UnitCategory.SPEED, "Foot/minute", "ft/min", setOf("ft/min", "foot per minute"), { it * 0.00508 }, { it / 0.00508 }),
            UnitDefinition("kn", UnitCategory.SPEED, "Knot", "kn", setOf("kn", "knot"), { it * 0.5144444444444445 }, { it / 0.5144444444444445 }),
            UnitDefinition("mach", UnitCategory.SPEED, "Mach", "Ma", setOf("mach", "ma"), { it * 343.0 }, { it / 343.0 }, description = "Standard reference: 343 m/s (20°C, sea level)" )
        )
        UnitCategory.PRESSURE -> listOf(
            UnitDefinition("pa", UnitCategory.PRESSURE, "Pascal", "Pa", setOf("pa", "pascal"), { it }, { it }),
            UnitDefinition("hpa", UnitCategory.PRESSURE, "Hectopascal", "hPa", setOf("hpa", "hectopascal"), { it * 100.0 }, { it / 100.0 }),
            UnitDefinition("kpa", UnitCategory.PRESSURE, "Kilopascal", "kPa", setOf("kpa", "kilopascal"), { it * 1000.0 }, { it / 1000.0 }),
            UnitDefinition("mpa", UnitCategory.PRESSURE, "Megapascal", "MPa", setOf("mpa", "megapascal"), { it * 1e6 }, { it / 1e6 }),
            UnitDefinition("gpa", UnitCategory.PRESSURE, "Gigapascal", "GPa", setOf("gpa", "gigapascal"), { it * 1e9 }, { it / 1e9 }),
            UnitDefinition("bar", UnitCategory.PRESSURE, "Bar", "bar", setOf("bar"), { it * 100000.0 }, { it / 100000.0 }),
            UnitDefinition("mbar", UnitCategory.PRESSURE, "Millibar", "mbar", setOf("mbar", "millibar"), { it * 100.0 }, { it / 100.0 }),
            UnitDefinition("ubar", UnitCategory.PRESSURE, "Microbar", "µbar", setOf("ubar", "microbar", "µbar"), { it * 0.1 }, { it / 0.1 }),
            UnitDefinition("atm", UnitCategory.PRESSURE, "Standard atmosphere", "atm", setOf("atm", "standard atmosphere", "atmosphere"), { it * 101325.0 }, { it / 101325.0 }),
            UnitDefinition("at", UnitCategory.PRESSURE, "Technical atmosphere", "at", setOf("at", "technical atmosphere"), { it * 98066.5 }, { it / 98066.5 }),
            UnitDefinition("psi", UnitCategory.PRESSURE, "PSI", "psi", setOf("psi", "pounds per square inch"), { it * 6894.757293168 }, { it / 6894.757293168 }),
            UnitDefinition("torr", UnitCategory.PRESSURE, "Torr", "Torr", setOf("torr"), { it * 133.32236842105263 }, { it / 133.32236842105263 }),
            UnitDefinition("mmhg", UnitCategory.PRESSURE, "mmHg", "mmHg", setOf("mmhg", "mm hg"), { it * 133.32236842105263 }, { it / 133.32236842105263 }),
            UnitDefinition("cmhg", UnitCategory.PRESSURE, "Centimeter mercury", "cmHg", setOf("cmhg", "cm hg"), { it * 1333.2236842105263 }, { it / 1333.2236842105263 }),
            UnitDefinition("inhg", UnitCategory.PRESSURE, "Inch of mercury", "inHg", setOf("inhg", "in hg", "inch of mercury"), { it * 3386.388666666666 }, { it / 3386.388666666666 }),
            UnitDefinition("inw", UnitCategory.PRESSURE, "Inch of water", "inH₂O", setOf("inw", "inch of water", "in h2o"), { it * 249.0889083333333 }, { it / 249.0889083333333 }),
            UnitDefinition("dyne_cm2", UnitCategory.PRESSURE, "Dyne/cm²", "dyn/cm²", setOf("dyne per square centimeter", "dyn/cm2"), { it * 0.1 }, { it / 0.1 })
        )
        UnitCategory.ENERGY -> listOf(
            UnitDefinition("j", UnitCategory.ENERGY, "Joule", "J", setOf("j", "joule"), { it }, { it }),
            UnitDefinition("kj", UnitCategory.ENERGY, "Kilojoule", "kJ", setOf("kj", "kilojoule"), { it * 1000.0 }, { it / 1000.0 }),
            UnitDefinition("mj", UnitCategory.ENERGY, "Megajoule", "MJ", setOf("mj", "megajoule"), { it * 1e6 }, { it / 1e6 }),
            UnitDefinition("gj", UnitCategory.ENERGY, "Gigajoule", "GJ", setOf("gj", "gigajoule"), { it * 1e9 }, { it / 1e9 }),
            UnitDefinition("erg", UnitCategory.ENERGY, "Erg", "erg", setOf("erg"), { it * 1e-7 }, { it / 1e-7 }),
            UnitDefinition("cal", UnitCategory.ENERGY, "Calorie", "cal", setOf("cal", "calorie"), { it * 4.184 }, { it / 4.184 }),
            UnitDefinition("kcal", UnitCategory.ENERGY, "Kilocalorie", "kcal", setOf("kcal", "kilocalorie"), { it * 4184.0 }, { it / 4184.0 }),
            UnitDefinition("wh", UnitCategory.ENERGY, "Watt-hour", "Wh", setOf("wh", "watt hour"), { it * 3600.0 }, { it / 3600.0 }),
            UnitDefinition("kwh", UnitCategory.ENERGY, "Kilowatt-hour", "kWh", setOf("kwh", "kilowatt hour"), { it * 3600000.0 }, { it / 3600000.0 }),
            UnitDefinition("mwh", UnitCategory.ENERGY, "Megawatt-hour", "MWh", setOf("mwh", "megawatt hour"), { it * 3.6e9 }, { it / 3.6e9 }),
            UnitDefinition("gwh", UnitCategory.ENERGY, "Gigawatt-hour", "GWh", setOf("gwh", "gigawatt hour"), { it * 3.6e12 }, { it / 3.6e12 }),
            UnitDefinition("ev", UnitCategory.ENERGY, "Electronvolt", "eV", setOf("ev", "electronvolt"), { it * 1.602176634e-19 }, { it / 1.602176634e-19 }),
            UnitDefinition("kev", UnitCategory.ENERGY, "Kiloelectronvolt", "keV", setOf("kev", "kiloelectronvolt"), { it * 1.602176634e-16 }, { it / 1.602176634e-16 }),
            UnitDefinition("mev", UnitCategory.ENERGY, "Megaelectronvolt", "MeV", setOf("mev", "megaelectronvolt"), { it * 1.602176634e-13 }, { it / 1.602176634e-13 }),
            UnitDefinition("gev", UnitCategory.ENERGY, "Gigaelectronvolt", "GeV", setOf("gev", "gigaelectronvolt"), { it * 1.602176634e-10 }, { it / 1.602176634e-10 }),
            UnitDefinition("btu", UnitCategory.ENERGY, "BTU", "BTU", setOf("btu", "british thermal unit"), { it * 1055.05585262 }, { it / 1055.05585262 }),
            UnitDefinition("ftlb", UnitCategory.ENERGY, "Foot-pound", "ft·lbf", setOf("ftlb", "foot pound", "foot-pound"), { it * 1.3558179483314 }, { it / 1.3558179483314 }),
            UnitDefinition("therm", UnitCategory.ENERGY, "Therm", "thm", setOf("therm", "thm"), { it * 105480400.0 }, { it / 105480400.0 })
        )
        UnitCategory.POWER -> listOf(
            UnitDefinition("nw", UnitCategory.POWER, "Nanowatt", "nW", setOf("nw", "nanowatt"), { it * 1e-9 }, { it / 1e-9 }),
            UnitDefinition("uw", UnitCategory.POWER, "Microwatt", "µW", setOf("uw", "µw", "microwatt"), { it * 1e-6 }, { it / 1e-6 }),
            UnitDefinition("mwatt", UnitCategory.POWER, "Milliwatt", "mW", setOf("milliwatt", "mwatt"), { it * 1e-3 }, { it / 1e-3 }),
            UnitDefinition("w", UnitCategory.POWER, "Watt", "W", setOf("w", "watt"), { it }, { it }),
            UnitDefinition("kw", UnitCategory.POWER, "Kilowatt", "kW", setOf("kw", "kilowatt"), { it * 1000.0 }, { it / 1000.0 }),
            UnitDefinition("mw", UnitCategory.POWER, "Megawatt", "MW", setOf("mw", "megawatt"), { it * 1e6 }, { it / 1e6 }),
            UnitDefinition("gw", UnitCategory.POWER, "Gigawatt", "GW", setOf("gw", "gigawatt"), { it * 1e9 }, { it / 1e9 }),
            UnitDefinition("hp_metric", UnitCategory.POWER, "Metric horsepower", "PS", setOf("hp metric", "ps", "metric horsepower"), { it * 735.49875 }, { it / 735.49875 }),
            UnitDefinition("hp_mech", UnitCategory.POWER, "Mechanical horsepower", "hp", setOf("hp mech", "mechanical horsepower"), { it * 745.6998715822702 }, { it / 745.6998715822702 }),
            UnitDefinition("hp_elec", UnitCategory.POWER, "Electrical horsepower", "hpE", setOf("hp electrical", "electrical horsepower"), { it * 746.0 }, { it / 746.0 }),
            UnitDefinition("hp_boiler", UnitCategory.POWER, "Boiler horsepower", "hp boiler", setOf("boiler horsepower"), { it * 9809.5 }, { it / 9809.5 }),
            UnitDefinition("btu_h", UnitCategory.POWER, "BTU/hour", "BTU/h", setOf("btu/h", "btuh"), { it * 0.2930710701722222 }, { it / 0.2930710701722222 }),
            UnitDefinition("ft_lb_s", UnitCategory.POWER, "Foot-pound/second", "ft·lbf/s", setOf("ft lb/s", "foot pound second"), { it * 1.3558179483314 }, { it / 1.3558179483314 })
        )
        UnitCategory.FREQUENCY -> listOf(
            UnitDefinition("mhz", UnitCategory.FREQUENCY, "Millihertz", "mHz", setOf("mhz", "millihertz"), { it * 1e-3 }, { it / 1e-3 }),
            UnitDefinition("hz", UnitCategory.FREQUENCY, "Hertz", "Hz", setOf("hz", "hertz"), { it }, { it }),
            UnitDefinition("khz", UnitCategory.FREQUENCY, "Kilohertz", "kHz", setOf("khz", "kilohertz"), { it * 1e3 }, { it / 1e3 }),
            UnitDefinition("mhz2", UnitCategory.FREQUENCY, "Megahertz", "MHz", setOf("mhz", "megahertz"), { it * 1e6 }, { it / 1e6 }),
            UnitDefinition("ghz", UnitCategory.FREQUENCY, "Gigahertz", "GHz", setOf("ghz", "gigahertz"), { it * 1e9 }, { it / 1e9 }),
            UnitDefinition("thz", UnitCategory.FREQUENCY, "Terahertz", "THz", setOf("thz", "terahertz"), { it * 1e12 }, { it / 1e12 }),
            UnitDefinition("rpm", UnitCategory.FREQUENCY, "Revolutions per minute", "rpm", setOf("rpm", "revolutions per minute"), { it / 60.0 }, { it * 60.0 })
        )
        UnitCategory.ANGLE -> listOf(
            UnitDefinition("deg", UnitCategory.ANGLE, "Degree", "°", setOf("deg", "degree", "degrees", "°"), { Math.toRadians(it) }, { Math.toDegrees(it) }),
            UnitDefinition("rad", UnitCategory.ANGLE, "Radian", "rad", setOf("rad", "radian", "radians"), { it }, { it }),
            UnitDefinition("grad", UnitCategory.ANGLE, "Gradian", "grad", setOf("grad", "gradian"), { it * PI / 200.0 }, { it / (PI / 200.0) }),
            UnitDefinition("gon", UnitCategory.ANGLE, "Gon", "gon", setOf("gon", "gons"), { it * PI / 200.0 }, { it / (PI / 200.0) }),
            UnitDefinition("arcmin", UnitCategory.ANGLE, "Arcminute", "′", setOf("arcmin", "arcminute", "minute of arc"), { it * PI / 10800.0 }, { it / (PI / 10800.0) }),
            UnitDefinition("arcsec", UnitCategory.ANGLE, "Arcsecond", "″", setOf("arcsec", "arcsecond", "second of arc"), { it * PI / 648000.0 }, { it / (PI / 648000.0) }),
            UnitDefinition("mrad", UnitCategory.ANGLE, "Milliradian", "mrad", setOf("mrad", "milliradian"), { it * 1e-3 }, { it / 1e-3 }),
            UnitDefinition("rev", UnitCategory.ANGLE, "Revolution", "rev", setOf("rev", "revolution"), { it * 2.0 * PI }, { it / (2.0 * PI) }),
            UnitDefinition("turn", UnitCategory.ANGLE, "Turn", "turn", setOf("turn", "turns"), { it * 2.0 * PI }, { it / (2.0 * PI) })
        )
        UnitCategory.DATA -> listOf(
            UnitDefinition("bit", UnitCategory.DATA, "Bit", "bit", setOf("bit"), { it / 8.0 }, { it * 8.0 }),
            UnitDefinition("kbit", UnitCategory.DATA, "Kilobit", "kbit", setOf("kbit", "kilobit"), { it * 1_000.0 / 8.0 }, { it * 8.0 / 1_000.0 }),
            UnitDefinition("mbit", UnitCategory.DATA, "Megabit", "Mbit", setOf("mbit", "megabit"), { it * 1_000_000.0 / 8.0 }, { it * 8.0 / 1_000_000.0 }),
            UnitDefinition("gbit", UnitCategory.DATA, "Gigabit", "Gbit", setOf("gbit", "gigabit"), { it * 1_000_000_000.0 / 8.0 }, { it * 8.0 / 1_000_000_000.0 }),
            UnitDefinition("tbit", UnitCategory.DATA, "Terabit", "Tbit", setOf("tbit", "terabit"), { it * 1_000_000_000_000.0 / 8.0 }, { it * 8.0 / 1_000_000_000_000.0 }),
            UnitDefinition("pbit", UnitCategory.DATA, "Petabit", "Pbit", setOf("pbit", "petabit"), { it * 1_000_000_000_000_000.0 / 8.0 }, { it * 8.0 / 1_000_000_000_000_000.0 }),
            UnitDefinition("ebit", UnitCategory.DATA, "Exabit", "Ebit", setOf("ebit", "exabit"), { it * 1_000_000_000_000_000_000.0 / 8.0 }, { it * 8.0 / 1_000_000_000_000_000_000.0 }),
            UnitDefinition("byte", UnitCategory.DATA, "Byte", "B", setOf("byte", "b"), { it }, { it }),
            UnitDefinition("kbyte", UnitCategory.DATA, "Kilobyte", "kB", setOf("kbyte", "kilobyte", "kb"), { it * 1000.0 }, { it / 1000.0 }),
            UnitDefinition("mbyte", UnitCategory.DATA, "Megabyte", "MB", setOf("mbyte", "megabyte", "mb"), { it * 1_000_000.0 }, { it / 1_000_000.0 }),
            UnitDefinition("gbyte", UnitCategory.DATA, "Gigabyte", "GB", setOf("gbyte", "gigabyte", "gb"), { it * 1_000_000_000.0 }, { it / 1_000_000_000.0 }),
            UnitDefinition("tbyte", UnitCategory.DATA, "Terabyte", "TB", setOf("tbyte", "terabyte", "tb"), { it * 1_000_000_000_000.0 }, { it / 1_000_000_000_000.0 }),
            UnitDefinition("pbyte", UnitCategory.DATA, "Petabyte", "PB", setOf("pbyte", "petabyte", "pb"), { it * 1_000_000_000_000_000.0 }, { it / 1_000_000_000_000_000.0 }),
            UnitDefinition("ebyte", UnitCategory.DATA, "Exabyte", "EB", setOf("ebyte", "exabyte", "eb"), { it * 1_000_000_000_000_000_000.0 }, { it / 1_000_000_000_000_000_000.0 }),
            UnitDefinition("kib", UnitCategory.DATA, "Kibibyte", "KiB", setOf("kib", "kibibyte", "ki"), { it * 1024.0 }, { it / 1024.0 }),
            UnitDefinition("mib", UnitCategory.DATA, "Mebibyte", "MiB", setOf("mib", "mebibyte", "mi"), { it * 1024.0 * 1024.0 }, { it / (1024.0 * 1024.0) }),
            UnitDefinition("gib", UnitCategory.DATA, "Gibibyte", "GiB", setOf("gib", "gibibyte", "gi"), { it * 1024.0 * 1024.0 * 1024.0 }, { it / (1024.0 * 1024.0 * 1024.0) }),
            UnitDefinition("tib", UnitCategory.DATA, "Tebibyte", "TiB", setOf("tib", "tebibyte", "ti"), { it * 1024.0 * 1024.0 * 1024.0 * 1024.0 }, { it / (1024.0 * 1024.0 * 1024.0 * 1024.0) }),
            UnitDefinition("pib", UnitCategory.DATA, "Pebibyte", "PiB", setOf("pib", "pebibyte", "pi"), { it * Math.pow(1024.0, 5.0) }, { it / Math.pow(1024.0, 5.0) }),
            UnitDefinition("eib", UnitCategory.DATA, "Exbibyte", "EiB", setOf("eib", "exbibyte", "ei"), { it * Math.pow(1024.0, 6.0) }, { it / Math.pow(1024.0, 6.0) }),
            UnitDefinition("kibyte", UnitCategory.DATA, "Kibibyte", "KiB", setOf("kibyte", "kibibyte", "ki"), { it * 1024.0 }, { it / 1024.0 }),
            UnitDefinition("mibyte", UnitCategory.DATA, "Mebibyte", "MiB", setOf("mibyte", "mebibyte", "mi"), { it * 1024.0 * 1024.0 }, { it / (1024.0 * 1024.0) }),
            UnitDefinition("gibyte", UnitCategory.DATA, "Gibibyte", "GiB", setOf("gibyte", "gibibyte", "gi"), { it * 1024.0 * 1024.0 * 1024.0 }, { it / (1024.0 * 1024.0 * 1024.0) }),
            UnitDefinition("tibyte", UnitCategory.DATA, "Tebibyte", "TiB", setOf("tibyte", "tebibyte", "ti"), { it * Math.pow(1024.0, 4.0) }, { it / Math.pow(1024.0, 4.0) }),
            UnitDefinition("pibyte", UnitCategory.DATA, "Pebibyte", "PiB", setOf("pibyte", "pebibyte", "pi"), { it * Math.pow(1024.0, 5.0) }, { it / Math.pow(1024.0, 5.0) }),
            UnitDefinition("ebyte2", UnitCategory.DATA, "Exbibyte", "EiB", setOf("ebyte", "exbibyte", "ei"), { it * Math.pow(1024.0, 6.0) }, { it / Math.pow(1024.0, 6.0) })
        )
        UnitCategory.ILLUMINATION -> listOf(
            UnitDefinition("lux", UnitCategory.ILLUMINATION, "Lux", "lx", setOf("lux", "lx"), { it }, { it }),
            UnitDefinition("phot", UnitCategory.ILLUMINATION, "Phot", "ph", setOf("phot", "ph"), { it * 10000.0 }, { it / 10000.0 }),
            UnitDefinition("nox", UnitCategory.ILLUMINATION, "Nox", "nox", setOf("nox"), { it * 0.001 }, { it / 0.001 }),
            UnitDefinition("fc", UnitCategory.ILLUMINATION, "Foot-candle", "fc", setOf("foot candle", "fc"), { it * 10.763910416709722 }, { it / 10.763910416709722 })
        )
        UnitCategory.ELECTRICAL -> listOf(
            UnitDefinition("v", UnitCategory.ELECTRICAL, "Volt", "V", setOf("v", "volt"), { it }, { it }),
            UnitDefinition("mv", UnitCategory.ELECTRICAL, "Millivolt", "mV", setOf("mv", "millivolt"), { it * 1e-3 }, { it / 1e-3 }),
            UnitDefinition("uv", UnitCategory.ELECTRICAL, "Microvolt", "µV", setOf("uv", "microvolt", "μv"), { it * 1e-6 }, { it / 1e-6 }),
            UnitDefinition("kv", UnitCategory.ELECTRICAL, "Kilovolt", "kV", setOf("kv", "kilovolt"), { it * 1e3 }, { it / 1e3 }),
            UnitDefinition("a", UnitCategory.ELECTRICAL, "Ampere", "A", setOf("a", "ampere", "amp"), { it }, { it }),
            UnitDefinition("ma", UnitCategory.ELECTRICAL, "Milliampere", "mA", setOf("ma", "milliampere"), { it * 1e-3 }, { it / 1e-3 }),
            UnitDefinition("ua", UnitCategory.ELECTRICAL, "Microampere", "µA", setOf("ua", "microampere", "μa"), { it * 1e-6 }, { it / 1e-6 }),
            UnitDefinition("ohm", UnitCategory.ELECTRICAL, "Ohm", "Ω", setOf("ohm", "ω"), { it }, { it }),
            UnitDefinition("mohm", UnitCategory.ELECTRICAL, "Milliohm", "mΩ", setOf("mohm", "milliohm"), { it * 1e-3 }, { it / 1e-3 }),
            UnitDefinition("kohm", UnitCategory.ELECTRICAL, "Kiloohm", "kΩ", setOf("kohm", "kiloohm"), { it * 1e3 }, { it / 1e3 }),
            UnitDefinition("mohm2", UnitCategory.ELECTRICAL, "Megaohm", "MΩ", setOf("megaohm", "mohm2"), { it * 1e6 }, { it / 1e6 }),
            UnitDefinition("f", UnitCategory.ELECTRICAL, "Farad", "F", setOf("f", "farad"), { it }, { it }),
            UnitDefinition("uf", UnitCategory.ELECTRICAL, "Microfarad", "µF", setOf("uf", "microfarad"), { it * 1e-6 }, { it / 1e-6 }),
            UnitDefinition("nf", UnitCategory.ELECTRICAL, "Nanofarad", "nF", setOf("nf", "nanofarad"), { it * 1e-9 }, { it / 1e-9 }),
            UnitDefinition("pf", UnitCategory.ELECTRICAL, "Picofarad", "pF", setOf("pf", "picofarad"), { it * 1e-12 }, { it / 1e-12 }),
            UnitDefinition("c", UnitCategory.ELECTRICAL, "Coulomb", "C", setOf("c", "coulomb"), { it }, { it }),
            UnitDefinition("mc", UnitCategory.ELECTRICAL, "Millicoulomb", "mC", setOf("mc", "millicoulomb"), { it * 1e-3 }, { it / 1e-3 }),
            UnitDefinition("uc", UnitCategory.ELECTRICAL, "Microcoulomb", "µC", setOf("uc", "microcoulomb"), { it * 1e-6 }, { it / 1e-6 }),
            UnitDefinition("ah", UnitCategory.ELECTRICAL, "Ampere-hour", "Ah", setOf("ah", "ampere hour"), { it * 3600.0 }, { it / 3600.0 }),
            UnitDefinition("mah", UnitCategory.ELECTRICAL, "Milliampere-hour", "mAh", setOf("mah", "milliampere hour"), { it * 3.6 }, { it / 3.6 }),
            UnitDefinition("s", UnitCategory.ELECTRICAL, "Siemens", "S", setOf("s", "siemens"), { it }, { it }),
            UnitDefinition("msiemens", UnitCategory.ELECTRICAL, "Millisiemens", "mS", setOf("msiemens", "millisiemens"), { it * 1e-3 }, { it / 1e-3 }),
            UnitDefinition("usiemens", UnitCategory.ELECTRICAL, "Microsiemens", "µS", setOf("usiemens", "microsiemens"), { it * 1e-6 }, { it / 1e-6 })
        )
        UnitCategory.RADIOACTIVITY -> listOf(
            UnitDefinition("bq", UnitCategory.RADIOACTIVITY, "Becquerel", "Bq", setOf("bq", "becquerel"), { it }, { it }),
            UnitDefinition("kbq", UnitCategory.RADIOACTIVITY, "Kilobecquerel", "kBq", setOf("kbq", "kilobecquerel"), { it * 1e3 }, { it / 1e3 }),
            UnitDefinition("mbq", UnitCategory.RADIOACTIVITY, "Megabecquerel", "MBq", setOf("mbq", "megabecquerel"), { it * 1e6 }, { it / 1e6 }),
            UnitDefinition("gbq", UnitCategory.RADIOACTIVITY, "Gigabecquerel", "GBq", setOf("gbq", "gigabecquerel"), { it * 1e9 }, { it / 1e9 }),
            UnitDefinition("ci", UnitCategory.RADIOACTIVITY, "Curie", "Ci", setOf("ci", "curie"), { it * 3.7e10 }, { it / 3.7e10 }),
            UnitDefinition("gy", UnitCategory.RADIOACTIVITY, "Gray", "Gy", setOf("gy", "gray"), { it }, { it }),
            UnitDefinition("mgy", UnitCategory.RADIOACTIVITY, "Milligray", "mGy", setOf("mgy", "milligray"), { it * 1e-3 }, { it / 1e-3 }),
            UnitDefinition("sv", UnitCategory.RADIOACTIVITY, "Sievert", "Sv", setOf("sv", "sievert"), { it }, { it }),
            UnitDefinition("msv", UnitCategory.RADIOACTIVITY, "Millisievert", "mSv", setOf("msv", "millisievert"), { it * 1e-3 }, { it / 1e-3 }),
            UnitDefinition("usv", UnitCategory.RADIOACTIVITY, "Microsievert", "µSv", setOf("usv", "microsievert"), { it * 1e-6 }, { it / 1e-6 }),
            UnitDefinition("rad", UnitCategory.RADIOACTIVITY, "Rad", "rad", setOf("rad"), { it * 0.01 }, { it / 0.01 }),
            UnitDefinition("rem", UnitCategory.RADIOACTIVITY, "Rem", "rem", setOf("rem"), { it * 0.01 }, { it / 0.01 })
        )
        UnitCategory.TORQUE -> listOf(
            UnitDefinition("nm_torque", UnitCategory.TORQUE, "Newton-meter", "N·m", setOf("newton meter", "nm", "n m"), { it }, { it }),
            UnitDefinition("dyne_cm", UnitCategory.TORQUE, "Dyne-centimeter", "dyn·cm", setOf("dyne centimeter", "dyne-centimeter"), { it * 1e-7 }, { it / 1e-7 }),
            UnitDefinition("kgf_m", UnitCategory.TORQUE, "Kilogram-force meter", "kgf·m", setOf("kgf m", "kilogram force meter"), { it * 9.80665 }, { it / 9.80665 }),
            UnitDefinition("lbf_ft", UnitCategory.TORQUE, "Pound-force foot", "lbf·ft", setOf("lbf ft", "pound force foot"), { it * 1.3558179483314 }, { it / 1.3558179483314 }),
            UnitDefinition("lbf_in", UnitCategory.TORQUE, "Pound-force inch", "lbf·in", setOf("lbf in", "pound force inch"), { it * 0.112984829931 }, { it / 0.112984829931 })
        )
        UnitCategory.FORCE -> listOf(
            UnitDefinition("n", UnitCategory.FORCE, "Newton", "N", setOf("n", "newton"), { it }, { it }),
            UnitDefinition("kn", UnitCategory.FORCE, "Kilonewton", "kN", setOf("kn", "kilonewton"), { it * 1e3 }, { it / 1e3 }),
            UnitDefinition("mn", UnitCategory.FORCE, "Meganewton", "MN", setOf("mn", "meganewton"), { it * 1e6 }, { it / 1e6 }),
            UnitDefinition("dyne", UnitCategory.FORCE, "Dyne", "dyn", setOf("dyne", "dyn"), { it * 1e-5 }, { it / 1e-5 }),
            UnitDefinition("kgf", UnitCategory.FORCE, "Kilogram-force", "kgf", setOf("kgf", "kilogram force"), { it * 9.80665 }, { it / 9.80665 }),
            UnitDefinition("gf", UnitCategory.FORCE, "Gram-force", "gf", setOf("gf", "gram force"), { it * 0.00980665 }, { it / 0.00980665 }),
            UnitDefinition("lbf", UnitCategory.FORCE, "Pound-force", "lbf", setOf("lbf", "pound force"), { it * 4.4482216152605 }, { it / 4.4482216152605 })
        )
        UnitCategory.DENSITY -> listOf(
            UnitDefinition("kg_m3", UnitCategory.DENSITY, "Kilogram/m³", "kg/m³", setOf("kg/m3", "kilogram per cubic meter"), { it }, { it }),
            UnitDefinition("g_cm3", UnitCategory.DENSITY, "Gram/cm³", "g/cm³", setOf("g/cm3", "gram per cubic centimeter"), { it * 1000.0 }, { it / 1000.0 }),
            UnitDefinition("g_ml", UnitCategory.DENSITY, "Gram/mL", "g/mL", setOf("g/ml", "gram per milliliter"), { it * 1000.0 }, { it / 1000.0 }),
            UnitDefinition("g_l", UnitCategory.DENSITY, "Gram/L", "g/L", setOf("g/l", "gram per liter"), { it * 1.0 }, { it / 1.0 }),
            UnitDefinition("lb_ft3", UnitCategory.DENSITY, "Pound/ft³", "lb/ft³", setOf("lb/ft3", "pound per cubic foot"), { it * 16.01846337396 }, { it / 16.01846337396 }),
            UnitDefinition("lb_in3", UnitCategory.DENSITY, "Pound/in³", "lb/in³", setOf("lb/in3", "pound per cubic inch"), { it * 27679.90471038 }, { it / 27679.90471038 }),
            UnitDefinition("lb_gal", UnitCategory.DENSITY, "Pound/gallon", "lb/gal", setOf("lb/gal", "pound per gallon"), { it * 119.826427316 }, { it / 119.826427316 })
        )
        UnitCategory.VISCOSITY -> listOf(
            UnitDefinition("pa_s", UnitCategory.VISCOSITY, "Pascal-second", "Pa·s", setOf("pa s", "pascal second", "pascal-second"), { it }, { it }),
            UnitDefinition("mpa_s", UnitCategory.VISCOSITY, "Millipascal-second", "mPa·s", setOf("mpa s", "millipascal second"), { it * 1e-3 }, { it / 1e-3 }),
            UnitDefinition("poise", UnitCategory.VISCOSITY, "Poise", "P", setOf("poise", "p"), { it * 0.1 }, { it / 0.1 }),
            UnitDefinition("cpoise", UnitCategory.VISCOSITY, "Centipoise", "cP", setOf("centipoise", "cp"), { it * 0.001 }, { it / 0.001 }),
            UnitDefinition("m2_s", UnitCategory.VISCOSITY, "Square meter/second", "m²/s", setOf("m2/s", "square meter per second"), { it }, { it }),
            UnitDefinition("cm2_s", UnitCategory.VISCOSITY, "Square centimeter/second", "cm²/s", setOf("cm2/s", "square centimeter per second"), { it * 1e-4 }, { it / 1e-4 }),
            UnitDefinition("stokes", UnitCategory.VISCOSITY, "Stokes", "St", setOf("stokes", "st"), { it * 1e-4 }, { it / 1e-4 }),
            UnitDefinition("cstokes", UnitCategory.VISCOSITY, "Centistokes", "cSt", setOf("centistokes", "cst"), { it * 1e-6 }, { it / 1e-6 })
        )
        UnitCategory.MAGNETISM -> listOf(
            UnitDefinition("t", UnitCategory.MAGNETISM, "Tesla", "T", setOf("t", "tesla"), { it }, { it }),
            UnitDefinition("mt", UnitCategory.MAGNETISM, "Millitesla", "mT", setOf("mt", "millitesla"), { it * 1e-3 }, { it / 1e-3 }),
            UnitDefinition("ut", UnitCategory.MAGNETISM, "Microtesla", "µT", setOf("ut", "microtesla", "μt"), { it * 1e-6 }, { it / 1e-6 }),
            UnitDefinition("gauss", UnitCategory.MAGNETISM, "Gauss", "G", setOf("gauss", "g"), { it * 1e-4 }, { it / 1e-4 }),
            UnitDefinition("wb", UnitCategory.MAGNETISM, "Weber", "Wb", setOf("wb", "weber"), { it }, { it }),
            UnitDefinition("maxwell", UnitCategory.MAGNETISM, "Maxwell", "Mx", setOf("maxwell", "mx"), { it * 1e-8 }, { it / 1e-8 })
        )
        UnitCategory.RADIO -> listOf(
            UnitDefinition("dbm", UnitCategory.RADIO, "dBm", "dBm", setOf("dbm", "decibel milliwatt"), { 0.001 * Math.pow(10.0, it / 10.0) }, { 10.0 * kotlin.math.log10(it / 0.001) }, description = "0 dBm = 1 mW"),
            UnitDefinition("dbw", UnitCategory.RADIO, "dBW", "dBW", setOf("dbw", "decibel watt"), { Math.pow(10.0, it / 10.0) }, { 10.0 * kotlin.math.log10(it) }, description = "0 dBW = 1 W")
        )
        UnitCategory.INFORMATION_RATE -> listOf(
            UnitDefinition("bit_s", UnitCategory.INFORMATION_RATE, "bit/s", "bit/s", setOf("bit/s", "bits per second"), { it }, { it }),
            UnitDefinition("kbit_s", UnitCategory.INFORMATION_RATE, "kbit/s", "kbit/s", setOf("kbit/s", "kilobits per second"), { it * 1e3 }, { it / 1e3 }),
            UnitDefinition("mbit_s", UnitCategory.INFORMATION_RATE, "Mbit/s", "Mbit/s", setOf("mbit/s", "megabits per second"), { it * 1e6 }, { it / 1e6 }),
            UnitDefinition("gbit_s", UnitCategory.INFORMATION_RATE, "Gbit/s", "Gbit/s", setOf("gbit/s", "gigabits per second"), { it * 1e9 }, { it / 1e9 }),
            UnitDefinition("tbit_s", UnitCategory.INFORMATION_RATE, "Tbit/s", "Tbit/s", setOf("tbit/s", "terabits per second"), { it * 1e12 }, { it / 1e12 }),
            UnitDefinition("byte_s", UnitCategory.INFORMATION_RATE, "byte/s", "B/s", setOf("byte/s", "bytes per second"), { it * 8.0 }, { it / 8.0 }),
            UnitDefinition("kbyte_s", UnitCategory.INFORMATION_RATE, "kB/s", "kB/s", setOf("kbyte/s", "kilobytes per second"), { it * 8_000.0 }, { it / 8_000.0 }),
            UnitDefinition("mbyte_s", UnitCategory.INFORMATION_RATE, "MB/s", "MB/s", setOf("mbyte/s", "megabytes per second"), { it * 8_000_000.0 }, { it / 8_000_000.0 }),
            UnitDefinition("gbyte_s", UnitCategory.INFORMATION_RATE, "GB/s", "GB/s", setOf("gbyte/s", "gigabytes per second"), { it * 8_000_000_000.0 }, { it / 8_000_000_000.0 }),
            UnitDefinition("tbyte_s", UnitCategory.INFORMATION_RATE, "TB/s", "TB/s", setOf("tbyte/s", "terabytes per second"), { it * 8_000_000_000_000.0 }, { it / 8_000_000_000_000.0 })
        )
    }

    fun findUnit(category: UnitCategory, query: String): UnitDefinition? {
        val normalized = query.trim().lowercase()
        return unitsFor(category).firstOrNull { unit ->
            unit.id.lowercase() == normalized ||
                unit.label.lowercase() == normalized ||
                unit.symbol.lowercase() == normalized ||
                unit.aliases.any { it.lowercase() == normalized }
        }
    }

    fun parseInput(raw: String, settings: CalculatorSettings = CalculatorSettings()): Double? {
        if (raw.isBlank()) return null
        val decimalSep = if (settings.decimalSeparator == DecimalSeparator.COMMA) "," else "."
        val groupingSep = if (settings.decimalSeparator == DecimalSeparator.COMMA) "." else ","
        
        val sanitized = raw.trim()
            .replace("−", "-")
            .replace(groupingSep, "")
            .replace(decimalSep, ".")
        
        if (sanitized.any { it.isLetter() && it.lowercaseChar() !in setOf('e') }) return null
        val valid = sanitized.matches(Regex("^-?(?:\\d+(?:\\.\\d*)?|\\.\\d+)(?:[eE][-+]?\\d+)?$"))
        if (!valid) return null
        return sanitized.toDoubleOrNull()
    }

    fun convert(value: Double, from: UnitDefinition, to: UnitDefinition): ConversionResult {
        if (from == to) {
            return ConversionResult(value)
        }

        if (from.category != to.category) {
            val powerPair = setOf(from.category, to.category) == setOf(UnitCategory.RADIO, UnitCategory.POWER)
            if (!powerPair) {
                return ConversionResult(null, "Cannot convert across categories")
            }

            val watts = when (from.id) {
                "dbm" -> 0.001 * Math.pow(10.0, value / 10.0)
                "dbw" -> Math.pow(10.0, value / 10.0)
                "w" -> value
                else -> return ConversionResult(null, "Cannot convert across categories")
            }

            val result = when (to.id) {
                "dbm" -> 10.0 * kotlin.math.log10(watts / 0.001)
                "dbw" -> 10.0 * kotlin.math.log10(watts)
                "w" -> watts
                else -> return ConversionResult(null, "Cannot convert across categories")
            }
            if (!result.isFinite()) return ConversionResult(null, "Result too large")
            return ConversionResult(result)
        }

        if (from.isTemperature || to.isTemperature) {
            val toCelsius: (String, Double) -> Double = { unitId, unitValue ->
                when (unitId) {
                    "c" -> unitValue
                    "f" -> (unitValue - 32.0) * 5.0 / 9.0
                    "k" -> unitValue - 273.15
                    "rankine" -> (unitValue - 491.67) * 5.0 / 9.0
                    "reaumur" -> unitValue * 5.0 / 4.0
                    "romer" -> (unitValue - 7.5) * 40.0 / 21.0
                    "newton" -> unitValue * 100.0 / 33.0
                    "delisle" -> 100.0 - unitValue * 2.0 / 3.0
                    else -> unitValue
                }
            }
            val fromC = toCelsius(from.id, value)
            val result = when (to.id) {
                "c" -> fromC
                "f" -> fromC * 9.0 / 5.0 + 32.0
                "k" -> fromC + 273.15
                "rankine" -> (fromC + 273.15) * 9.0 / 5.0
                "reaumur" -> fromC * 4.0 / 5.0
                "romer" -> fromC * 21.0 / 40.0 + 7.5
                "newton" -> fromC * 33.0 / 100.0
                "delisle" -> (100.0 - fromC) * 3.0 / 2.0
                else -> fromC
            }
            if (from.id == "k" && value < 0.0) return ConversionResult(null, "Kelvin must be >= 0")
            if (to.id == "k" && result < 0.0) return ConversionResult(null, "Kelvin must be >= 0")
            if (!result.isFinite()) return ConversionResult(null, "Invalid temperature conversion")
            return ConversionResult(result)
        }

        if (value.isNaN() || value.isInfinite()) {
            return ConversionResult(null, "Invalid value")
        }

        val baseValue = from.toBase(value)
        val converted = to.fromBase(baseValue)
        if (!converted.isFinite()) return ConversionResult(null, "Result too large")
        return ConversionResult(converted)
    }

    fun formatNumber(value: Double): String {
        if (!value.isFinite()) return "—"
        val absValue = abs(value)
        
        // We always return a normalized (dot-decimal, no grouping) string here.
        // The UI will handle formatting for display.
        if (absValue >= 1_000_000_000.0 || (absValue in 0.0000001..0.0001 && absValue != 0.0)) {
            return "%.10e".format(Locale.US, value).replace("e+0", "e").replace("e+", "e")
        }

        val rounded = kotlin.math.round(value * 1_000_000_000.0) / 1_000_000_000.0
        return BigDecimal.valueOf(rounded).stripTrailingZeros().toPlainString()
    }
}
