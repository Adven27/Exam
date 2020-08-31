@file:JvmName("PlaceholdersResolver")

package com.adven.concordion.extensions.exam.core

import com.adven.concordion.extensions.exam.core.utils.HANDLEBARS
import com.adven.concordion.extensions.exam.core.utils.PLACEHOLDER_TYPE
import com.adven.concordion.extensions.exam.core.utils.resolve
import com.adven.concordion.extensions.exam.core.utils.resolveObj
import org.concordion.api.Evaluator
import org.joda.time.Days
import org.joda.time.Hours
import org.joda.time.Minutes
import org.joda.time.Months
import org.joda.time.Period
import org.joda.time.Seconds
import org.joda.time.Years
import org.joda.time.base.BaseSingleFieldPeriod
import java.lang.Integer.parseInt

fun Evaluator.resolveForContentType(body: String, type: String): String = if (type.contains("xml", true))
    resolveXml(body) else resolveJson(body)

fun Evaluator.resolveNoType(body: String) = resolveJson(body)

fun Evaluator.resolveJson(body: String): String = resolveTxt(body, "json", this)
fun Evaluator.resolveXml(body: String): String = resolveTxt(body, "xml", this)

fun Evaluator.resolve(body: String): String = when {
    body.insideApostrophes() -> body.substring(1, body.lastIndex)
    else -> HANDLEBARS.resolve(this, body)
}

private fun resolveTxt(body: String, type: String, eval: Evaluator): String =
    eval.apply { setVariable("#$PLACEHOLDER_TYPE", type) }.resolve(body)

fun parsePeriodFrom(v: String): Period = v.split(",").filter { it.isNotBlank() }
    .map {
        val (p1, p2) = it.trim().split(" ")
        if (p1.toIntOrNull() != null) periodBy(parseInt(p1), p2)
        else periodBy(parseInt(p2), p1)
    }.fold(Period.ZERO) { a, n -> a + n }

fun periodBy(value: Int, type: String): BaseSingleFieldPeriod = when (type) {
    "d", "day", "days" -> Days.days(value)
    "M", "month", "months" -> Months.months(value)
    "y", "year", "years" -> Years.years(value)
    "h", "hour", "hours" -> Hours.hours(value)
    "m", "min", "minute", "minutes" -> Minutes.minutes(value)
    "s", "sec", "second", "seconds" -> Seconds.seconds(value)
    else -> throw UnsupportedOperationException("Unsupported period type $type")
}

fun Evaluator.resolveToObj(placeholder: String?): Any? = when {
    placeholder == null -> null
    placeholder.insideApostrophes() -> placeholder.substring(1, placeholder.lastIndex)
    else -> HANDLEBARS.resolveObj(this, placeholder)
}

private fun String.insideApostrophes() = this.startsWith("'") && this.endsWith("'")

fun resolveToObj(placeholder: String?, evaluator: Evaluator): Any? = evaluator.resolveToObj(placeholder)

fun String?.vars(eval: Evaluator, setVar: Boolean = false, separator: String = ","): Map<String, Any?> = this?.split(separator)
    ?.map { it.split('=', limit = 2) }
    ?.map { (k, v) -> k.trim() to v.trim() }
    ?.map { (k, v) -> k to eval.resolveToObj(v).apply { if (setVar) eval.setVariable("#$k", this) } }
    ?.toMap() ?: emptyMap()