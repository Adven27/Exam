package com.adven.concordion.extensions.exam.rest

import com.adven.concordion.extensions.exam.rest.JsonPrettyPrinter.State.*
import java.util.*

class JsonPrettyPrinter {

    fun prettyPrint(json: String?): String {
        return if (json == null) "" else Parser(json).parse()
    }

    internal enum class State {
        UNKNOWN, OBJECT, ARRAY
    }

    private class Parser(json: String) {
        private val scanner: Scanner = Scanner(json)
        private val result: StringBuilder
        private var indent = 0
        private var newLine = false
        private val state = Stack<State>()
        private var token: String? = null

        init {
            scanner.useDelimiter("")
            result = StringBuilder()
            enterState(UNKNOWN)
        }

        internal fun parse(): String {
            scanner.use { _ ->
                while (hasToken()) {
                    if (nextTokenIs("{")) {
                        consumeToken()
                        if (!nextTokenIs("}")) {
                            growIndent()
                            append("{")
                            enterState(OBJECT)
                        } else {
                            append("{}")
                            consumeToken()
                        }
                        continue
                    }
                    if (nextTokenIs("}")) {
                        shrinkIndent()
                    }
                    if (nextTokenIs("[")) {
                        enterState(ARRAY)
                    }
                    if (nextTokenIs("]")) {
                        leaveState()
                    }
                    if (nextTokenIs(",")) {
                        append(",")
                        if (inState(OBJECT)) {
                            newLine = true
                        } else if (inState(ARRAY)) {
                            append(" ")
                        }
                        consumeToken()
                        continue
                    }
                    if (nextTokenIs(":")) {
                        append(": ")
                        consumeToken()
                        continue
                    }

                    if (newLine) {
                        append("\n")
                        indent()
                        newLine = false
                    }

                    if (nextTokenIs("\"")) {
                        append("\"")
                        eatUnit("\"")
                        consumeToken()
                        continue
                    }

                    if (nextTokenIs("'")) {
                        append("'")
                        eatUnit("'")
                        consumeToken()
                        continue
                    }
                    append(token)
                    consumeToken()
                }
                return result.toString()
            }
        }

        private fun inState(testState: State): Boolean {
            return state.peek() == testState
        }

        private fun leaveState(): State {
            return if (state.isEmpty()) UNKNOWN else state.pop()
        }

        private fun enterState(newState: State) {
            state.push(newState)
        }

        private fun indent() {
            for (i in 0 until indent) {
                append("  ")
            }
        }

        private fun append(value: String?) {
            result.append(value)
        }

        private fun nextTokenIs(testToken: String): Boolean {
            return hasToken() && testToken == token
        }

        private fun eatUnit(desiredToken: String) {
            var prev = ""
            while (scanner.hasNext()) {
                val x = scanner.next()
                result.append(x)
                if (x == desiredToken && prev != ESCAPE_CHARACTER) {
                    break
                }
                prev = x
            }
        }

        private fun shrinkIndent() {
            indent--
            if (leaveState() == OBJECT) {
                newLine = true
            }
        }

        private fun growIndent() {
            indent++
            newLine = true
        }

        private fun hasToken(): Boolean {
            if (token != null) {
                return true
            }

            while (scanner.hasNext()) {
                token = scanner.next().trim { it <= ' ' }
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

        companion object {
            private const val ESCAPE_CHARACTER = "\\"
        }
    }
}