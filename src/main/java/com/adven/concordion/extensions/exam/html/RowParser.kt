package com.adven.concordion.extensions.exam.html

import com.adven.concordion.extensions.exam.PlaceholdersResolver
import org.concordion.api.Evaluator

import java.util.ArrayList

import com.google.common.base.Strings.isNullOrEmpty
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings

class RowParser(private val el: Html, private val tag: String, private val eval: Evaluator) {
    private val placeholdersResolver = PlaceholdersResolver
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

    @SuppressFBWarnings(value = "BC_BAD_CAST_TO_ABSTRACT_COLLECTION", justification = "no idea how to fix")
    fun parse(): List<List<Any?>> {
        val result = ArrayList<List<Any?>>()
        var i = 1
        el.childs().filter { tag == it.localName() }.forEach {
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
                    rest.startsWith(PlaceholdersResolver.PREFIX_EXAM) || rest.startsWith(PlaceholdersResolver.PREFIX_VAR) -> {
                        values.add(placeholdersResolver.resolveToObj(
                                rest.substringBefore(PlaceholdersResolver.POSTFIX) + PlaceholdersResolver.POSTFIX, eval)
                        )
                        rest = rest.substringAfter(PlaceholdersResolver.POSTFIX, "").substringAfter(separator, "").trim()
                    }
                    rest.startsWith("'") -> {
                        val cell = rest.substring(1).substringBefore("'")
                        values.add(placeholdersResolver.resolveToObj(cell, eval))
                        rest = rest.substring(cell.length).substringAfter(separator, "").trimStart()
                    }
                    else -> {
                        values.add(placeholdersResolver.resolveToObj(rest.substringBefore(separator).trim(), eval))
                        rest = rest.substringAfter(separator, "").trim()
                    }
                }
            } while (!rest.isBlank())

        }
        return values
    }
}