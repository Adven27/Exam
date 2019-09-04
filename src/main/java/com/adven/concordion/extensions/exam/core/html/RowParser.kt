package com.adven.concordion.extensions.exam.core.html

import com.adven.concordion.extensions.exam.core.*
import com.google.common.base.Strings.isNullOrEmpty
import org.concordion.api.Evaluator
import java.util.*

class RowParser(private val el: Html, private val tag: String, private val eval: Evaluator) {
    private val ignoreBefore: Int
    private val ignoreAfter: Int
    private val separator: Char

    init {
        val ignoreBeforeStr = el.takeAwayAttr("ignoreRowsBefore", eval)
        val ignoreAfterStr = el.takeAwayAttr("ignoreRowsAfter", eval)
        ignoreBefore = if (ignoreBeforeStr != null) Integer.parseInt(ignoreBeforeStr) else 1
        ignoreAfter = if (ignoreAfterStr != null) Integer.parseInt(ignoreAfterStr) else 0
        separator = el.takeAwayAttr("separator", ",").first()
    }

    fun parse(): List<List<Any?>> {
        val result = ArrayList<List<Any?>>()
        var i = 1
        el.childs().filter { it.localName().contains(tag) }.forEach {
            if (skip(i++)) return@forEach
            result.add(parseValues(eval, it.text(), separator))
            el.remove(it)
        }
        return result
    }

    private fun skip(i: Int) = i < ignoreBefore || (ignoreAfter != 0 && i > ignoreAfter)

    private fun parseValues(eval: Evaluator, text: String, separator: Char): List<Any?> {
        val values = ArrayList<Any?>()
        if (isNullOrEmpty(text)) {
            values.add(text)
        } else {
            var rest = text.trim()
            do {
                when {
                    rest.startsWith(PREFIX) -> {
                        values.add(
                            eval.resolveToObj(rest.substringBefore(separator))
                        )
                        rest = rest.substringAfter(separator, "").trim()
                    }
                    rest.startsWith("!{") -> {
                        values.add(rest.substringBefore(separator))
                        rest = rest.substringAfter(separator, "").trim()
                    }
                    rest.startsWith("'") -> {
                        val cell = rest.substring(1).substringBefore("'")
                        values.add(eval.resolveToObj(cell))
                        rest = rest.substring(cell.length).substringAfter(separator, "").trimStart()
                    }
                    else -> {
                        values.add(
                            eval.resolveToObj(rest.substringBefore(separator).trim())
                        )
                        rest = rest.substringAfter(separator, "").trim()
                    }
                }
            } while (!rest.isBlank())

        }
        return values
    }
}