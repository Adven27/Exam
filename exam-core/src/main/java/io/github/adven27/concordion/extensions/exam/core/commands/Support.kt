package io.github.adven27.concordion.extensions.exam.core.commands

import org.awaitility.core.ConditionFactory
import org.concordion.api.CommandCall
import org.concordion.api.Element
import org.concordion.api.Evaluator

interface CommandParser<T> {
    fun parse(command: CommandCall, evaluator: Evaluator): T
}

abstract class SuitableCommandParser<T> : CommandParser<T> {
    abstract fun isSuitFor(element: Element): Boolean
}

class FirsSuitableCommandParser<T>(private vararg val parsers: SuitableCommandParser<T>) : CommandParser<T> {
    override fun parse(command: CommandCall, evaluator: Evaluator): T =
        parsers.first { it.isSuitFor(command.element) }.parse(command, evaluator)
}

interface ActualProvider<S, R> {
    fun provide(source: S): R
}

interface Verifier<E, A, R> {
    fun verify(expected: E, actual: A): Result<R>
}

interface AwaitVerifier<E, A, R> : Verifier<E, A, R> {
    fun verify(expected: E, getActual: () -> Pair<Boolean, A>, await: ConditionFactory): Result<R>
}
