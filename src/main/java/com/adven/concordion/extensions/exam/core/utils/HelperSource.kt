package com.adven.concordion.extensions.exam.core.utils

import com.adven.concordion.extensions.exam.core.parsePeriodFrom
import com.github.jknack.handlebars.Helper
import com.github.jknack.handlebars.Options
import org.apache.commons.lang3.LocaleUtils
import org.apache.commons.lang3.Validate.isInstanceOf
import org.joda.time.LocalDateTime
import java.text.SimpleDateFormat
import java.util.*

private const val TZ = "tz"
private const val FORMAT = "format"
private const val PLUS = "plus"
private const val MINUS = "minus"

val missing = Helper<Any> { _, options ->
    throw IllegalArgumentException(
        "Variable or helper '${options.fn.text()}' not found, available helpers:\n\n${HelperSource.values().joinToString(
            "\n"
        )}\n"
    )
}

enum class HelperSource(val desc: String, val opts: Map<String, String> = emptyMap()) : Helper<Any?> {
    dateFormat(
        """{{dateFormat date format="yyyy-MM-DD'T'HH:mm Z" tz="GMT+3" minus="1 y, 2 months, d 3" plus="4 h, 5 min, 6 s"}}""",
        mapOf(TZ to "\"GMT+3\"", FORMAT to "\"dd.MM.yyyy\"", PLUS to "\"1 day\"", MINUS to "\"5 hours\"")
    ) {
        override fun invoke(context: Any?, options: Options): CharSequence {
            validate(context, options)
            val pattern = options.param(0, options.hash<Any>("format", "dd.MM.yyyy'T'HH:mm:ss"))
            val dateFormat = SimpleDateFormat(
                pattern.toString(),
                LocaleUtils.toLocale(options.param(1, Locale.getDefault().toString()))
            )
            val tz = options.hash<Any>(TZ)
            if (tz != null) {
                val timeZone = tz as? TimeZone ?: TimeZone.getTimeZone(tz.toString())
                dateFormat.timeZone = timeZone
            }
            return dateFormat.format(
                LocalDateTime((context as Date).time)
                    .plus(parsePeriodFrom(options.hash("plus", "")))
                    .minus(parsePeriodFrom(options.hash("minus", "")))
                    .toDate()
            )
        }
    },
    now("""{{now format="yyyy-MM-dd'T'HH:mm:ss Z" tz="GMT+3"}}""", dateFormat.opts) {
        override fun invoke(context: Any?, options: Options): CharSequence = dateFormat(Date(), options)

    };

    override fun apply(context: Any?, options: Options): Any? = this(context, options)

    /**
     * Apply the helper to the context.
     *
     * @param context The context object (param=0).
     * @param options The options object.
     * @return A string result.
     */
    protected abstract operator fun invoke(context: Any?, options: Options): CharSequence

    fun validate(value: Any?, options: Options) {
        isInstanceOf(
            Date::class.java,
            value,
            "Wrong context for helper '${options.fn.text()}', found '%s', expected instance of Date: ${this.desc}",
            value
        )
        val unexpected = options.hash.keys - opts.keys
        if (unexpected.isNotEmpty()) throw IllegalArgumentException(
            "Wrong options for helper '${options.fn.text()}', found '$unexpected', expected any of '$opts'"
        )
    }

    override fun toString(): String = "$ordinal) $name: $desc"
}