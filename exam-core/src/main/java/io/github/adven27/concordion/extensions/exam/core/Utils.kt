@file:Suppress("TooManyFunctions")

package io.github.adven27.concordion.extensions.exam.core

import com.github.jknack.handlebars.internal.text.StringEscapeUtils
import io.github.adven27.concordion.extensions.exam.core.html.Html
import io.github.adven27.concordion.extensions.exam.core.html.codeHighlight
import io.github.adven27.concordion.extensions.exam.core.html.span
import mu.KotlinLogging
import nu.xom.Builder
import org.concordion.api.Element
import org.concordion.api.Evaluator
import java.io.StringReader
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.Period
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeFormatterBuilder
import java.time.format.DateTimeParseException
import java.time.format.ResolverStyle
import java.time.temporal.ChronoField.MICRO_OF_SECOND
import java.util.Date
import java.util.Random

fun String.toHtml() = parseTemplate(this)
fun parseTemplate(tmpl: String) = Html(Element(Builder().build(StringReader(tmpl)).rootElement).deepClone())

private val DEFAULT_ZONED_DATETIME_FORMAT =
    DateTimeFormatter.ISO_OFFSET_DATE_TIME.withResolverStyle(ResolverStyle.SMART)
private val DEFAULT_LOCAL_DATETIME_FORMAT = DateTimeFormatter.ISO_DATE_TIME.withResolverStyle(ResolverStyle.SMART)
private val DEFAULT_LOCAL_DATE_FORMAT = DateTimeFormatter.ISO_DATE.withResolverStyle(ResolverStyle.SMART)

fun ZonedDateTime.toString(pattern: String): String = this.format(DateTimeFormatter.ofPattern(pattern))
fun Date.toString(pattern: String): String =
    pattern.toDatePattern().withZone(ZoneId.systemDefault()).format(this.toInstant())

fun ZonedDateTime.toDate(): Date = Date.from(this.toInstant())
fun LocalDateTime.toDate(zoneId: ZoneId = ZoneId.systemDefault()): Date = Date.from(this.atZone(zoneId).toInstant())
fun LocalDate.toDate(zoneId: ZoneId = ZoneId.systemDefault()): Date = Date.from(this.atStartOfDay(zoneId).toInstant())
fun Date.toZonedDateTime(): ZonedDateTime = ZonedDateTime.from(this.toInstant().atZone(ZoneId.systemDefault()))
fun Date.toLocalDateTime(zoneId: ZoneId = ZoneId.systemDefault()): LocalDateTime =
    this.toInstant().atZone(zoneId).toLocalDateTime()

fun Date.toLocalDate(zoneId: ZoneId = ZoneId.systemDefault()): LocalDate =
    this.toInstant().atZone(zoneId).toLocalDate()

fun Date.plus(period: Pair<Period, Duration>): LocalDateTime =
    this.toLocalDateTime().plus(period.first).plus(period.second)

fun Date.minus(period: Pair<Period, Duration>): LocalDateTime =
    this.toLocalDateTime().minus(period.first).minus(period.second)

fun LocalDateTime.plus(period: Pair<Period, Duration>): LocalDateTime = this.plus(period.first).plus(period.second)
fun LocalDateTime.minus(period: Pair<Period, Duration>): LocalDateTime = this.minus(period.first).minus(period.second)

fun String.parseDate(format: String? = null) = try {
    this.parseDateTime(format).toDate()
} catch (e: DateTimeParseException) {
    logger.debug("Failed to parse ZonedDateTime from $this with pattern '${format ?: DEFAULT_ZONED_DATETIME_FORMAT}'. Try to parse as LocalDateTime.")
    try {
        this.parseLocalDateTime(format).toDate()
    } catch (e: DateTimeParseException) {
        logger.debug("Failed to parse LocalDateTime from $this with pattern '${format ?: DEFAULT_LOCAL_DATETIME_FORMAT}'. Try to parse as LocalDate.")
        this.parseLocalDate(format).toDate()
    }
}

fun String.parseDateTime(format: String? = null): ZonedDateTime = ZonedDateTime.parse(
    this,
    format?.toDatePattern() ?: DEFAULT_ZONED_DATETIME_FORMAT
)

fun String.parseLocalDateTime(format: String? = null): LocalDateTime = LocalDateTime.parse(
    this,
    format?.toDatePattern() ?: DEFAULT_LOCAL_DATETIME_FORMAT
)

fun String.parseLocalDate(format: String? = null): LocalDate = LocalDate.parse(
    this,
    format?.toDatePattern() ?: DEFAULT_LOCAL_DATE_FORMAT
)

@Suppress("MagicNumber")
fun String.toDatePattern(): DateTimeFormatter = DateTimeFormatterBuilder()
    .appendPattern(this)
    .appendFraction(MICRO_OF_SECOND, 0, 9, true)
    .toFormatter()

fun String.fileExt() = substring(lastIndexOf('.') + 1).lowercase()

fun String.toMap(): Map<String, String> = unboxIfNeeded(this)
    .split(",")
    .map {
        val (n, v) = it.split("=")
        Pair(n.trim(), v.trim())
    }.toMap()

fun Map<String, String>.resolveValues(eval: Evaluator) = this.mapValues { eval.resolveNoType(it.value) }

private fun unboxIfNeeded(it: String) = if (it.trim().startsWith("{")) it.substring(1, it.lastIndex) else it

private val logger = KotlinLogging.logger {}

private fun failTemplate(header: String = "", help: String = "", cntId: String) = //language=xml
    """
    <div class="card border-danger alert-warning shadow-lg">
      ${if (header.isNotEmpty()) "<div class='card-header bg-danger text-white'>$header</div>" else ""}
      <div class="card-body mb-1 mt-1">
        <div id='$cntId'> </div>
        ${help(help, cntId)}
      </div>
    </div>
    """

//language=xml
private fun help(help: String, cntId: String) = if (help.isNotEmpty()) """
<p data-bs-toggle="collapse" data-bs-target="#help-$cntId" aria-expanded="false">
    <i class="far fa-caret-square-down"> </i><span> Help</span>
</p>
<div id='help-$cntId' class='collapse'>$help</div>
""" else ""

fun errorMessage(
    header: String = "",
    message: String,
    help: String = "",
    html: Html = span(),
    type: String
): Pair<String, Html> =
    "error-${Random().nextInt()}".let { id ->
        id to failTemplate(header, help, id).toHtml().apply {
            findBy(id)!!(
                codeHighlight(message, type),
                html
            )
        }
    }

fun Throwable.rootCause(): Throwable {
    var rootCause = this
    while (rootCause.cause != null && rootCause.cause !== rootCause) {
        rootCause = rootCause.cause!!
    }
    return rootCause
}

fun Throwable.rootCauseMessage() = this.rootCause().let { it.message ?: it.toString() }

fun List<String>.sameSizeWith(values: List<Any?>): List<String> = if (values.size != size) {
    fun breakReason(cols: List<String>, vals: List<Any?>) =
        if (cols.size > vals.size) "variable '${cols[vals.size]}' has no value" else "value '${vals[cols.size]}' has no variable"

    fun msg(columns: List<String>, values: List<Any?>) =
        "Zipped " + columns.zip(values) { a, b -> "$a=$b" } + " then breaks because " + breakReason(
            columns.toList(),
            values.toList()
        )
    throw IllegalArgumentException(
        String.format(
            "e:where has the variables and values mismatch\ngot %s vars: %s\ngot %s vals: %s:\n%s",
            size,
            this,
            values.size,
            values,
            msg(this, values)
        )
    )
} else this

fun String.escapeHtml(): String = StringEscapeUtils.escapeHtml4(this)
