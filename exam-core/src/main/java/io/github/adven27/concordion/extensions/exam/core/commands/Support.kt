package io.github.adven27.concordion.extensions.exam.core.commands

import io.github.adven27.concordion.extensions.exam.core.commands.Verifier.Success
import org.concordion.api.CommandCall
import org.concordion.api.Element
import org.concordion.api.Evaluator
import org.concordion.api.listener.AbstractElementEvent
import java.util.EventListener

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
    fun verify(expected: E, actual: A): Result<Success<E, A>>
    data class Success<E, A>(val expected: E, val actual: A)
}

interface AwaitVerifier<E, A> : Verifier<E, A> {
    fun verify(expected: E, getActual: () -> Pair<Boolean, A>): Result<Success<E, A>>
}

interface SetUpListener<T> : EventListener {
    fun setUpCompleted(event: SetUpEvent<T>)
}

class SetUpEvent<T>(element: Element, val target: T) : AbstractElementEvent(element)
