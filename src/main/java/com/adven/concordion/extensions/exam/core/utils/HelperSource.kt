package com.adven.concordion.extensions.exam.core.utils

import com.adven.concordion.extensions.exam.core.parsePeriodFrom
import com.adven.concordion.extensions.exam.core.utils.HelperSource.Companion.HANDELBAR_RESULT
import com.fasterxml.jackson.databind.util.ISO8601DateFormat
import com.github.jknack.handlebars.Context
import com.github.jknack.handlebars.Handlebars
import com.github.jknack.handlebars.Helper
import com.github.jknack.handlebars.Options
import org.apache.commons.lang3.LocaleUtils
import org.apache.commons.lang3.Validate.isInstanceOf
import org.concordion.api.Evaluator
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import org.joda.time.LocalDateTime
import java.text.SimpleDateFormat
import java.util.*

var START_DELIMITER = "{{"
var END_DELIMITER = "}}"
const val PLACEHOLDER_TYPE = "placeholder_type"

val HANDLEBARS: Handlebars = Handlebars()
    .prettyPrint(false)
    .registerHelpers(HelperSource::class.java)
    .registerHelperMissing(Helper<Any> { _, opts ->
        throw IllegalArgumentException(
            "Variable or helper '${opts.fn.text()}' not found, available helpers:\n\n${HelperSource.values().joinToString(
                "\n"
            )}\n"
        )
    })

fun Handlebars.resolve(eval: Any?, placeholder: String): String =
    this.with { value, next ->
        if (eval is Evaluator) eval.setVariable(HANDELBAR_RESULT, value)
        return@with next.format(value)
    }.compileInline(placeholder, START_DELIMITER, END_DELIMITER).apply(
        Context.newBuilder(eval).resolver(EvaluatorValueResolver.INSTANCE).build()
    )

fun Handlebars.resolveObj(eval: Evaluator, placeholder: String): Any? {
    eval.setVariable(HANDELBAR_RESULT, placeholder)
    resolve(eval, placeholder)
    return eval.getVariable(HANDELBAR_RESULT)
}

private const val TZ = "tz"
private const val FORMAT = "format"
private const val PLUS = "plus"
private const val MINUS = "minus"

enum class HelperSource(
    val example: String,
    val context: Map<String, Any?> = emptyMap(),
    val expected: Any = "",
    val opts: Map<String, String> = emptyMap()
) : Helper<Any?> {
    dateFormat(
        """{{dateFormat date "yyyy-MM-dd'T'HH:mm Z" tz="GMT+3" minus="1 y, 2 months, d 3" plus="4 h, 5 min, 6 s"}}""",
        mapOf("date" to DateTime(2000, 1, 2, 10, 20, DateTimeZone.forOffsetHours(3)).toDate()),
        "1998-10-30T14:25 +0300",
        mapOf(TZ to "\"GMT+3\"", PLUS to "\"1 day\"", MINUS to "\"5 hours\"")
    ) {
        override fun invoke(context: Any?, options: Options): Any? = dateFormat(
            context,
            options,
            options.param(0, DEFAULT_FORMAT),
            options.param(1, Locale.getDefault().toString()),
            options.hash(PLUS, ""),
            options.hash(MINUS, ""),
            options.hash<Any>(TZ)
        )
    },
    now(
        """{{now "yyyy-MM-dd'T'HH:mm Z" tz="GMT+3" minus="1 y, 2 months, d 3" plus="4 h, 5 min, 6 s"}}""",
        emptyMap(),
        DateTime.now(DateTimeZone.forOffsetHours(3))
            .minusYears(1).minusMonths(2).minusDays(3)
            .plusHours(4).plusMinutes(5).plusSeconds(6)
            .toString("yyyy-MM-dd'T'HH:mm Z"),
        mapOf(TZ to "\"GMT+3\"", PLUS to "\"1 day\"", MINUS to "\"5 hours\"")
    ) {
        override fun invoke(context: Any?, options: Options): Any? = if (context is String && context.isNotBlank()) {
            dateFormat(
                Date(),
                options,
                context,
                options.param(0, Locale.getDefault().toString()),
                options.hash(PLUS, ""),
                options.hash(MINUS, ""),
                options.hash<Any>(TZ)
            )
        } else LocalDateTime.now()
            .plus(parsePeriodFrom(options.hash(PLUS, "")))
            .minus(parsePeriodFrom(options.hash(MINUS, "")))
            .toDate()

    },
    date(
        """{{date '01.02.2000 10:20' format="dd.MM.yyyy HH:mm"}}""",
        emptyMap(),
        LocalDateTime(2000, 2, 1, 10, 20).toDate(),
        mapOf(FORMAT to "\"dd.MM.yyyy\"")
    ) {
        override fun invoke(context: Any?, options: Options): Any? {
            val format = options.hash<String>("format", null)
            return if (format == null) ISO8601DateFormat().parse(context as String)
            else SimpleDateFormat(format).parse(context as String)
        }
    },
    string("{{string}}", mapOf(PLACEHOLDER_TYPE to "json"), "\${json-unit.any-string}") {
        override fun invoke(context: Any?, options: Options): Any? =
            "\${${placeholderType(options.context)}-unit.any-string}"
    },
    number("{{number}}", mapOf(PLACEHOLDER_TYPE to "json"), "\${json-unit.any-number}") {
        override fun invoke(context: Any?, options: Options): Any? =
            "\${${placeholderType(options.context)}-unit.any-number}"
    },
    bool("{{bool}}", mapOf(PLACEHOLDER_TYPE to "json"), "\${json-unit.any-boolean}") {
        override fun invoke(context: Any?, options: Options): Any? =
            "\${${placeholderType(options.context)}-unit.any-boolean}"
    },
    ignore("{{ignore}}", mapOf(PLACEHOLDER_TYPE to "json"), "\${json-unit.ignore}") {
        override fun invoke(context: Any?, options: Options): Any? =
            "\${${placeholderType(options.context)}-unit.ignore}"
    },
    regex("{{regex '\\d+'}}", mapOf(PLACEHOLDER_TYPE to "json"), "\${json-unit.regex}\\d+") {
        override fun invoke(context: Any?, options: Options): CharSequence =
            "\${${placeholderType(options.context)}-unit.regex}$context"
    },
    matches(
        "{{matches 'name' 'params'}}", mapOf(PLACEHOLDER_TYPE to "json"), "\${json-unit.matches:name}params"
    ) {
        override fun invoke(context: Any?, options: Options): Any? =
            "\${${placeholderType(options.context)}-unit.matches:$context}${options.param(0, "")}"
    };

    override fun apply(context: Any?, options: Options): Any? {
        validate(options)
        val result = this(context, options)
        (options.context.model() as Evaluator).setVariable(HANDELBAR_RESULT, result)
        return result
    }

    /**
     * Apply the helper to the context.
     *
     * @param context The context object (param=0).
     * @param options The options object.
     * @return A string result.
     */
    protected abstract operator fun invoke(context: Any?, options: Options): Any?

    private fun validate(options: Options) {
        val unexpected = options.hash.keys - opts.keys
        if (unexpected.isNotEmpty()) throw IllegalArgumentException(
            "Wrong options for helper '${options.fn.text()}': found '$unexpected', expected any of '$opts'"
        )
    }

    override fun toString() =
        "$name: '$example' ${if (context.isEmpty()) "" else "+ variables:$context "}=> ${if (expected is String) "'$expected'" else "$expected (${expected::class.java})"}"

    protected fun placeholderType(context: Context) = (context.model() as Evaluator).getVariable("#$PLACEHOLDER_TYPE")

    protected fun dateFormat(
        context: Any?, options: Options, format: String, local: String, plus: String, minus: String, tz: Any?
    ): String? {
        isInstanceOf(
            Date::class.java,
            context,
            "Wrong context for helper '${options.fn.text()}', found '%s', expected instance of Date: ${this.example}",
            context
        )
        val dateFormat = SimpleDateFormat(
            format,
            LocaleUtils.toLocale(local)
        )
        if (tz != null) {
            dateFormat.timeZone = tz as? TimeZone ?: TimeZone.getTimeZone(tz.toString())
        }
        return dateFormat.format(
            LocalDateTime((context as Date).time)
                .plus(parsePeriodFrom(plus))
                .minus(parsePeriodFrom(minus))
                .toDate()
        )
    }

    companion object {
        const val DEFAULT_FORMAT = "yyyy-MM-dd'T'HH:mm:ss"
        const val HANDELBAR_RESULT = "#handlebar_result"
    }
}