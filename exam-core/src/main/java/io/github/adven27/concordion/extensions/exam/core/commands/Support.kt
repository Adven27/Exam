package io.github.adven27.concordion.extensions.exam.core.commands

import io.github.adven27.concordion.extensions.exam.core.commands.Verifier.Success
import io.github.adven27.concordion.extensions.exam.core.utils.After
import io.github.adven27.concordion.extensions.exam.core.utils.Before
import org.concordion.api.CommandCall
import org.concordion.api.Element
import org.concordion.api.Evaluator
import org.concordion.api.listener.AbstractElementEvent
import java.util.EventListener
import java.util.UUID
import java.util.regex.Pattern

interface CommandParser<T> {
    fun parse(command: CommandCall, evaluator: Evaluator): T
}

interface SuitableCommandParser<T> : CommandParser<T> {
    fun isSuitFor(element: Element): Boolean
}

class FirstSuitableCommandParser<T>(private vararg val parsers: SuitableCommandParser<T>) : CommandParser<T> {
    override fun parse(command: CommandCall, evaluator: Evaluator): T =
        parsers.first { it.isSuitFor(command.element) }.parse(command, evaluator)
}

interface ActualProvider<S, R> {
    fun provide(source: S): R
}

interface Verifier<E, A> {
    fun verify(eval: Evaluator, expected: E, actual: A): Result<Success<E, A>>
    data class Success<E, A>(val expected: E, val actual: A)
}

interface AwaitVerifier<E, A> : Verifier<E, A> {
    fun verify(eval: Evaluator, expected: E, getActual: () -> Pair<Boolean, A>): Result<Success<E, A>>
}

interface SetUpListener<T> : EventListener {
    fun setUpCompleted(event: SetUpEvent<T>)
}

class SetUpEvent<T>(element: Element, val target: T) : AbstractElementEvent(element)

fun String.expression() = substring(indexOf("}") + 1).trim()

fun matchesRegex(pattern: String, actualValue: Any?): Boolean =
    if (actualValue == null) false else Pattern.compile(pattern).matcher(actualValue.toString()).matches()

fun matchesAnyNumber(actual: Any?) = matchesRegex("^\\d+\$", actual)
fun matchesAnyString(actual: Any?) = matchesRegex("^\\w+\$", actual)
fun matchesAnyBoolean(actual: Any?) = actual is String? && actual?.toBooleanStrictOrNull()?.let { true } ?: false

fun matchesAnyUuid(a: Any?) = a is String && try {
    UUID.fromString(a)
    true
} catch (ignore: Exception) {
    false
}

fun <T> checkAndSet(
    eval: Evaluator,
    actual: T,
    expected: String,
    check: (actual: T, expected: String) -> Boolean = { a, e ->
        when {
            e == "\${text-unit.any-string}" -> matchesAnyString(a)
            e == "\${text-unit.any-number}" -> matchesAnyNumber(a)
            e == "\${text-unit.any-boolean}" -> matchesAnyBoolean(a)
            e == "\${text-unit.ignore}" -> true
            e.startsWith("\${text-unit.regex}") -> matchesRegex(e.substringAfter("}"), a)
            e.startsWith("\${text-unit.matches:after}") -> After().apply { setParameter(e.substringAfter("}")) }.matches(a!!)
            e.startsWith("\${text-unit.matches:before}") -> Before().apply { setParameter(e.substringAfter("}")) }.matches(a!!)
            else -> a == e
        }
    }
): Boolean {
    val split = expected.split(">>")
    if (split.size > 1) eval.setVariable("#${split[1]}", actual)
    return check(actual, split[0])
}
