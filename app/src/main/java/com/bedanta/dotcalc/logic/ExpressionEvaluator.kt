package com.bedanta.dotcalc.logic

import java.math.BigDecimal
import java.math.BigInteger
import java.math.MathContext
import java.math.RoundingMode
import java.util.*
import kotlin.math.*

enum class AngleMode { DEG, RAD }

/**
 * Robust Rational number implementation for commercial-grade arithmetic.
 */
data class Rational(val n: Long, val d: Long = 1) {
    init {
        require(d != 0L) { "Denominator cannot be zero" }
    }

    fun simplify(): Rational {
        val common = gcd(abs(n), abs(d))
        val sign = if (d < 0) -1 else 1
        return Rational(sign * n / common, abs(d) / common)
    }

    private fun gcd(a: Long, b: Long): Long = if (b == 0L) a else gcd(b, a % b)

    operator fun plus(other: Rational) = Rational(n * other.d + other.n * d, d * other.d).simplify()
    operator fun minus(other: Rational) = Rational(n * other.d - other.n * d, d * other.d).simplify()
    operator fun times(other: Rational) = Rational(n * other.n, d * other.d).simplify()
    operator fun div(other: Rational) = Rational(n * other.d, d * other.n).simplify()
    fun negate() = Rational(-n, d)
    fun isZero() = n == 0L
    fun toBigDecimal(mc: MathContext) = BigDecimal.valueOf(n).divide(BigDecimal.valueOf(d), mc)

    override fun toString(): String = if (d == 1L) "$n" else "$n/$d"
}

/**
 * Symbolic + Numerical hybrid representation.
 */
enum class EvaluationError(val message: String) {
    DIV_BY_ZERO("Error: Division by zero"),
    UNDEFINED_TRIG("Error: Undefined (tan is undefined at 90°)"),
    OUT_OF_DOMAIN("Error: Out of domain (asin only accepts -1 to 1)"),
    LOG_UNDEFINED("Error: Log undefined for zero or negative numbers"),
    SQRT_NEGATIVE("Error: Cannot take square root of a negative number"),
    UNMATCHED_PARENTHESES("Error: Unmatched parentheses"),
    INVALID_EXPRESSION("Error: Invalid expression"),
    RESULT_TOO_LARGE("Error: Result too large")
}

sealed class MathValue(val isPercentage: Boolean = false) {
    abstract val isExact: Boolean
    abstract fun toNumeric(mc: MathContext): BigDecimal
    abstract fun toExactString(): String?
    abstract fun toFormattedExactString(settings: CalculatorSettings?): String
    
    val numericValue: Double by lazy {
        try { toNumeric(MathContext(34)).toDouble() } catch (e: Exception) { Double.NaN }
    }

    object Undefined : MathValue() {
        override val isExact = false
        override fun toNumeric(mc: MathContext) = throw ArithmeticException("Undefined")
        override fun toExactString() = "Undefined"
        override fun toFormattedExactString(settings: CalculatorSettings?) = "Undefined"
    }

    data class Approximate(val value: BigDecimal, val isPercent: Boolean = false) : MathValue(isPercent) {
        override val isExact = false
        override fun toNumeric(mc: MathContext) = value
        override fun toExactString() = null
        override fun toFormattedExactString(settings: CalculatorSettings?) = SettingsManager.formatDecimal(value, settings ?: CalculatorSettings())
    }

    data class Exact(
        val rational: Rational = Rational(0),
        val radical: Pair<Rational, Long>? = null,
        val piCoeff: Rational = Rational(0),
        val eCoeff: Rational = Rational(0),
        val isPercent: Boolean = false
    ) : MathValue(isPercent) {
        override val isExact = true
        override fun toNumeric(mc: MathContext): BigDecimal {
            var result = rational.toBigDecimal(mc)
            radical?.let {
                val sqrtVal = BigDecimal(sqrt(it.second.toDouble()).toString(), mc)
                result = result.add(it.first.toBigDecimal(mc).multiply(sqrtVal, mc), mc)
            }
            if (!piCoeff.isZero()) {
                val pi = BigDecimal("3.141592653589793238462643383279503")
                result = result.add(piCoeff.toBigDecimal(mc).multiply(pi, mc), mc)
            }
            if (!eCoeff.isZero()) {
                val e = BigDecimal("2.718281828459045235360287471352662")
                result = result.add(eCoeff.toBigDecimal(mc).multiply(e, mc), mc)
            }
            return result
        }

        private fun formatRational(r: Rational, settings: CalculatorSettings?): String {
            if (settings == null) return r.toString()
            if (r.d == 1L) return SettingsManager.formatDecimal(BigDecimal.valueOf(r.n), settings)
            val nStr = SettingsManager.formatDecimal(BigDecimal.valueOf(r.n), settings)
            val dStr = SettingsManager.formatDecimal(BigDecimal.valueOf(r.d), settings)
            return "$nStr/$dStr"
        }

        private fun formatRadicalTerm(coeff: Rational, root: Long, settings: CalculatorSettings?): String {
            if (root == 1L) return formatRational(coeff, settings)
            val n = coeff.n
            val d = coeff.d
            val nStr = if (abs(n) == 1L) (if (n < 0) "−" else "") else SettingsManager.formatDecimal(BigDecimal.valueOf(n), settings ?: CalculatorSettings())
            val dSuffix = if (d == 1L) "" else "/${SettingsManager.formatDecimal(BigDecimal.valueOf(d), settings ?: CalculatorSettings())}"
            
            return when {
                n == 0L -> "0"
                abs(n) == 1L && d == 1L -> "${if (n < 0) "−" else ""}√${root}"
                else -> "${nStr}√${root}$dSuffix"
            }
        }

        override fun toExactString(): String? = toFormattedExactString(null)

        override fun toFormattedExactString(settings: CalculatorSettings?): String {
            val parts = mutableListOf<String>()
            if (!rational.isZero() || (radical == null && piCoeff.isZero() && eCoeff.isZero())) {
                parts.add(formatRational(rational, settings))
            }
            radical?.let {
                val s = formatRadicalTerm(it.first, it.second, settings)
                parts.add(if (parts.isNotEmpty() && it.first.n > 0) "+$s" else s)
            }
            if (!piCoeff.isZero()) {
                val s = when {
                    piCoeff.n == 1L && piCoeff.d == 1L -> "π"
                    piCoeff.n == -1L && piCoeff.d == 1L -> "−π"
                    else -> "${formatRational(piCoeff, settings)}π"
                }
                parts.add(if (parts.isNotEmpty() && piCoeff.n > 0) "+$s" else s)
            }
            if (!eCoeff.isZero()) {
                val s = when {
                    eCoeff.n == 1L && eCoeff.d == 1L -> "e"
                    eCoeff.n == -1L && eCoeff.d == 1L -> "−e"
                    else -> "${formatRational(eCoeff, settings)}e"
                }
                parts.add(if (parts.isNotEmpty() && eCoeff.n > 0) "+$s" else s)
            }
            return if (parts.isEmpty()) "0" else parts.joinToString("")
        }

        fun isZero() = rational.isZero() && radical == null && piCoeff.isZero() && eCoeff.isZero()

        /**
         * Returns true if this value is a pure rational (no radicals or transcendental constants).
         */
        fun isPureRational() = radical == null && piCoeff.isZero() && eCoeff.isZero()
    }
}

data class DetailedResult(
    val expression: String,
    val decimal: String,
    val scientific: String,
    val engineering: String,
    val fraction: String? = null,
    val mixedFraction: String? = null,
    val exactDisplay: String? = null,
    val isExact: Boolean = false
)

class ExpressionEvaluator {
    private val mc = MathContext(34, RoundingMode.HALF_UP)

    fun evaluate(expression: String, angleMode: AngleMode, appMode: AppMode = AppMode.BASIC, settings: CalculatorSettings = CalculatorSettings()): MathValue {
        if (expression.isEmpty()) return MathValue.Exact()
        return try {
            val tokens = tokenize(expression, settings)
            val rpn = toRPN(tokens)
            evaluateRPN(rpn, angleMode, appMode)
        } catch (e: Exception) {
            MathValue.Undefined
        }
    }

    private fun normalizeOperatorToken(c: Char): String = when (c) {
        '-', '−' -> "−"
        '*', '×' -> "×"
        '/', '÷' -> "÷"
        else -> c.toString()
    }

    private fun needsImplicitMultiplication(left: String?, right: String): Boolean {
        if (left == null) return false
        val leftIsValue = left.first().isDigit() || left == ")" || left == "π" || left == "e" || left == "%" || left.endsWith(")")
        val rightIsValue = right.first().isDigit() || right == "(" || right == "π" || right == "e" || right == "√" || right.startsWith("sin") || right.startsWith("cos") || right.startsWith("tan") || right.startsWith("ln") || right.startsWith("log") || right.startsWith("sin⁻¹") || right.startsWith("cos⁻¹") || right.startsWith("tan⁻¹")
        return leftIsValue && rightIsValue
    }

    private fun tokenize(expr: String, settings: CalculatorSettings): List<String> {
        val tokens = mutableListOf<String>()
        var i = 0
        val s = SettingsManager.normalizeInput(expr, settings)

        while (i < s.length) {
            val c = s[i]
            when {
                c.isDigit() || c == '.' -> {
                    val start = i
                    while (i < s.length && (s[i].isDigit() || s[i] == '.')) i++
                    tokens.add(s.substring(start, i))
                }
                c in "+−-×*÷/^%!()" -> {
                    val op = normalizeOperatorToken(c)
                    if (op == "−") {
                        val prev = tokens.lastOrNull()
                        if (prev == null || prev == "(" || isBinaryOperator(prev)) {
                            tokens.add("U−")
                        } else tokens.add(op)
                    } else tokens.add(op)
                    i++
                }
                c.isLetter() -> {
                    val start = i
                    while (i < s.length && (s[i].isLetter() || s[i].isDigit() || s[i] == '⁻' || s[i] == '¹' || s[i] == 'ˣ')) i++
                    tokens.add(s.substring(start, i))
                }
                c == 'π' || c == 'e' || c == '√' -> {
                    tokens.add(c.toString())
                    i++
                }
                else -> i++
            }
        }

        val implicitTokens = mutableListOf<String>()
        for (idx in tokens.indices) {
            val token = tokens[idx]
            if (idx > 0 && needsImplicitMultiplication(implicitTokens.lastOrNull(), token)) {
                implicitTokens.add("×")
            }
            implicitTokens.add(token)
        }
        return implicitTokens
    }

    private fun isBinaryOperator(s: String) = s in listOf("+", "−", "×", "÷", "^")
    private fun isFunction(s: String) = s in listOf("sin", "cos", "tan", "sin⁻¹", "cos⁻¹", "tan⁻¹", "ln", "log", "√", "eˣ", "10ˣ")

    private fun precedence(op: String): Int = when (op) {
        "+", "−" -> 1
        "×", "÷" -> 2
        "U−" -> 3
        "^" -> 4
        "sin", "cos", "tan", "sin⁻¹", "cos⁻¹", "tan⁻¹", "ln", "log", "√", "eˣ", "10ˣ" -> 5
        "!", "%" -> 6
        else -> 0
    }

    private fun toRPN(tokens: List<String>): List<String> {
        val output = mutableListOf<String>()
        val stack = Stack<String>()
        for (token in tokens) {
            when {
                token.first().isDigit() || token == "π" || token == "e" -> output.add(token)
                token == "(" -> stack.push(token)
                token == ")" -> {
                    while (stack.isNotEmpty() && stack.peek() != "(") output.add(stack.pop())
                    if (stack.isNotEmpty()) stack.pop()
                }
                isFunction(token) -> stack.push(token)
                else -> {
                    while (stack.isNotEmpty() && precedence(stack.peek()) >= precedence(token) && token != "^") {
                        output.add(stack.pop())
                    }
                    stack.push(token)
                }
            }
        }
        while (stack.isNotEmpty()) output.add(stack.pop())
        return output
    }

    private fun evaluateRPN(rpn: List<String>, angleMode: AngleMode, appMode: AppMode): MathValue {
        val stack = Stack<MathValue>()
        for (token in rpn) {
            when {
                token.first().isDigit() || token == "." -> {
                    if (!token.contains(".")) stack.push(MathValue.Exact(rational = Rational(token.toLong())))
                    else stack.push(MathValue.Approximate(BigDecimal(token)))
                }
                token == "π" -> stack.push(MathValue.Exact(piCoeff = Rational(1)))
                token == "e" -> stack.push(MathValue.Exact(eCoeff = Rational(1)))
                token == "U−" -> stack.push(negate(stack.pop()))
                token == "!" -> stack.push(factorial(stack.pop()))
                token == "%" -> stack.push(percent(stack.pop(), appMode))
                isBinaryOperator(token) -> {
                    val b = stack.pop()
                    val a = stack.pop()
                    stack.push(applyBinaryOp(token, a, b, appMode))
                }
                isFunction(token) -> stack.push(applyFunction(token, stack.pop(), angleMode))
            }
        }
        return if (stack.isEmpty()) MathValue.Exact() else stack.pop()
    }

    private fun applyBinaryOp(op: String, a: MathValue, b: MathValue, appMode: AppMode): MathValue {
        if (a is MathValue.Undefined || b is MathValue.Undefined) return MathValue.Undefined

        val actualB = if (b.isPercentage && (op == "+" || op == "−")) {
            applyBinaryOp("×", a, b, appMode)
        } else {
            b
        }

        if (a is MathValue.Exact && actualB is MathValue.Exact) {
            val res = when (op) {
                "+" -> addExact(a, actualB)
                "−" -> addExact(a, negate(actualB) as MathValue.Exact)
                "×" -> multiplyExact(a, actualB)
                "÷" -> divideExact(a, actualB)
                "^" -> power(a, actualB)
                else -> MathValue.Undefined
            }
            if (appMode == AppMode.BASIC && res is MathValue.Exact && res.isPureRational() && res.rational.d != 1L) {
                return MathValue.Approximate(res.toNumeric(mc))
            }
            return res
        }
        val av = a.toNumeric(mc)
        val bv = actualB.toNumeric(mc)
        return try {
            MathValue.Approximate(when (op) {
                "+" -> av.add(bv)
                "−" -> av.subtract(bv)
                "×" -> av.multiply(bv)
                "÷" -> if (bv.signum() == 0) return MathValue.Undefined else av.divide(bv, mc)
                "^" -> {
                    val bDouble = bv.toDouble()
                    if (bDouble == bDouble.roundToInt().toDouble() && bDouble >= 0 && bDouble <= 999999999) {
                        av.pow(bDouble.toInt(), mc)
                    } else {
                        BigDecimal(av.toDouble().pow(bDouble).toString(), mc)
                    }
                }
                else -> return MathValue.Undefined
            })
        } catch (e: Exception) { MathValue.Undefined }
    }

    private fun addExact(a: MathValue.Exact, b: MathValue.Exact): MathValue {
        if (a.radical?.second == b.radical?.second || a.radical == null || b.radical == null) {
            val newRad = if (a.radical != null && b.radical != null) {
                if (a.radical.second == b.radical.second) Pair(a.radical.first + b.radical.first, a.radical.second)
                else null
            } else a.radical ?: b.radical
            
            if (a.radical != null && b.radical != null && a.radical.second != b.radical.second) {
                return MathValue.Approximate(a.toNumeric(mc).add(b.toNumeric(mc), mc))
            }
            return MathValue.Exact(a.rational + b.rational, newRad, a.piCoeff + b.piCoeff, a.eCoeff + b.eCoeff)
        }
        return MathValue.Approximate(a.toNumeric(mc).add(b.toNumeric(mc), mc))
    }

    private fun multiplyExact(a: MathValue.Exact, b: MathValue.Exact): MathValue {
        return when {
            b.radical == null && b.piCoeff.isZero() && b.eCoeff.isZero() -> {
                val r = b.rational
                MathValue.Exact(a.rational * r, a.radical?.let { Pair(it.first * r, it.second) }, a.piCoeff * r, a.eCoeff * r)
            }
            a.radical == null && a.piCoeff.isZero() && a.eCoeff.isZero() -> {
                val r = a.rational
                MathValue.Exact(b.rational * r, b.radical?.let { Pair(it.first * r, it.second) }, b.piCoeff * r, b.eCoeff * r)
            }
            else -> MathValue.Approximate(a.toNumeric(mc).multiply(b.toNumeric(mc), mc))
        }
    }

    private fun divideExact(a: MathValue.Exact, b: MathValue.Exact): MathValue {
        if (b.radical == null && b.piCoeff.isZero() && b.eCoeff.isZero()) {
            if (b.rational.isZero()) return MathValue.Undefined
            val r = b.rational
            return MathValue.Exact(a.rational / r, a.radical?.let { Pair(it.first / r, it.second) }, a.piCoeff / r, a.eCoeff / r)
        }
        return MathValue.Approximate(a.toNumeric(mc).divide(b.toNumeric(mc), mc))
    }

    private fun negate(a: MathValue): MathValue = when (a) {
        is MathValue.Exact -> MathValue.Exact(a.rational.negate(), a.radical?.let { Pair(it.first.negate(), it.second) }, a.piCoeff.negate(), a.eCoeff.negate())
        is MathValue.Approximate -> MathValue.Approximate(a.value.negate())
        else -> a
    }

    private fun factorial(a: MathValue): MathValue {
        val n = a.toNumeric(mc).toDouble()
        if (n < 0 || n != floor(n) || n > 100) return MathValue.Undefined
        var res = BigInteger.ONE
        for (i in 1..n.toInt()) res = res.multiply(BigInteger.valueOf(i.toLong()))
        return MathValue.Approximate(BigDecimal(res, mc))
    }

    private fun percent(a: MathValue, appMode: AppMode): MathValue {
        val res = if (a is MathValue.Exact) {
            val hundred = Rational(100)
            MathValue.Exact(a.rational / hundred, a.radical?.let { Pair(it.first / hundred, it.second) }, a.piCoeff / hundred, a.eCoeff / hundred, isPercent = true)
        } else {
            MathValue.Approximate(a.toNumeric(mc).divide(BigDecimal("100"), mc), isPercent = true)
        }
        if (appMode == AppMode.BASIC && res is MathValue.Exact && res.isPureRational() && res.rational.d != 1L) {
            return MathValue.Approximate(res.toNumeric(mc), isPercent = true)
        }
        return res
    }

    private fun validateExactTrigResult(expected: Double, candidate: MathValue.Exact): Boolean {
        return try {
            val actual = candidate.toNumeric(MathContext(34)).toDouble()
            abs(actual - expected) <= 1e-12
        } catch (e: Exception) {
            false
        }
    }

    private fun applyFunction(name: String, a: MathValue, mode: AngleMode): MathValue {
        if (a is MathValue.Undefined) return MathValue.Undefined
        val d = a.toNumeric(mc).toDouble()

        // Exact Trig Recognition
        if (name in listOf("sin", "cos", "tan") && a is MathValue.Exact && a.radical == null && a.eCoeff.isZero()) {
            val isPiMultiple = a.rational.isZero() && !a.piCoeff.isZero()
            val isPureExactAngle = a.piCoeff.isZero()
            if (!(isPiMultiple || isPureExactAngle)) return MathValue.Approximate(BigDecimal(when (name) {
                "sin" -> sin(if (mode == AngleMode.DEG) Math.toRadians(d) else d)
                "cos" -> cos(if (mode == AngleMode.DEG) Math.toRadians(d) else d)
                "tan" -> tan(if (mode == AngleMode.DEG) Math.toRadians(d) else d)
                else -> d
            }.toString(), mc))
            val deg = if (mode == AngleMode.DEG) d else Math.toDegrees(d)
            val angle = ((deg % 360 + 360) % 360).roundToInt()
            if (abs(deg - angle) < 1e-10) {
                val exactCandidate: MathValue? = when (name) {
                    "sin" -> when (angle) {
                        0, 180 -> MathValue.Exact(rational = Rational(0))
                        30, 150 -> MathValue.Exact(rational = Rational(1, 2))
                        90 -> MathValue.Exact(rational = Rational(1))
                        210, 330 -> MathValue.Exact(rational = Rational(-1, 2))
                        270 -> MathValue.Exact(rational = Rational(-1))
                        45, 135 -> MathValue.Exact(radical = Pair(Rational(1, 2), 2L))
                        60, 120 -> MathValue.Exact(radical = Pair(Rational(1, 2), 3L))
                        else -> null
                    }
                    "cos" -> when (angle) {
                        90, 270 -> MathValue.Exact(rational = Rational(0))
                        60, 300 -> MathValue.Exact(rational = Rational(1, 2))
                        0, 360 -> MathValue.Exact(rational = Rational(1))
                        120, 240 -> MathValue.Exact(rational = Rational(-1, 2))
                        180 -> MathValue.Exact(rational = Rational(-1))
                        45, 315 -> MathValue.Exact(radical = Pair(Rational(1, 2), 2L))
                        30, 330 -> MathValue.Exact(radical = Pair(Rational(1, 2), 3L))
                        else -> null
                    }
                    "tan" -> when (angle) {
                        0, 180 -> MathValue.Exact(rational = Rational(0))
                        45, 225 -> MathValue.Exact(rational = Rational(1))
                        135, 315 -> MathValue.Exact(rational = Rational(-1))
                        90, 270 -> MathValue.Undefined
                        else -> null
                    }
                    else -> null
                }
                if (exactCandidate != null) {
                    val expected = when (name) {
                        "sin" -> sin(Math.toRadians(angle.toDouble()))
                        "cos" -> cos(Math.toRadians(angle.toDouble()))
                        "tan" -> tan(Math.toRadians(angle.toDouble()))
                        else -> Double.NaN
                    }
                    if (exactCandidate is MathValue.Exact && validateExactTrigResult(expected, exactCandidate)) {
                        return exactCandidate
                    }
                    if (exactCandidate == MathValue.Undefined) return MathValue.Undefined
                }
            }
        }

        // Domain Checks
        if (name == "ln" || name == "log") if (d <= 0.0) return MathValue.Undefined
        if (name == "√") if (d < 0.0) return MathValue.Undefined
        if (name == "sin⁻¹" || name == "cos⁻¹") if (d < -1.0 || d > 1.0) return MathValue.Undefined

        // Symbolic Simplification
        if (name == "ln" && a is MathValue.Exact && a.rational.isZero() && a.radical == null && a.piCoeff.isZero() && a.eCoeff.n == 1L) return MathValue.Exact(Rational(1))
        if (name == "log" && a is MathValue.Exact && a.rational.n == 10L && a.radical == null) return MathValue.Exact(Rational(1))
        if (name == "√" && a is MathValue.Exact && a.radical == null && a.piCoeff.isZero() && a.eCoeff.isZero()) {
            val r = a.rational
            val sqN = sqrt(r.n.toDouble()).toLong()
            val sqD = sqrt(r.d.toDouble()).toLong()
            if (sqN * sqN == r.n && sqD * sqD == r.d) return MathValue.Exact(Rational(sqN, sqD))
        }

        return MathValue.Approximate(BigDecimal(when (name) {
            "sin" -> sin(if (mode == AngleMode.DEG) Math.toRadians(d) else d)
            "cos" -> cos(if (mode == AngleMode.DEG) Math.toRadians(d) else d)
            "tan" -> tan(if (mode == AngleMode.DEG) Math.toRadians(d) else d)
            "sin⁻¹" -> if (mode == AngleMode.DEG) Math.toDegrees(asin(d)) else asin(d)
            "cos⁻¹" -> if (mode == AngleMode.DEG) Math.toDegrees(acos(d)) else acos(d)
            "tan⁻¹" -> if (mode == AngleMode.DEG) Math.toDegrees(atan(d)) else atan(d)
            "ln" -> ln(d)
            "log" -> log10(d)
            "√" -> sqrt(d)
            "eˣ" -> exp(d)
            "10ˣ" -> 10.0.pow(d)
            else -> d
        }.toString(), mc))
    }

    private fun power(a: MathValue, b: MathValue): MathValue {
        val av = a.toNumeric(mc).toDouble()
        val bv = b.toNumeric(mc).toDouble()
        return MathValue.Approximate(BigDecimal(av.pow(bv).toString(), mc))
    }

    fun formatResult(value: MathValue, settings: CalculatorSettings = CalculatorSettings(), stripZeros: Boolean = true, isBaseMode: Boolean = false): String {
        if (value is MathValue.Undefined) return "Undefined"

        // Nuanced display policy: Exact Rational -> Fraction String, Else -> Numerical String
        if (value is MathValue.Exact && value.isPureRational()) {
            return value.toFormattedExactString(settings)
        }

        val num = try { value.toNumeric(mc) } catch (e: Exception) { return EvaluationError.INVALID_EXPRESSION.message }
        
        if (num.abs() > BigDecimal("1e15") || (num.abs() < BigDecimal("1e-10") && num.signum() != 0)) {
            return SettingsManager.formatScientific(num.toDouble(), settings)
        }

        return SettingsManager.formatDecimal(num, settings, stripZeros, isBaseMode)
    }

    fun getErrorMessage(expression: String, angleMode: AngleMode, appMode: AppMode = AppMode.BASIC, settings: CalculatorSettings = CalculatorSettings()): String {
        val decimalSep = if (settings.decimalSeparator == DecimalSeparator.COMMA) "," else "."
        val groupingSep = if (settings.decimalSeparator == DecimalSeparator.COMMA) "." else ","
        val normalized = expression.replace(" ", "").replace(groupingSep, "").replace(decimalSep, ".")
        
        if (normalized.isEmpty()) return EvaluationError.INVALID_EXPRESSION.message
        if (normalized.count { it == '(' } != normalized.count { it == ')' }) {
            return EvaluationError.UNMATCHED_PARENTHESES.message
        }
        if (normalized.contains("÷") || normalized.contains("/")) {
            val zeroDenominator = Regex("([0-9πe.]+|\\([^)]*\\)|[A-Za-z⁻¹²³⁴⁵⁶⁷⁸⁹()]+)\\s*[÷/]\\s*(0|0\\.[0-9]+|\\([^)]*\\)|[A-Za-z⁻¹²³⁴⁵⁶⁷⁸⁹()]+)")
            if (zeroDenominator.containsMatchIn(normalized)) {
                return EvaluationError.DIV_BY_ZERO.message
            }
        }

        val tanArg = extractFunctionArgument(normalized, "tan")
        if (tanArg != null) {
            val tanValue = evaluate(tanArg, angleMode, appMode, settings)
            val numeric = try { tanValue.toNumeric(MathContext(34)).toDouble() } catch (e: Exception) { Double.NaN }
            if (!numeric.isNaN() && !numeric.isInfinite()) {
                val angleDeg = if (angleMode == AngleMode.DEG) numeric else Math.toDegrees(numeric)
                val normalizedAngle = ((angleDeg % 360.0) + 360.0) % 360.0
                if (abs(normalizedAngle - 90.0) < 1e-9 || abs(normalizedAngle - 270.0) < 1e-9) {
                    return "Error: Undefined (tan is undefined at 90°)"
                }
            }
        }

        val inverseTrig = listOf("sin⁻¹", "cos⁻¹", "tan⁻¹")
        for (func in inverseTrig) {
            val arg = extractFunctionArgument(normalized, func) ?: continue
            val value = evaluate(arg, angleMode, appMode, settings)
            val numeric = try { value.toNumeric(MathContext(34)).toDouble() } catch (e: Exception) { Double.NaN }
            if (numeric.isNaN() || numeric.isInfinite()) continue
            if (numeric < -1.0 || numeric > 1.0) {
                return "Error: Out of domain (asin only accepts -1 to 1)"
            }
        }

        val logArg = extractFunctionArgument(normalized, "log")
        if (logArg != null) {
            val value = evaluate(logArg, angleMode, appMode, settings)
            val numeric = try { value.toNumeric(MathContext(34)).toDouble() } catch (e: Exception) { Double.NaN }
            if (numeric <= 0.0 && !numeric.isNaN()) {
                return EvaluationError.LOG_UNDEFINED.message
            }
        }

        val lnArg = extractFunctionArgument(normalized, "ln")
        if (lnArg != null) {
            val value = evaluate(lnArg, angleMode, appMode, settings)
            val numeric = try { value.toNumeric(MathContext(34)).toDouble() } catch (e: Exception) { Double.NaN }
            if (numeric <= 0.0 && !numeric.isNaN()) {
                return EvaluationError.LOG_UNDEFINED.message
            }
        }

        val sqrtArg = extractFunctionArgument(normalized, "√")
        if (sqrtArg != null) {
            val value = evaluate(sqrtArg, angleMode, appMode, settings)
            val numeric = try { value.toNumeric(MathContext(34)).toDouble() } catch (e: Exception) { Double.NaN }
            if (numeric < 0.0 && !numeric.isNaN()) {
                return EvaluationError.SQRT_NEGATIVE.message
            }
        }

        return EvaluationError.INVALID_EXPRESSION.message
    }

    private fun extractFunctionArgument(expression: String, functionName: String): String? {
        val pattern = Regex("${Regex.escape(functionName)}\\((.*)\\)")
        val match = pattern.find(expression)
        return match?.groupValues?.getOrNull(1)
    }

    fun getDetailedResult(expression: String, value: MathValue, settings: CalculatorSettings = CalculatorSettings()): DetailedResult {
        val num = try { value.toNumeric(mc) } catch (e: Exception) { BigDecimal.ZERO }
        
        // Exact fraction/mixed logic
        var fraction: String? = null
        var mixed: String? = null
        if (value is MathValue.Exact && value.radical == null && value.piCoeff.isZero() && value.eCoeff.isZero()) {
            fraction = value.rational.toString()
            val r = value.rational
            if (abs(r.n) > r.d && r.d != 1L) {
                val whole = r.n / r.d
                val rem = abs(r.n % r.d)
                mixed = "$whole $rem/${r.d}"
            }
        }

        return DetailedResult(
            expression = expression,
            decimal = formatResult(MathValue.Approximate(num), settings),
            scientific = SettingsManager.formatScientific(num.toDouble(), settings),
            engineering = SettingsManager.formatEngineering(num.toDouble(), settings),
            fraction = fraction,
            mixedFraction = mixed,
            exactDisplay = value.toFormattedExactString(settings),
            isExact = value.isExact
        )
    }
}
