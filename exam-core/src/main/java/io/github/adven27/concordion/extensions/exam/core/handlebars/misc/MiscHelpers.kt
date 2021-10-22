package io.github.adven27.concordion.extensions.exam.core.handlebars.misc

import com.github.jknack.handlebars.Options
import io.github.adven27.concordion.extensions.exam.core.handlebars.ExamHelper
import io.github.adven27.concordion.extensions.exam.core.handlebars.HB_RESULT
import io.github.adven27.concordion.extensions.exam.core.handlebars.evaluator
import io.github.adven27.concordion.extensions.exam.core.readFile
import io.github.adven27.concordion.extensions.exam.core.resolve
import io.github.adven27.concordion.extensions.exam.core.resolveToObj
import io.github.adven27.concordion.extensions.exam.core.toDate
import java.time.LocalDate

@Suppress("EnumNaming")
enum class MiscHelpers(
    override val example: String,
    override val context: Map<String, Any?> = emptyMap(),
    override val expected: Any? = "",
    override val options: Map<String, String> = emptyMap()
) : ExamHelper {
    set("""{{set 1 "someVar"}}""", mapOf(), 1, mapOf()) {
        override fun invoke(context: Any?, options: Options): Any? = options.params.map {
            options.evaluator().setVariable("#$it", context)
        }.let { context }
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
    },
    prop("{{prop 'system.property' 'optional default'}}", emptyMap(), "optional default") {
        override fun invoke(context: Any?, options: Options) = System.getProperty(context.toString(), options.param(0))
    },
    env("{{env 'env.property' 'optional default'}}", emptyMap(), "optional default") {
        override fun invoke(context: Any?, options: Options) = System.getenv(context.toString()) ?: options.param(0)
    };

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
        val unexpected = options.hash.keys - this.options.keys
        if (unexpected.isNotEmpty()) throw IllegalArgumentException(
            "Wrong options for helper '${options.fn.text()}': found '$unexpected', expected any of '${this.options}'"
        )
    }

    override fun toString() = describe()
    abstract operator fun invoke(context: Any?, options: Options): Any?
}
