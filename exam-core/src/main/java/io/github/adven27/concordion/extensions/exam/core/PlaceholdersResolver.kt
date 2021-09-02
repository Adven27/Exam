@file:JvmName("PlaceholdersResolver")

package io.github.adven27.concordion.extensions.exam.core

import io.github.adven27.concordion.extensions.exam.core.handlebars.HANDLEBARS
import io.github.adven27.concordion.extensions.exam.core.handlebars.PLACEHOLDER_TYPE
import io.github.adven27.concordion.extensions.exam.core.handlebars.resolve
import io.github.adven27.concordion.extensions.exam.core.handlebars.resolveObj
import org.concordion.api.Evaluator
import java.lang.Integer.parseInt
import java.time.Duration
import java.time.Period
import java.time.temporal.TemporalAmount

fun Evaluator.resolveForContentType(body: String, type: String): String =
    if (type.contains("xml", true)) resolveXml(body) else resolveJson(body)

fun Evaluator.resolveNoType(body: String) = resolveJson(body)

fun Evaluator.resolveJson(body: String): String = resolveTxt(body, "json", this)
fun Evaluator.resolveXml(body: String): String = resolveTxt(body, "xml", this)

fun Evaluator.resolve(body: String): String = when {
    body.insideApostrophes() -> body.substring(1, body.lastIndex)
    else -> HANDLEBARS.resolve(this, body)
}

private fun resolveTxt(body: String, type: String, eval: Evaluator): String =
    eval.apply { setVariable("#$PLACEHOLDER_TYPE", type) }.resolve(body)

fun parsePeriodFrom(v: String): Pair<Period, Duration> = v.split(",").filter { it.isNotBlank() }
    .map {
        val (p1, p2) = it.trim().split(" ")
        if (p1.toIntOrNull() != null) periodBy(parseInt(p1), p2)
        else periodBy(parseInt(p2), p1)
    }.fold(Pair(Period.ZERO, Duration.ZERO)) { a, n ->
        if (n is Period) a.first + n to a.second else a.first to a.second + (n as Duration)
    }

fun periodBy(value: Int, type: String): TemporalAmount = when (type) {
    "d", "day", "days" -> Period.ofDays(value)
    "M", "month", "months" -> Period.ofMonths(value)
    "y", "year", "years" -> Period.ofYears(value)
    "h", "hour", "hours" -> Duration.ofHours(value.toLong())
    "m", "min", "minute", "minutes" -> Duration.ofMinutes(value.toLong())
    "s", "sec", "second", "seconds" -> Duration.ofSeconds(value.toLong())
    else -> throw UnsupportedOperationException("Unsupported period type $type")
}

fun Evaluator.resolveToObj(placeholder: String?): Any? = when {
    placeholder == null -> null
    placeholder.insideApostrophes() -> placeholder.substring(1, placeholder.lastIndex)
    else -> HANDLEBARS.resolveObj(this, placeholder)
}

private fun String.insideApostrophes() = this.startsWith("'") && this.endsWith("'")

fun resolveToObj(placeholder: String?, evaluator: Evaluator): Any? = evaluator.resolveToObj(placeholder)

fun String?.vars(eval: Evaluator, setVar: Boolean = false, separator: String = ","): Map<String, Any?> =
    this?.split(separator)
        ?.map { it.split('=', limit = 2) }
        ?.map { (k, v) -> k.trim() to v.trim() }
        ?.associate { (k, v) -> k to eval.resolveToObj(v).apply { if (setVar) eval.setVariable("#$k", this) } }
        ?: emptyMap()
