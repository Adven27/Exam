package io.github.adven27.concordion.extensions.exam.core

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
import java.time.format.DateTimeParseException
import java.time.format.ResolverStyle
import java.util.Date
import java.util.Random

fun String.toHtml() = parseTemplate(this)
fun parseTemplate(tmpl: String) = Html(Element(Builder().build(StringReader(tmpl)).rootElement).deepClone())

private val DEFAULT_ZONED_DATETIME_FORMAT = DateTimeFormatter.ISO_OFFSET_DATE_TIME.withResolverStyle(ResolverStyle.SMART)
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

fun String.toDatePattern(): DateTimeFormatter = DateTimeFormatter.ofPattern(this)

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
      <div id='$cntId' class="card-body mb-1 mt-1">
        $help
      </div>
    </div>
    """

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

fun String.fixIndent() = this.replace("\n            ", "\n")

fun Throwable.rootCause(): Throwable {
    var rootCause = this
    while (rootCause.cause != null && rootCause.cause !== rootCause) {
        rootCause = rootCause.cause!!
    }
    return rootCause
}
