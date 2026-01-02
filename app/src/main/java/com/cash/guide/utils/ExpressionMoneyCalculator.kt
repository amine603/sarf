package com.cash.guide.utils

import java.math.BigDecimal
import java.math.MathContext
import java.math.RoundingMode

/**
 * Evaluates a simple arithmetic expression containing money numbers (MAD) into cents (MAD * 100).
 *
 * Examples:
 * - "12+5.5+3"  -> 2050 cents
 * - "2*10.5"    -> 2100 cents
 *
 * Supported:
 * - numbers with '.' or ',' decimals (0-2 decimals)
 * - operators: +, -, *, / (also × and ÷)
 * - parentheses: ( )
 */
object ExpressionMoneyCalculator {
    private val mc = MathContext(24, RoundingMode.HALF_UP)

    fun evaluateToCents(expression: String): Result<Int> {
        val tokens = tokenize(expression).getOrElse { return Result.failure(it) }
        val rpn = toRpn(tokens).getOrElse { return Result.failure(it) }
        val value = evalRpn(rpn).getOrElse { return Result.failure(it) }

        // Round to 2 decimals at the end.
        val scaled = value.setScale(2, RoundingMode.HALF_UP)
        val cents = scaled.movePointRight(2).setScale(0, RoundingMode.HALF_UP)

        return runCatching { cents.intValueExact() }
            .recoverCatching { throw IllegalArgumentException("Result is too large") }
    }

    private sealed class Token {
        data class Number(val value: BigDecimal) : Token()
        data class Op(val op: Char) : Token() // + - * / and unary '~'
        data object LParen : Token()
        data object RParen : Token()
    }

    private fun tokenize(expression: String): Result<List<Token>> = runCatching {
        val s = expression.trim()
        if (s.isEmpty()) throw IllegalArgumentException("Calculator input is empty")

        val out = ArrayList<Token>()
        var i = 0

        fun peekNonSpace(start: Int): Int {
            var j = start
            while (j < s.length && s[j].isWhitespace()) j++
            return j
        }

        while (i < s.length) {
            i = peekNonSpace(i)
            if (i >= s.length) break

            val c = s[i]
            when {
                c.isDigit() -> {
                    val start = i
                    var seenSep = false
                    var fracCount = 0
                    i++
                    while (i < s.length) {
                        val ch = s[i]
                        if (ch.isDigit()) {
                            if (seenSep) fracCount++
                            i++
                            continue
                        }
                        if ((ch == '.' || ch == ',') && !seenSep) {
                            seenSep = true
                            i++
                            continue
                        }
                        break
                    }
                    val raw = s.substring(start, i)
                    val cents = MoneyInputParser.parseToCents(raw)
                        .getOrElse { throw IllegalArgumentException("Invalid number: '$raw'") }

                    out.add(Token.Number(BigDecimal(cents).movePointLeft(2)))
                }
                c == '(' -> {
                    out.add(Token.LParen)
                    i++
                }
                c == ')' -> {
                    out.add(Token.RParen)
                    i++
                }
                c == '+' || c == '-' || c == '*' || c == '/' -> {
                    // Support unary +/- (e.g. "-(2+3)" or "(-5+2)")
                    val prev = out.lastOrNull()
                    val isUnary = prev == null || prev is Token.Op || prev == Token.LParen
                    if (isUnary && (c == '+' || c == '-')) {
                        if (c == '-') out.add(Token.Op('~')) // unary minus
                        // unary plus is a no-op
                    } else {
                        out.add(Token.Op(c))
                    }
                    i++
                }
                c == '×' -> {
                    out.add(Token.Op('*'))
                    i++
                }
                c == '÷' -> {
                    out.add(Token.Op('/'))
                    i++
                }
                c.isWhitespace() -> i++
                else -> throw IllegalArgumentException("Unexpected character: '$c'")
            }
        }

        if (out.isEmpty()) throw IllegalArgumentException("Calculator input is empty")

        // Insert implicit multiplication, e.g. "2(3+4)" or "(2+3)4"
        insertImplicitMultiplication(out)
    }

    private fun precedence(op: Char): Int = when (op) {
        '~' -> 3 // unary minus
        '*', '/' -> 2
        '+', '-' -> 1
        else -> 0
    }

    private fun isRightAssociative(op: Char): Boolean = (op == '~')

    private fun toRpn(tokens: List<Token>): Result<List<Token>> = runCatching {
        val output = ArrayList<Token>()
        val stack = ArrayDeque<Token>()

        for (t in tokens) {
            when (t) {
                is Token.Number -> output.add(t)
                is Token.Op -> {
                    while (stack.isNotEmpty()) {
                        val top = stack.last()
                        if (top is Token.Op) {
                            val pop = if (isRightAssociative(t.op)) {
                                precedence(top.op) > precedence(t.op)
                            } else {
                                precedence(top.op) >= precedence(t.op)
                            }
                            if (pop) output.add(stack.removeLast()) else break
                        } else break
                    }
                    stack.addLast(t)
                }
                Token.LParen -> stack.addLast(t)
                Token.RParen -> {
                    var found = false
                    while (stack.isNotEmpty()) {
                        val top = stack.removeLast()
                        if (top == Token.LParen) {
                            found = true
                            break
                        }
                        output.add(top)
                    }
                    if (!found) throw IllegalArgumentException("Mismatched parentheses")
                }
            }
        }

        while (stack.isNotEmpty()) {
            val top = stack.removeLast()
            if (top == Token.LParen || top == Token.RParen) throw IllegalArgumentException("Mismatched parentheses")
            output.add(top)
        }

        output
    }

    private fun evalRpn(tokens: List<Token>): Result<BigDecimal> = runCatching {
        val stack = ArrayDeque<BigDecimal>()

        for (t in tokens) {
            when (t) {
                is Token.Number -> stack.addLast(t.value)
                is Token.Op -> {
                    if (t.op == '~') {
                        if (stack.isEmpty()) throw IllegalArgumentException("Invalid expression")
                        val a = stack.removeLast()
                        stack.addLast(a.negate(mc))
                    } else {
                        if (stack.size < 2) throw IllegalArgumentException("Invalid expression")
                        val b = stack.removeLast()
                        val a = stack.removeLast()
                        val res = when (t.op) {
                            '+' -> a.add(b, mc)
                            '-' -> a.subtract(b, mc)
                            '*' -> a.multiply(b, mc)
                            '/' -> {
                                if (b.compareTo(BigDecimal.ZERO) == 0) throw IllegalArgumentException("Division by zero")
                                a.divide(b, mc)
                            }
                            else -> throw IllegalArgumentException("Invalid operator")
                        }
                        stack.addLast(res)
                    }
                }
                Token.LParen, Token.RParen -> throw IllegalArgumentException("Invalid expression")
            }
        }

        if (stack.size != 1) throw IllegalArgumentException("Invalid expression")
        val v = stack.removeLast()
        if (v.signum() < 0) throw IllegalArgumentException("Result cannot be negative")
        v
    }

    private fun insertImplicitMultiplication(tokens: List<Token>): List<Token> {
        if (tokens.isEmpty()) return tokens
        val out = ArrayList<Token>(tokens.size + 4)
        for (idx in tokens.indices) {
            val cur = tokens[idx]
            out.add(cur)

            val next = tokens.getOrNull(idx + 1) ?: continue
            val leftCanMultiply = cur is Token.Number || cur == Token.RParen
            val rightCanMultiply = next is Token.Number || next == Token.LParen || (next is Token.Op && next.op == '~')
            if (leftCanMultiply && rightCanMultiply) {
                out.add(Token.Op('*'))
            }
        }
        return out
    }
}






