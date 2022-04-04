package io.github.adven27.concordion.extensions.exam.core.handlebars

import com.github.jknack.handlebars.Context
import com.github.jknack.handlebars.EscapingStrategy.NOOP
import com.github.jknack.handlebars.Formatter
import com.github.jknack.handlebars.Handlebars
import com.github.jknack.handlebars.Helper
import com.github.jknack.handlebars.Options
import io.github.adven27.concordion.extensions.exam.core.handlebars.date.DateHelpers
import io.github.adven27.concordion.extensions.exam.core.handlebars.matchers.MatcherHelpers
import io.github.adven27.concordion.extensions.exam.core.handlebars.misc.MiscHelpers
import org.concordion.api.Evaluator

val HELPER_RESULTS: MutableList<Any?> = mutableListOf()

val HANDLEBARS: Handlebars = Handlebars()
    .with { value: Any?, next: Formatter.Chain ->
        (if (value is Result<*>) value.getOrThrow() else value).let {
            HELPER_RESULTS.add(it)
            next.format(it.toString())
        }
    }
    .with(NOOP)
    .prettyPrint(false)
    .registerHelpers(MiscHelpers::class.java)
    .registerHelpers(DateHelpers::class.java)
    .registerHelpers(MatcherHelpers::class.java)
    .registerHelperMissing(HelperMissing())

class MissingHelperException(options: Options) : IllegalArgumentException(
    "Variable or helper '${options.fn.text()}' not found"
)

class HelperMissing : Helper<Any?> {
    override fun apply(context: Any?, options: Options) = throw MissingHelperException(options)

    companion object {
        fun helpersDesc(): Map<Package, Map<String, Helper<*>>> =
            HANDLEBARS.helpers().groupBy { it.value.javaClass.`package` }.map { e ->
                e.key to e.value
                    .filterNot { it.key == "helperMissing" }
                    .sortedBy { it.key }
                    .associate { it.key to it.value }
            }.toMap()
    }
}

private fun Handlebars.resolve(eval: Any?, placeholder: String): Any? = compileInline(placeholder).let { template ->
    HELPER_RESULTS.clear()
    template.apply(Context.newBuilder(eval).resolver(EvaluatorValueResolver.INSTANCE).build()).let {
        if (HELPER_RESULTS.size == 1 && placeholder.singleHelper()) HELPER_RESULTS.single() else it
    }
}

fun Handlebars.resolveObj(eval: Evaluator, placeholder: String?): Any? = placeholder?.trim()?.let {
    resolve(
        eval,
        if (placeholder.insideApostrophes()) placeholder.substring(1, placeholder.lastIndex) else placeholder
    )
}

private fun String.insideApostrophes() = startsWith("'") && endsWith("'")
private fun String.singleHelper() = startsWith("{{") && endsWith("}}")

interface ExamHelper : Helper<Any?> {
    val example: String
    val context: Map<String, Any?>
    val expected: Any?
    val options: Map<String, String>

    fun describe() =
        "$example will produce: ${expectedStr()} ${if (context.isEmpty()) "" else "(given context has variables: $context)"}"

    private fun expectedStr() = when (expected) {
        is String -> "\"$expected\""
        null -> null
        else -> "object of ${expected?.javaClass} = \"$expected\""
    }

    class InvocationFailed(name: String, context: Any?, options: Options, throwable: Throwable) :
        RuntimeException(
            "Invocation of {{$name}} (context: $context, options: ${options.fn.text()}) failed: ${throwable.message}",
            throwable
        )
}

fun Options.evaluator(): Evaluator = (this.context.model() as Evaluator)
