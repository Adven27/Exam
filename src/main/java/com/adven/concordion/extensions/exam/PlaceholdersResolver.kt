@file:JvmName("PlaceholdersResolver")

package com.adven.concordion.extensions.exam

import org.concordion.api.Evaluator
import org.joda.time.*
import org.joda.time.LocalDateTime.fromDateFields
import org.joda.time.LocalDateTime.now
import org.joda.time.base.BaseSingleFieldPeriod
import org.joda.time.format.DateTimeFormat.forPattern
import java.lang.Integer.parseInt
import java.util.*

const val PREFIX_JSON_UNIT_ALIAS = "!{"
const val PREFIX_EXAM = "\${exam."
const val PREFIX_VAR = "\${#"
const val POSTFIX = '}'

fun resolveJson(body: String, eval: Evaluator): String {
    return resolve(body, "json", eval)
}

fun resolveXml(body: String, eval: Evaluator): String {
    return resolve(body, "xml", eval)
}

private fun resolve(body: String, type: String, eval: Evaluator): String {
    return resolveAliases(
        type,
        resolveExamCommands(
            resolveVars(body, eval)
        )
    )
}

private fun resolveVars(body: String, eval: Evaluator): String {
    var result = body
    while (result.contains(PREFIX_VAR)) {
        val original = body
        val v = extractVarFrom(original, PREFIX_VAR)
        result = original.replace(PREFIX_VAR + v + POSTFIX, getObject(eval, v).toString())
    }
    return result
}

private fun getObject(eval: Evaluator, value: String): Any? {
    return if (value.contains(":")) {
        val (v, p) = value.split(":".toRegex(), 2)
        forPattern(p).print(fromDateFields(getObject(eval, v) as Date))
    } else {
        eval.getVariable("#$value") ?: eval.evaluate(if (value.contains(".")) "#$value" else value)
    }
}

private fun resolveExamCommands(body: String): String {
    var b = body
    while (b.contains(PREFIX_EXAM)) {
        var original = b
        val v = extractVarFrom(original, PREFIX_EXAM)
        original = original.replace(PREFIX_EXAM + v + POSTFIX, resolveDate(v)!!.toString())
        b = original
    }
    return b
}

private fun resolveAliases(type: String, body: String): String {
    var b = body
    while (b.contains(PREFIX_JSON_UNIT_ALIAS)) {
        val original = b
        val alias = extractFromAlias(original)
        b = original.replace(PREFIX_JSON_UNIT_ALIAS + alias + POSTFIX, toPlaceholder(alias, type))
    }
    return b
}

private fun toPlaceholder(alias: String, type: String): String {
    return when (alias.toLowerCase()) {
        "any-string", "string", "str" -> "\${$type-unit.any-string}"
        "any-number", "number", "num" -> "\${$type-unit.any-number}"
        "any-boolean", "boolean", "bool" -> "\${$type-unit.any-boolean}"
        "regexp", "regex" -> "\${$type-unit.regex}"
        "ignored", "ignore" -> "\${$type-unit.ignore}"
        else -> String.format("\${$type-unit.matches:%s}%s", *alias.split(" ").toTypedArray() as Array<*>)
    }
}

private fun constants(v: String): Any? {
    return when {
        v.startsWith("date(") -> {
            val date = v.substring("date(".length, v.indexOf(")"))
            LocalDateTime.parse(date, forPattern("dd.MM.yyyy")).toDate()
        }
        v.startsWith("now+") -> now().plus(parsePeriod(v)).toDate()
        v.startsWith("now-") -> now().minus(parsePeriod(v)).toDate()
        else -> when (v) {
            "yesterday" -> now().minusDays(1).toDate()
            "today", "now" -> now().toDate()
            "tomorrow" -> now().plusDays(1).toDate()
            else -> null
        }
    }
}

private fun parsePeriod(v: String): Period {
    return v.substring(5, v.indexOf("]")).split(",")
        .map {
            val (p1, p2) = it.trim().split(" ")
            if (p1.isNum())
                periodBy(parseInt(p1), p2)
            else
                periodBy(parseInt(p2), p1)
        }
        .fold(Period.ZERO) { a, n -> a.plus(n) }
}

fun periodBy(value: Int, type: String): BaseSingleFieldPeriod {
    return when (type) {
        "d", "day", "days" -> Days.days(value)
        "M", "month", "months" -> Months.months(value)
        "y", "year", "years" -> Years.years(value)
        "h", "hour", "hours" -> Hours.hours(value)
        "m", "min", "minute", "minutes" -> Minutes.minutes(value)
        "s", "sec", "second", "seconds" -> Seconds.seconds(value)
        else -> throw UnsupportedOperationException("Unsupported period type $type")
    }
}

private fun String.isNum(): Boolean {
    return try {
        parseInt(this)
        true
    } catch (e: NumberFormatException) {
        false
    }
}

fun resolveToObj(placeholder: String?, evaluator: Evaluator): Any? {
    return when {
        placeholder == null -> null
        placeholder.startsWith("'") && placeholder.endsWith("'") -> placeholder.substring(1, placeholder.lastIndex)
        placeholder.startsWith(PREFIX_VAR) -> getObject(evaluator, extractVarFrom(placeholder, PREFIX_VAR))
        placeholder.startsWith(PREFIX_EXAM) -> resolveDate(extractVarFrom(placeholder, PREFIX_EXAM))
        placeholder.isRange() -> placeholder.toRange()
        else -> placeholder
    }
}

fun String.isRange() = this.matches("^[0-9]+[.]{2}[0-9]+$".toRegex())

fun String.toRange(): IntProgression {
    if (this.isRange()) {
        val (start, end) = this.split("[.]{2}".toRegex()).map(String::toInt)
        return IntProgression.fromClosedRange(start, end, end.compareTo(start))
    }
    throw IllegalArgumentException("Couldn't parse range from string $this")
}

private fun extractVarFrom(placeholder: String, namespace: String): String {
    val s = placeholder.substring(placeholder.indexOf(namespace))
    return s.substring(namespace.length, s.indexOf(POSTFIX))
}

private fun extractFromAlias(placeholder: String): String {
    val s = placeholder.substring(placeholder.indexOf(PREFIX_JSON_UNIT_ALIAS))
    return s.substring(2, s.indexOf(POSTFIX))
}

private fun resolveDate(v: String): Any? {
    return if (v.contains(":")) getDateFromPattern(v) else constants(v)
}

private fun getDateFromPattern(value: String): String {
    val (v, p) = value.split(":".toRegex(), 2)
    return forPattern(p).print(fromDateFields((constants(v) as Date?)!!))
}