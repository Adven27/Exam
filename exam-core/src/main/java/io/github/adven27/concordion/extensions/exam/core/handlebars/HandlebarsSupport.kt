package io.github.adven27.concordion.extensions.exam.core.handlebars

import com.github.jknack.handlebars.Context
import com.github.jknack.handlebars.EscapingStrategy.NOOP
import com.github.jknack.handlebars.Handlebars
import com.github.jknack.handlebars.Helper
import com.github.jknack.handlebars.Options
import io.github.adven27.concordion.extensions.exam.core.handlebars.date.DateHelpers
import io.github.adven27.concordion.extensions.exam.core.handlebars.matchers.MatcherHelpers
import io.github.adven27.concordion.extensions.exam.core.handlebars.misc.MiscHelpers
import org.concordion.api.Evaluator
import java.util.concurrent.atomic.AtomicReference

val HB_RESULT: AtomicReference<Any?> = AtomicReference()

val HANDLEBARS: Handlebars = Handlebars()
    .with(NOOP)
    .with { value, next ->
        HB_RESULT.set(value)
        return@with next.format(value)
    }
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

fun Handlebars.resolve(eval: Any?, placeholder: String): String = compileInline(placeholder).apply(
    Context.newBuilder(eval).resolver(EvaluatorValueResolver.INSTANCE).build()
)

fun Handlebars.resolveObj(eval: Evaluator, placeholder: String): Any? {
    HB_RESULT.set(placeholder)
    resolve(eval, placeholder)
    return HB_RESULT.get()
}

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
