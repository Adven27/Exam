package io.github.adven27.concordion.extensions.exam.core.handlebars

import com.github.jknack.handlebars.ValueResolver
import org.concordion.api.Evaluator
import org.concordion.internal.InvalidExpressionException

enum class EvaluatorValueResolver : ValueResolver {
    INSTANCE;

    override fun resolve(context: Any, name: String): Any {
        var value: Any? = null
        if (context is Evaluator) {
            value = context.getVariable("#$name") ?: context.tryEval(name)
        }
        return value ?: ValueResolver.UNRESOLVED
    }

    private fun Evaluator.tryEval(name: String): Any? = try {
        evaluate(name)
    } catch (e: InvalidExpressionException) {
        null
    }

    override fun resolve(context: Any): Any = context as? Evaluator ?: ValueResolver.UNRESOLVED

    override fun propertySet(context: Any?): MutableSet<MutableMap.MutableEntry<String, Any>> {
        if (context is Evaluator) {
            throw UnsupportedOperationException()
        }
        return HashSet()
    }
}
