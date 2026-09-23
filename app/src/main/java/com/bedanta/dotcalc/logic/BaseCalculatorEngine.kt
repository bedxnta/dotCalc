package com.bedanta.dotcalc.logic

import java.math.BigInteger
import kotlin.math.max

enum class NumBase { BIN, OCT, DEC, HEX }

data class BaseEvalResult(val value: BigInteger?, val error: String?)

class BaseCalculatorEngine {
    var base: NumBase = NumBase.DEC
    var bitWidth: Int = 32
    var signed: Boolean = false

    fun parseNumberLiteral(token: String, baseHint: NumBase? = null): BigInteger? {
        val t = token.trim()
        if (t.isEmpty()) return null
        var s = t
        var b = baseHint ?: base
        // prefixes
        if (s.startsWith("0b") || s.startsWith("0B")) { b = NumBase.BIN; s = s.substring(2) }
        else if (s.startsWith("0o") || s.startsWith("0O")) { b = NumBase.OCT; s = s.substring(2) }
        else if (s.startsWith("0x") || s.startsWith("0X")) { b = NumBase.HEX; s = s.substring(2) }
        if (s.isEmpty()) return null
        return try {
            val radix = when (b) {
                NumBase.BIN -> 2
                NumBase.OCT -> 8
                NumBase.DEC -> 10
                NumBase.HEX -> 16
            }
            BigInteger(s, radix)
        } catch (e: Exception) {
            // If parsing failed (e.g., token '2' in binary context), allow plain decimal digits to be parsed as decimal
            return try {
                if (s.matches(Regex("^\\d+$"))) BigInteger(s, 10) else null
            } catch (e2: Exception) { null }
        }
    }

    // Convert BigInteger to representations
    fun toBases(value: BigInteger): Map<NumBase, String> {
        val unsignedMask = if (bitWidth >= 1024) null else BigInteger.ONE.shiftLeft(bitWidth).subtract(BigInteger.ONE)
        val valForDisplay = if (signed && unsignedMask != null) {
            // interpret as two's complement if negative
            val maxVal = BigInteger.ONE.shiftLeft(bitWidth - 1)
            var v = value
            // if value is negative in BigInteger, we already have negative; otherwise if value has high bit set, interpret
            if (v.signum() >= 0 && v.bitLength() > 0 && v.testBit(bitWidth - 1)) {
                // convert from unsigned to signed two's complement
                v = v.subtract(BigInteger.ONE.shiftLeft(bitWidth))
            }
            v
        } else value

        return mapOf(
            NumBase.DEC to valForDisplay.toString(),
            NumBase.HEX to value.abs().toString(16).uppercase(),
            NumBase.OCT to value.abs().toString(8),
            NumBase.BIN to value.abs().toString(2)
        )
    }

    // Evaluate expression (infix) to BigInteger; supports + - * / % & | ^ << >> ~ unary
    fun evaluate(expression: String): BaseEvalResult {
        try {
            val tokens = tokenize(expression)
            println("DEBUG tokens for '$expression': $tokens")
            val rpn = shuntingYard(tokens)
            val result = evalRPN(rpn) ?: return BaseEvalResult(null, "Invalid expression")
            return BaseEvalResult(result, null)
        } catch (e: ArithmeticException) {
            println("BaseCalculatorEngine.evaluate arithmetic error for '$expression': ${e.message}")
            return BaseEvalResult(null, e.message ?: "Arithmetic error")
        } catch (e: Exception) {
            println("BaseCalculatorEngine.evaluate parse error for '$expression': ${e.message}")
            return BaseEvalResult(null, e.message ?: "Parse error")
        }
    }

    private fun tokenize(str: String): List<String> {
        val s = str.replace("(?i)XOR".toRegex(), " ^ ")
            .replace("(?i)AND".toRegex(), " & ")
            .replace("(?i)OR".toRegex(), " | ")
            .replace("(?i)NOT".toRegex(), " ~ ")
        val tokens = mutableListOf<String>()
        var i = 0
        while (i < s.length) {
            val c = s[i]
            when {
                c.isWhitespace() -> i++
                c.isLetterOrDigit() -> {
                    val sb = StringBuilder()
                    while (i < s.length && (s[i].isLetterOrDigit())) {
                        sb.append(s[i]); i++
                    }
                    val tok = sb.toString()
                    when (tok.uppercase()) {
                        "AND" -> tokens.add("&")
                        "OR" -> tokens.add("|")
                        "XOR" -> tokens.add("^")
                        "NOT" -> tokens.add("~")
                        else -> tokens.add(tok)
                    }
                }
                c == '0' && i + 1 < s.length && (s[i + 1].lowercaseChar() == 'x' || s[i + 1].lowercaseChar() == 'b' || s[i + 1].lowercaseChar() == 'o') -> {
                    // prefix
                    val sb = StringBuilder()
                    sb.append(s[i]); sb.append(s[i + 1]); i += 2
                    while (i < s.length && (s[i].isDigit() || s[i].isLetter())) { sb.append(s[i]); i++ }
                    tokens.add(sb.toString())
                }
                s.startsWith("<<", i) || s.startsWith(">>", i) -> { tokens.add(s.substring(i, i + 2)); i += 2 }
                c == '(' || c == ')' || c == '+' || c == '-' || c == '*' || c == '/' || c == '%' || c == '&' || c == '|' || c == '^' || c == '~' -> { tokens.add(c.toString()); i++ }
                else -> throw IllegalArgumentException("Invalid character: $c")
            }
        }
        return tokens
    }

    private fun shuntingYard(tokens: List<String>): List<String> {
        val output = mutableListOf<String>()
        val ops = ArrayDeque<String>()
        var prevIsOperatorOrLeftParen = true // treat start as operator to detect unary
        for (t in tokens) {
            when {
                isNumberToken(t) -> {
                    output.add(t)
                    prevIsOperatorOrLeftParen = false
                }
                t == "(" -> {
                    ops.addFirst(t)
                    prevIsOperatorOrLeftParen = true
                }
                t == ")" -> {
                    while (ops.isNotEmpty() && ops.first() != "(") output.add(ops.removeFirst())
                    if (ops.isEmpty() || ops.first() != "(") throw IllegalArgumentException("Mismatched parentheses")
                    ops.removeFirst()
                    prevIsOperatorOrLeftParen = false
                }
                isOperator(t) -> {
                    val unary = (t == "+" || t == "-" || t == "~") && prevIsOperatorOrLeftParen
                    val opToken = if (unary) "u$t" else t
                    val prec1 = precedence(t, unary)
                    val assocLeft = !isRightAssociative(t, unary)
                    while (ops.isNotEmpty() && isOperator(ops.first().removePrefix("u"))) {
                        val op2Raw = ops.first()
                        val isUnary2 = op2Raw.startsWith("u")
                        val op2 = if (isUnary2) op2Raw.substring(1) else op2Raw
                        val prec2 = precedence(op2, isUnary2)
                        if ((assocLeft && prec1 <= prec2) || (!assocLeft && prec1 < prec2)) {
                            output.add(ops.removeFirst())
                        } else break
                    }
                    ops.addFirst(opToken)
                    prevIsOperatorOrLeftParen = true
                }
                else -> throw IllegalArgumentException("Unknown token $t")
            }
        }
        while (ops.isNotEmpty()) {
            val o = ops.removeFirst()
            if (o == "(" || o == ")") throw IllegalArgumentException("Mismatched parentheses")
            output.add(o)
        }
        return output
    }

    private fun isNumberToken(t: String): Boolean {
        return t.matches(Regex("^0[bBoOxX][0-9A-Fa-f]+")) || t.matches(Regex("^[0-9A-Fa-f]+$"))
    }

    private fun isOperator(t: String): Boolean {
        val ops = setOf("+","-","*","/","%","&","|","^","~","<<",">>")
        return ops.contains(t)
    }

    private fun isUnaryOperator(op: String, prevOut: String?): Boolean {
        // consider unary if prev token is null or an operator or '('
        if (op != "+" && op != "-" && op != "~") return false
        return (prevOut == null || prevOut in listOf("+","-","*","/","%","&","|","^","<<",">>","("))
    }

    private fun precedence(op: String, unary: Boolean): Int {
        if (unary) return 8
        return when (op) {
            "~" -> 8
            "*", "/", "%" -> 7
            "+", "-" -> 6
            "<<", ">>" -> 5
            "&" -> 4
            "^" -> 3
            "|" -> 2
            else -> 1
        }
    }

    private fun isRightAssociative(op: String, unary: Boolean): Boolean {
        return unary
    }

    private fun evalRPN(rpn: List<String>): BigInteger? {
        val stack = ArrayDeque<BigInteger>()
        for (tk in rpn) {
            if (tk.startsWith("u")) {
                val op = tk.substring(1)
                val v = stack.removeFirstOrNull() ?: return null
                val res = when (op) {
                    "+" -> v
                    "-" -> v.negate()
                    "~" -> applyNot(v)
                    else -> throw IllegalArgumentException("Unknown unary $op")
                }
                stack.addFirst(res)
            } else if (isNumberToken(tk)) {
                val num = parseNumberLiteral(tk) ?: throw IllegalArgumentException("Invalid number $tk")
                stack.addFirst(num)
            } else if (isOperator(tk)) {
                val b = stack.removeFirstOrNull() ?: throw IllegalArgumentException("Missing operand")
                val a = stack.removeFirstOrNull() ?: throw IllegalArgumentException("Missing operand")
                val res = when (tk) {
                    "+" -> a.add(b)
                    "-" -> a.subtract(b)
                    "*" -> a.multiply(b)
                    "/" -> if (b == BigInteger.ZERO) throw ArithmeticException("Division by zero") else a.divide(b)
                    "%" -> if (b == BigInteger.ZERO) throw ArithmeticException("Division by zero") else a.remainder(b)
                    "&" -> a.and(b)
                    "|" -> a.or(b)
                    "^" -> a.xor(b)
                    "<<" -> if (b.signum() < 0) throw IllegalArgumentException("Negative shift count") else a.shiftLeft(b.toInt())
                    ">>" -> if (b.signum() < 0) throw IllegalArgumentException("Negative shift count") else applyRightShift(a, b.toInt())
                    else -> throw IllegalArgumentException("Unknown operator $tk")
                }
                stack.addFirst(res)
            } else {
                throw IllegalArgumentException("Unknown RPN token $tk")
            }
        }
        return stack.firstOrNull()
    }

    private fun applyNot(v: BigInteger): BigInteger {
        if (bitWidth <= 0) return v.not()
        val mask = BigInteger.ONE.shiftLeft(bitWidth).subtract(BigInteger.ONE)
        return v.xor(mask)
    }

    private fun applyRightShift(a: BigInteger, shift: Int): BigInteger {
        if (shift < 0) throw IllegalArgumentException("Negative shift")
        return if (signed) {
            // arithmetic shift: preserve sign
            if (a.signum() >= 0) a.shiftRight(shift) else {
                // two's complement behavior: get unsigned representation within width
                if (bitWidth <= 0) a.shiftRight(shift)
                else {
                    val mask = BigInteger.ONE.shiftLeft(bitWidth).subtract(BigInteger.ONE)
                    var unsigned = a.and(mask)
                    unsigned = unsigned.shiftRight(shift)
                    // interpret back as signed
                    if (unsigned.testBit(bitWidth - shift - 1)) unsigned = unsigned.subtract(BigInteger.ONE.shiftLeft(bitWidth - shift))
                    unsigned
                }
            }
        } else {
            // logical shift: zero fill
            if (bitWidth <= 0) a.shiftRight(shift)
            else {
                val mask = BigInteger.ONE.shiftLeft(bitWidth).subtract(BigInteger.ONE)
                var unsigned = a.and(mask)
                unsigned = unsigned.shiftRight(shift)
                unsigned
            }
        }
    }
}
