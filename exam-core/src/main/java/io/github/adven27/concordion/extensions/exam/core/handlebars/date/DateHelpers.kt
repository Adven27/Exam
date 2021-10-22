package io.github.adven27.concordion.extensions.exam.core.handlebars.date

import com.github.jknack.handlebars.Options
import com.github.jknack.handlebars.internal.lang3.LocaleUtils
import com.github.jknack.handlebars.internal.lang3.Validate.isInstanceOf
import io.github.adven27.concordion.extensions.exam.core.handlebars.ExamHelper
import io.github.adven27.concordion.extensions.exam.core.handlebars.HB_RESULT
import io.github.adven27.concordion.extensions.exam.core.minus
import io.github.adven27.concordion.extensions.exam.core.parseDate
import io.github.adven27.concordion.extensions.exam.core.parsePeriodFrom
import io.github.adven27.concordion.extensions.exam.core.plus
import io.github.adven27.concordion.extensions.exam.core.toDate
import io.github.adven27.concordion.extensions.exam.core.toString
import java.time.LocalDate.now
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Locale

private const val TZ = "tz"
private const val FORMAT = "format"
private const val PLUS = "plus"
private const val MINUS = "minus"

@Suppress("EnumNaming", "MagicNumber")
enum class DateHelpers(
    override val example: String,
    override val context: Map<String, Any?> = emptyMap(),
    override val expected: Any? = "",
    override val options: Map<String, String> = emptyMap()
) : ExamHelper {
    weeksAgo(
        example = "{{weeksAgo 2}}",
        expected = now().minusWeeks(2).atStartOfDay().toDate()
    ) {
        override fun invoke(context: Any?, options: Options) =
            now().minusWeeks(context?.toString()?.toLong() ?: 1).atStartOfDay().toDate()
    },
    daysAgo(
        example = "{{daysAgo 2}}",
        expected = now().minusDays(2).atStartOfDay().toDate()
    ) {
        override fun invoke(context: Any?, options: Options) =
            now().minusDays(context?.toString()?.toLong() ?: 1).atStartOfDay().toDate()
    },
    dateFormat(
        example = """{{dateFormat date "yyyy-MM-dd'T'HH:mm O" tz="GMT+3" minus="1 y, 2 months, d 3" plus="4 h, 5 min, 6 s"}}""",
        context = mapOf("date" to "2000-01-02T10:20+03:00".parseDate()),
        expected = "1998-10-30T14:25 GMT+3",
        options = mapOf(TZ to "\"GMT+3\"", PLUS to "\"1 day\"", MINUS to "\"5 hours\"")
    ) {
        override fun invoke(context: Any?, options: Options): Any? {
            isInstanceOf(
                Date::class.java,
                context,
                "Wrong context for helper '%s': '%s', expected instance of Date. Example: %s",
                options.fn.text(),
                context,
                example
            )
            return dateFormat(
                context as Date,
                options.param(0, DEFAULT_FORMAT),
                options.param(1, Locale.getDefault().toString()),
                options.hash(PLUS, ""),
                options.hash(MINUS, ""),
                options.hash(TZ)
            )
        }
    },
    now(
        example = """{{now "yyyy-MM-dd'T'HH:mm Z" tz="GMT+3" minus="1 y, 2 months, d 3" plus="4 h, 5 min, 6 s"}}""",
        expected = ZonedDateTime.now("GMT+3".timeZoneId())
            .minusYears(1).minusMonths(2).minusDays(3)
            .plusHours(4).plusMinutes(5).plusSeconds(6)
            .toString("yyyy-MM-dd'T'HH:mm Z"),
        options = mapOf(TZ to "\"GMT+3\"", PLUS to "\"1 day\"", MINUS to "\"5 hours\"")
    ) {
        override fun invoke(context: Any?, options: Options): Any? = if (context is String && context.isNotBlank()) {
            dateFormat(
                Date(),
                context,
                options.param(0, Locale.getDefault().toString()),
                options.hash(PLUS, ""),
                options.hash(MINUS, ""),
                options.hash(TZ)
            )
        } else LocalDateTime.now()
            .plus(parsePeriodFrom(options.hash(PLUS, "")))
            .minus(parsePeriodFrom(options.hash(MINUS, "")))
            .toDate(options.hash<String?>(TZ)?.timeZoneId() ?: ZoneId.systemDefault())
    },
    today(
        example = """{{today "yyyy-MM-dd" minus="1 y, 2 months, d 3"}}""",
        expected = ZonedDateTime.now(ZoneId.systemDefault())
            .minusYears(1).minusMonths(2).minusDays(3)
            .toString("yyyy-MM-dd"),
        options = mapOf(PLUS to "\"1 day\"", MINUS to "\"5 hours\"")
    ) {
        override fun invoke(context: Any?, options: Options): Any? = if (context is String && context.isNotBlank()) {
            dateFormat(
                Date.from(now().atStartOfDay(ZoneId.systemDefault()).toInstant()),
                context,
                options.param(0, Locale.getDefault().toString()),
                options.hash(PLUS, ""),
                options.hash(MINUS, ""),
                options.hash(TZ)
            )
        } else now().atStartOfDay()
            .plus(parsePeriodFrom(options.hash(PLUS, "")))
            .minus(parsePeriodFrom(options.hash(MINUS, "")))
            .toLocalDate().toDate()
    },
    date(
        example = """{{date '01.02.2000 10:20' format="dd.MM.yyyy HH:mm" minus="1 h" plus="1 h"}}""",
        expected = LocalDateTime.of(2000, 2, 1, 10, 20).toDate(),
        options = mapOf(FORMAT to "\"dd.MM.yyyy\"", PLUS to "\"1 day\"", MINUS to "\"5 hours\"")
    ) {
        override fun invoke(context: Any?, options: Options): Any = parseDate(context, options)
            .plus(parsePeriodFrom(options.hash(PLUS, "")))
            .minus(parsePeriodFrom(options.hash(MINUS, "")))
            .toDate()

        private fun parseDate(context: Any?, options: Options): Date = if (context is String && context.isNotBlank()) {
            context.parseDate(options.hash<String>(FORMAT, null))
        } else {
            context as Date
        }
    };

    @Suppress("LongParameterList")
    protected fun dateFormat(date: Date, format: String, local: String, plus: String, minus: String, tz: String?) =
        DateTimeFormatter.ofPattern(format)
            .withLocale(LocaleUtils.toLocale(local))
            .apply { tz?.let { withZone(it.timeZoneId()) } }
            .format(date.plus(parsePeriodFrom(plus)).minus(parsePeriodFrom(minus)).atZone(ZoneId.systemDefault()))

    override fun apply(context: Any?, options: Options): Any? {
        validate(options)
        val result = try {
            this(context, options)
        } catch (expected: Exception) {
            throw ExamHelper.InvocationFailed(name, context, options, expected)
        }
        HB_RESULT.set(result)
        return result
    }

    private fun validate(options: Options) {
        val unexpected = options.hash.keys - this.options.keys
        if (unexpected.isNotEmpty()) throw IllegalArgumentException(
            "Wrong options for helper '${options.fn.text()}': found '$unexpected', expected any of '${this.options}'"
        )
    }

    override fun toString() = this.describe()
    abstract operator fun invoke(context: Any?, options: Options): Any?

    companion object {
        const val DEFAULT_FORMAT = "yyyy-MM-dd'T'HH:mm:ss"
    }
}

fun String.timeZoneId(): ZoneId = ZoneId.of(this)
