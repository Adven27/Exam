package io.github.adven27.concordion.extensions.exam.core.handlebars.matchers

import com.github.jknack.handlebars.Context
import com.github.jknack.handlebars.Options
import io.github.adven27.concordion.extensions.exam.core.handlebars.ExamHelper
import io.github.adven27.concordion.extensions.exam.core.handlebars.HB_RESULT
import org.concordion.api.Evaluator
import java.util.regex.Pattern

const val PLACEHOLDER_TYPE = "placeholder_type"
const val DB_ACTUAL = "db_actual"

enum class MatcherHelpers(
    override val example: String,
    override val context: Map<String, Any?> = emptyMap(),
    override val expected: Any? = "",
    override val exampleOptions: Map<String, String> = emptyMap()
) : ExamHelper<Any?> {

    string(
        example = "{{string}}",
        context = mapOf(PLACEHOLDER_TYPE to "json"),
        expected = "\${json-unit.any-string}"
    ) {
        override fun invoke(context: Any?, options: Options): Any =
            "\${${placeholderType(options.context)}-unit.any-string}"
    },
    number(
        example = "{{number}}",
        context = mapOf(PLACEHOLDER_TYPE to "json"),
        expected = "\${json-unit.any-number}"
    ) {
        override fun invoke(context: Any?, options: Options): Any =
            "\${${placeholderType(options.context)}-unit.any-number}"
    },
    bool(
        example = "{{bool}}",
        context = mapOf(PLACEHOLDER_TYPE to "json"),
        expected = "\${json-unit.any-boolean}"
    ) {
        override fun invoke(context: Any?, options: Options): Any =
            "\${${placeholderType(options.context)}-unit.any-boolean}"
    },
    ignore(
        example = "{{ignore}}",
        context = mapOf(PLACEHOLDER_TYPE to "json"),
        expected = "\${json-unit.ignore}"
    ) {
        override fun invoke(context: Any?, options: Options): Any =
            "\${${placeholderType(options.context)}-unit.ignore}"
    },
    regex(
        example = "{{regex '\\d+'}}",
        context = mapOf(PLACEHOLDER_TYPE to "json"),
        expected = "\${json-unit.regex}\\d+"
    ) {
        override fun invoke(context: Any?, options: Options): Any = if (placeholderType(options.context) == "db") {
            regexMatches(
                context.toString(),
                dbActual(options.context)
            )
        } else "\${${placeholderType(options.context)}-unit.regex}$context"
    },
    matches(
        example = "{{matches 'name' 'params'}}",
        context = mapOf(PLACEHOLDER_TYPE to "json"),
        expected = "\${json-unit.matches:name}params"
    ) {
        override fun invoke(context: Any?, options: Options): Any =
            "\${${placeholderType(options.context)}-unit.matches:$context}${options.param(0, "")}"
    },
    formattedAs(
        example = "{{formattedAs \"yyyy-MM-dd'T'hh:mm:ss\"}}",
        context = mapOf(PLACEHOLDER_TYPE to "json"),
        expected = "\${json-unit.matches:formattedAs}yyyy-MM-dd'T'hh:mm:ss"
    ) {
        override fun invoke(context: Any?, options: Options): Any =
            "\${${placeholderType(options.context)}-unit.matches:$name}$context"
    },
    formattedAndWithinNow(
        example = "{{formattedAndWithinNow \"yyyy-MM-dd'T'hh:mm:ss\" \"5s\"}}",
        context = mapOf(PLACEHOLDER_TYPE to "json"),
        expected = "\${json-unit.matches:formattedAndWithinNow}[yyyy-MM-dd'T'hh:mm:ss][5s]"
    ) {
        override fun invoke(context: Any?, options: Options): Any =
            "\${${placeholderType(options.context)}-unit.matches:$name}[$context][${options.param(0, "5s")}]"
    },
    formattedAndWithin(
        example = "{{formattedAndWithin 'yyyy-MM-dd' '5s' '1951-05-13'}}",
        context = mapOf(PLACEHOLDER_TYPE to "json"),
        expected = "\${json-unit.matches:formattedAndWithin}[yyyy-MM-dd][5s][1951-05-13]"
    ) {
        override fun invoke(context: Any?, options: Options): Any =
            "\${${placeholderType(options.context)}-unit.matches:$name}" +
                "[$context][${options.param(0, "5s")}][${options.param(1, "")}]"
    };

    protected fun placeholderType(context: Context) = (context.model() as Evaluator).getVariable("#$PLACEHOLDER_TYPE")
    protected fun dbActual(context: Context) = (context.model() as Evaluator).getVariable("#$DB_ACTUAL")

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
        val unexpected = options.hash.keys - exampleOptions.keys
        if (unexpected.isNotEmpty()) throw IllegalArgumentException(
            "Wrong options for helper '${options.fn.text()}': found '$unexpected', expected any of '$exampleOptions'"
        )
    }

    override fun toString() = this.describe()
    abstract operator fun invoke(context: Any?, options: Options): Any?
}

private fun regexMatches(p: String, actualValue: Any?): Boolean {
    if (actualValue == null) return false
    val pattern = Pattern.compile(p)
    return pattern.matcher(actualValue.toString()).matches()
}
