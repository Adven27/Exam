package io.github.adven27.concordion.extensions.exam.core.html

import io.github.adven27.concordion.extensions.exam.core.resolveToObj
import org.concordion.api.Evaluator

class DbRowParser(private val el: Html, private val tag: String, ignoreRowsBefore: String?, ignoreRowsAfter: String?) {
    private val ignoreBefore: Int = if (ignoreRowsBefore != null) Integer.parseInt(ignoreRowsBefore) else 1
    private val ignoreAfter: Int = if (ignoreRowsAfter != null) Integer.parseInt(ignoreRowsAfter) else 0
    private val separator: Char = el.takeAwayAttr("separator", ",").first()

    fun parse(): List<List<Any?>> {
        val result = ArrayList<List<Any?>>()
        var i = 1
        el.childs().filter { it.localName().contains(tag) }.forEach {
            if (skip(i++)) return@forEach
            result.add(parseValues(it.text(), separator))
            el.remove(it)
        }
        return result
    }

    private fun skip(i: Int) = i < ignoreBefore || (ignoreAfter != 0 && i > ignoreAfter)
}

class RowParserEval(private val el: Html, private val tag: String, private val eval: Evaluator) {
    private val separator: Char = el.takeAwayAttr("separator", ",").first()

    fun parse(): Map<String, List<Any?>> = el.childs().filter { it.localName().contains(tag) }
        .mapIndexed { i, html ->
            (html.attr("desc") ?: (i + 1).toString()) to parseValues(html.text(), separator).map { eval.resolveToObj(it) }
        }.toMap()
}

internal fun parseValues(text: String, separator: Char): List<String> {
    val values = ArrayList<String>()
    if (text.isEmpty()) {
        values.add(text)
    } else {
        var rest = text.trim()
        do {
            rest = if (rest.startsWith("'")) {
                val cell = rest.substring(1).substringBefore("'")
                values.add(cell)
                rest.substring(cell.length).substringAfter(separator, "").trimStart()
            } else {
                values.add(rest.substringBefore(separator).trim())
                rest.substringAfter(separator, "").trim()
            }
        } while (!rest.isBlank())
    }
    return values
}
