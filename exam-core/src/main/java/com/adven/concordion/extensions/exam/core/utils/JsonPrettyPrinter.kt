package com.adven.concordion.extensions.exam.core.utils

import com.adven.concordion.extensions.exam.core.utils.JsonPrettyPrinter.State.ARRAY
import com.adven.concordion.extensions.exam.core.utils.JsonPrettyPrinter.State.OBJECT
import com.adven.concordion.extensions.exam.core.utils.JsonPrettyPrinter.State.UNKNOWN
import java.util.Scanner
import java.util.Stack

private const val ESCAPE_CHARACTER = "\\"

class JsonPrettyPrinter {

    fun prettyPrint(json: String?): String {
        return if (json == null) "" else Parser().parse(json)
    }

    private enum class State {
        UNKNOWN, OBJECT, ARRAY
    }

    private class Parser {
        private var indent = 0
        private var newLine = false
        private val state = Stack<State>()
        private var token: String? = null

        @Suppress("ComplexMethod", "LoopWithTooManyJumpStatements")
        fun parse(json: String): String {
            state.push(UNKNOWN)
            Scanner(json).useDelimiter("").use { scanner ->
                return buildString {
                    while (scanner.hasToken()) {
                        if (scanner.nextTokenIs("{")) {
                            consumeToken()
                            if (!scanner.nextTokenIs("}")) {
                                growIndent()
                                append("{")
                                state.push(OBJECT)
                            } else {
                                append("{}")
                                consumeToken()
                            }
                            continue
                        }
                        if (scanner.nextTokenIs("}")) shrinkIndent()
                        if (scanner.nextTokenIs("[")) state.push(ARRAY)
                        if (scanner.nextTokenIs("]")) state.leave()
                        if (scanner.nextTokenIs(",")) {
                            append(",")
                            if (state.eq(OBJECT)) {
                                newLine = true
                            } else if (state.eq(ARRAY)) {
                                append(" ")
                            }
                            consumeToken()
                            continue
                        }
                        if (scanner.nextTokenIs(":")) {
                            append(": ")
                            consumeToken()
                            continue
                        }

                        if (newLine) {
                            append("\n")
                            indent(indent)
                            newLine = false
                        }

                        if (scanner.nextTokenIs("\"")) {
                            append("\"")
                            append(scanner.eatUnit("\""))
                            consumeToken()
                            continue
                        }

                        if (scanner.nextTokenIs("'")) {
                            append("'")
                            append(scanner.eatUnit("'"))
                            consumeToken()
                            continue
                        }
                        append(token)
                        consumeToken()
                    }
                }
            }
        }

        private fun Stack<State>.eq(testState: State) = peek() == testState

        private fun Stack<State>.leave() = if (isEmpty()) UNKNOWN else pop()

        private fun StringBuilder.indent(i: Int) = append("  ".repeat(i))

        private fun Scanner.nextTokenIs(testToken: String) = hasToken() && testToken == token

        private fun Scanner.eatUnit(desiredToken: String): String {
            var prev = ""
            var sb = ""
            while (hasNext()) {
                val x = next()
                sb += x
                if (x == desiredToken && prev != ESCAPE_CHARACTER) {
                    break
                }
                prev = x
            }
            return sb
        }

        private fun shrinkIndent() {
            indent--
            if (state.leave() == OBJECT) {
                newLine = true
            }
        }

        private fun growIndent() {
            indent++
            newLine = true
        }

        @Suppress("ReturnCount")
        private fun Scanner.hasToken(): Boolean {
            if (token != null) {
                return true
            }

            while (hasNext()) {
                token = next().trim()
                if (token!!.isEmpty()) {
                    continue
                }
                return true
            }
            return false
        }

        private fun consumeToken() {
            token = null
        }
    }
}
