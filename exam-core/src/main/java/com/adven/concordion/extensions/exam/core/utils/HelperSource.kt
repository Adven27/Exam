package com.adven.concordion.extensions.exam.core.utils

import com.adven.concordion.extensions.exam.core.minus
import com.adven.concordion.extensions.exam.core.parseDate
import com.adven.concordion.extensions.exam.core.parsePeriodFrom
import com.adven.concordion.extensions.exam.core.plus
import com.adven.concordion.extensions.exam.core.resolve
import com.adven.concordion.extensions.exam.core.resolveToObj
import com.adven.concordion.extensions.exam.core.toDate
import com.adven.concordion.extensions.exam.core.toString
import com.github.jknack.handlebars.Context
import com.github.jknack.handlebars.EscapingStrategy.NOOP
import com.github.jknack.handlebars.Handlebars
import com.github.jknack.handlebars.Helper
import com.github.jknack.handlebars.Options
import com.github.jknack.handlebars.internal.lang3.LocaleUtils
import com.github.jknack.handlebars.internal.lang3.Validate.isInstanceOf
import org.concordion.api.Evaluator
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Locale
import java.util.concurrent.atomic.AtomicReference
import java.util.regex.Pattern

var START_DELIMITER = "{{"
var END_DELIMITER = "}}"
const val PLACEHOLDER_TYPE = "placeholder_type"
const val DB_ACTUAL = "db_actual"
val HB_RESULT: AtomicReference<Any?> = AtomicReference()

val HANDLEBARS: Handlebars = Handlebars()
    .with(NOOP)
    .with { value, next ->
        HB_RESULT.set(value)
        return@with next.format(value)
    }
    .prettyPrint(false)
    .registerHelpers(HelperSource::class.java)
    .registerHelperMissing(HelpHelper())

class HelpHelper : Helper<Any?> {
    override fun apply(context: Any?, options: Options): Any {
        throw IllegalArgumentException(
            """
Variable or helper '${options.fn.text()}' not found, available helpers:

${helpersDesc()}

Or register custom one:

ExamExtension().withHandlebar { hb ->
    hb.registerHelper("hi", Helper { context: Any?, options ->
        //{{hi '1' 'p1 'p2' o1='a' o2='b'}} => Hello context = 1; params = [p1, p2]; options = {o1=a, o2=b}!
        //{{hi variable1 variable2 o1=variable3}} => Hello context = 1; params = [2]; options = {o1=3}!
        "Hello context = ${"$"}context; params = ${"$"}{options.params.map { it.toString() }}; options = ${"$"}{options.hash}!"
    })
}"""
        )
    }

    private fun helpersDesc() =
        HANDLEBARS.helpers().sortedBy { it.key }.joinToString("\n") { "${it.key.padStart(25)} : ${it.value}" }
}

fun Handlebars.resolve(eval: Any?, placeholder: String): String =
    this.compileInline(placeholder, START_DELIMITER, END_DELIMITER).apply(
        Context.newBuilder(eval).resolver(EvaluatorValueResolver.INSTANCE).build()
    )

fun Handlebars.resolveObj(eval: Evaluator, placeholder: String): Any? {
    HB_RESULT.set(placeholder)
    resolve(eval, placeholder)
    return HB_RESULT.get()
}

private const val TZ = "tz"
private const val FORMAT = "format"
private const val PLUS = "plus"
private const val MINUS = "minus"

enum class HelperSource(
    override val example: String,
    override val context: Map<String, Any?> = emptyMap(),
    override val expected: Any? = "",
    override val opts: Map<String, String> = emptyMap()
) : ExamHelper<Any?> {
    dateFormat(
        """{{dateFormat date "yyyy-MM-dd'T'HH:mm O" tz="GMT+3" minus="1 y, 2 months, d 3" plus="4 h, 5 min, 6 s"}}""",
        mapOf("date" to "2000-01-02T10:20+03".parseDate()),
        "1998-10-30T14:25 GMT+3",
        mapOf(TZ to "\"GMT+3\"", PLUS to "\"1 day\"", MINUS to "\"5 hours\"")
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
        """{{now "yyyy-MM-dd'T'HH:mm Z" tz="GMT+3" minus="1 y, 2 months, d 3" plus="4 h, 5 min, 6 s"}}""",
        emptyMap(),
        ZonedDateTime.now("GMT+3".timeZoneId())
            .minusYears(1).minusMonths(2).minusDays(3)
            .plusHours(4).plusMinutes(5).plusSeconds(6)
            .toString("yyyy-MM-dd'T'HH:mm Z"),
        mapOf(TZ to "\"GMT+3\"", PLUS to "\"1 day\"", MINUS to "\"5 hours\"")
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
        """{{today "yyyy-MM-dd" minus="1 y, 2 months, d 3"}}""",
        emptyMap(),
        ZonedDateTime.now(ZoneId.systemDefault()).minusYears(1).minusMonths(2).minusDays(3).toString("yyyy-MM-dd"),
        mapOf(PLUS to "\"1 day\"", MINUS to "\"5 hours\"")
    ) {
        override fun invoke(context: Any?, options: Options): Any? = if (context is String && context.isNotBlank()) {
            dateFormat(
                Date.from(LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant()),
                context,
                options.param(0, Locale.getDefault().toString()),
                options.hash(PLUS, ""),
                options.hash(MINUS, ""),
                options.hash(TZ)
            )
        } else LocalDate.now().atStartOfDay()
            .plus(parsePeriodFrom(options.hash(PLUS, "")))
            .minus(parsePeriodFrom(options.hash(MINUS, "")))
            .toLocalDate().toDate()
    },
    date(
        """{{date '01.02.2000 10:20' format="dd.MM.yyyy HH:mm" minus="1 h" plus="1 h"}}""",
        emptyMap(),
        LocalDateTime.of(2000, 2, 1, 10, 20).toDate(),
        mapOf(FORMAT to "\"dd.MM.yyyy\"", PLUS to "\"1 day\"", MINUS to "\"5 hours\"")
    ) {
        override fun invoke(context: Any?, options: Options): Any? = parseDate(context, options)
            .plus(parsePeriodFrom(options.hash(PLUS, "")))
            .minus(parsePeriodFrom(options.hash(MINUS, "")))
            .toDate()

        private fun parseDate(context: Any?, options: Options): Date = if (context is String && context.isNotBlank()) {
            context.parseDate(options.hash<String>(FORMAT, null))
        } else {
            context as Date
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
        override fun invoke(context: Any?, options: Options): Any? = if (placeholderType(options.context) == "db") {
            regexMatches(context.toString(), dbActual(options.context))
        } else "\${${placeholderType(options.context)}-unit.regex}$context"
    },
    matches(
        "{{matches 'name' 'params'}}", mapOf(PLACEHOLDER_TYPE to "json"), "\${json-unit.matches:name}params"
    ) {
        override fun invoke(context: Any?, options: Options): Any? =
            "\${${placeholderType(options.context)}-unit.matches:$context}${options.param(0, "")}"
    },
    formattedAs(
        "{{formattedAs \"yyyy-MM-dd'T'hh:mm:ss\"}}",
        mapOf(PLACEHOLDER_TYPE to "json"),
        "\${json-unit.matches:formattedAs}yyyy-MM-dd'T'hh:mm:ss"
    ) {
        override fun invoke(context: Any?, options: Options): Any? =
            "\${${placeholderType(options.context)}-unit.matches:$name}$context"
    },
    formattedAndWithinNow(
        "{{formattedAndWithinNow \"yyyy-MM-dd'T'hh:mm:ss\" \"5s\"}}",
        mapOf(PLACEHOLDER_TYPE to "json"),
        "\${json-unit.matches:formattedAndWithinNow}[yyyy-MM-dd'T'hh:mm:ss][5s]"
    ) {
        override fun invoke(context: Any?, options: Options): Any? =
            "\${${placeholderType(options.context)}-unit.matches:$name}[$context][${options.param(0, "5s")}]"
    },
    formattedAndWithin(
        "{{formattedAndWithin 'yyyy-MM-dd' '5s' '1951-05-13'}}",
        mapOf(PLACEHOLDER_TYPE to "json"),
        "\${json-unit.matches:formattedAndWithin}[yyyy-MM-dd][5s][1951-05-13]"
    ) {
        override fun invoke(context: Any?, options: Options): Any? =
            "\${${placeholderType(options.context)}-unit.matches:$name}" +
                "[$context][${options.param(0, "5s")}][${options.param(1, "")}]"
    },
    NULL("{{NULL}}", emptyMap(), null) {
        override fun invoke(context: Any?, options: Options): Any? = null
    },
    eval("{{eval '#var'}}", mapOf("var" to 2), 2) {
        override fun invoke(context: Any?, options: Options): Any? = options.evaluator().evaluate("$context")
    },
    resolve("{{resolve 'today is {{today}}'}}", emptyMap(), "today is ${LocalDate.now().toDate()}") {
        override fun invoke(context: Any?, options: Options): Any? {
            val evaluator = options.evaluator()
            options.hash.forEach { (key, value) ->
                evaluator.setVariable("#$key", evaluator.resolveToObj(value as String?))
            }
            return evaluator.resolve("$context")
        }
    },
    resolveFile("{{resolveFile '/hb/some-file.txt'}}", emptyMap(), "today is ${LocalDate.now().toDate()}") {
        override fun invoke(context: Any?, options: Options): Any? {
            val evaluator = options.evaluator()
            options.hash.forEach { (key, value) ->
                evaluator.setVariable("#$key", evaluator.resolveToObj(value as String?))
            }
            return evaluator.resolve(context.toString().readFile())
        }
    };

    protected fun placeholderType(context: Context) = (context.model() as Evaluator).getVariable("#$PLACEHOLDER_TYPE")
    protected fun dbActual(context: Context) = (context.model() as Evaluator).getVariable("#$DB_ACTUAL")

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
        if ("resolve" == this.name || "resolveFile" == this.name) return
        val unexpected = options.hash.keys - opts.keys
        if (unexpected.isNotEmpty()) throw IllegalArgumentException(
            "Wrong options for helper '${options.fn.text()}': found '$unexpected', expected any of '$opts'"
        )
    }

    override fun toString() = "'$example' ${if (context.isEmpty()) "" else "+ variables:$context "}=> ${expectedStr()}"

    private fun expectedStr() = when (expected) {
        is String -> "'$expected'"
        null -> null
        else -> "$expected (${expected?.javaClass})"
    }

    abstract operator fun invoke(context: Any?, options: Options): Any?

    companion object {
        const val DEFAULT_FORMAT = "yyyy-MM-dd'T'HH:mm:ss"
    }
}

interface ExamHelper<T> : Helper<T> {
    val example: String
    val context: Map<String, Any?>
    val expected: Any?
    val opts: Map<String, String>

    class InvocationFailed(name: String, context: Any?, options: Options, throwable: Throwable) :
        RuntimeException(
            "Invocation of {{$name}} (context: $context, options: ${options.fn.text()}) failed: ${throwable.message}",
            throwable
        )
}

fun String.timeZoneId() = ZoneId.of(this)

private fun regexMatches(p: String, actualValue: Any?): Boolean {
    if (actualValue == null) return false
    val pattern = Pattern.compile(p)
    return pattern.matcher(actualValue.toString()).matches()
}

fun Options.evaluator(): Evaluator = (this.context.model() as Evaluator)
