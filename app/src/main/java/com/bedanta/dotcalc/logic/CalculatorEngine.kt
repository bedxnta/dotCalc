package com.bedanta.dotcalc.logic

import java.math.BigDecimal
import java.math.RoundingMode
import java.util.Stack

/**
 * A minimalist calculator engine that handles basic operations, parentheses, and unary minus.
 */
class CalculatorEngine {

    fun calculate(expression: String): String {
        return try {
            val result = evaluate(expression)
            formatResult(result)
        } catch (e: Exception) {
            "Error"
        }
    }

    private fun evaluate(expression: String): BigDecimal {
        if (expression.isEmpty()) return BigDecimal.ZERO

        val tokens = tokenize(expression)
        val tokensWithImplicit = insertImplicitMultiplication(tokens)
        val rpn = toRPN(tokensWithImplicit)
        return evaluateRPN(rpn)
    }

    private fun insertImplicitMultiplication(tokens: List<String>): List<String> {
        val result = mutableListOf<String>()
        for (i in tokens.indices) {
            result.add(tokens[i])
            if (i < tokens.size - 1) {
                val current = tokens[i]
                val next = tokens[i + 1]

                val isCurrentOperand = current.last().isDigit() || current == ")" || current == "%"
                val isNextOperand = next.first().isDigit() || next == "("

                if (isCurrentOperand && isNextOperand) {
                    result.add("×")
                }
            }
        }
        return result
    }

    private fun tokenize(expression: String): List<String> {
        val tokens = mutableListOf<String>()
        var i = 0
        while (i < expression.length) {
            val c = expression[i]
            if (c.isDigit() || c == '.') {
                val start = i
                while (i < expression.length && (expression[i].isDigit() || expression[i] == '.')) {
                    i++
                }
                tokens.add(expression.substring(start, i))
            } else if (isOperator(c.toString()) || c == '(' || c == ')') {
                val op = c.toString()
                // Detect unary minus
                if (op == "−") {
                    val prevToken = tokens.lastOrNull()
                    if (prevToken == null || prevToken == "(" || isBinaryOperator(prevToken)) {
                        // This is a unary minus. We'll tokenize it as "UNARY_MINUS"
                        tokens.add("U−")
                    } else {
                        tokens.add(op)
                    }
                } else {
                    tokens.add(op)
                }
                i++
            } else {
                i++ // Skip whitespace or unknown
            }
        }
        return tokens
    }

    private fun isOperator(s: String) = s in listOf("+", "−", "×", "÷", "%")
    private fun isBinaryOperator(s: String) = s in listOf("+", "−", "×", "÷")

    private fun precedence(op: String): Int {
        return when (op) {
            "+", "−" -> 1
            "×", "÷" -> 2
            "U−" -> 3 // Unary minus has high precedence
            "%" -> 4  // Percentage suffix has even higher
            else -> 0
        }
    }

    private fun toRPN(tokens: List<String>): List<String> {
        val output = mutableListOf<String>()
        val operators = Stack<String>()

        for (token in tokens) {
            when {
                token == "(" -> operators.push(token)
                token == ")" -> {
                    while (operators.isNotEmpty() && operators.peek() != "(") {
                        output.add(operators.pop())
                    }
                    if (operators.isNotEmpty()) operators.pop() // Remove "("
                }
                isOperator(token) || token == "U−" -> {
                    while (operators.isNotEmpty() && precedence(operators.peek()) >= precedence(token)) {
                        output.add(operators.pop())
                    }
                    operators.push(token)
                }
                else -> output.add(token) // Number
            }
        }

        while (operators.isNotEmpty()) {
            output.add(operators.pop())
        }

        return output
    }

    private fun evaluateRPN(rpn: List<String>): BigDecimal {
        val stack = Stack<BigDecimal>()
        var lastWasPercent = false

        for (token in rpn) {
            when (token) {
                "U−" -> {
                    if (stack.isEmpty()) throw Exception("Invalid")
                    val a = stack.pop()
                    stack.push(a.negate())
                }
                "%" -> {
                    if (stack.isEmpty()) throw Exception("Invalid")
                    val a = stack.pop()
                    stack.push(a.divide(BigDecimal("100"), 10, RoundingMode.HALF_UP))
                    lastWasPercent = true
                }
                "+" -> {
                    val b = stack.pop(); val a = stack.pop()
                    val actualB = if (lastWasPercent) a.multiply(b) else b
                    stack.push(a.add(actualB))
                    lastWasPercent = false
                }
                "−" -> {
                    val b = stack.pop(); val a = stack.pop()
                    val actualB = if (lastWasPercent) a.multiply(b) else b
                    stack.push(a.subtract(actualB))
                    lastWasPercent = false
                }
                "×" -> {
                    val b = stack.pop(); val a = stack.pop()
                    stack.push(a.multiply(b))
                    lastWasPercent = false
                }
                "÷" -> {
                    val b = stack.pop(); val a = stack.pop()
                    if (b.compareTo(BigDecimal.ZERO) == 0) throw ArithmeticException("Div0")
                    stack.push(a.divide(b, 10, RoundingMode.HALF_UP))
                    lastWasPercent = false
                }
                else -> {
                    stack.push(BigDecimal(token))
                    lastWasPercent = false
                }
            }
        }

        return if (stack.isEmpty()) BigDecimal.ZERO else stack.pop()
    }

    private fun formatResult(result: BigDecimal): String {
        val plain = result.stripTrailingZeros().toPlainString()
        return if (plain.contains(".") && plain.length > 15) {
             result.setScale(8, RoundingMode.HALF_UP).stripTrailingZeros().toPlainString()
        } else {
            plain
        }
    }
}