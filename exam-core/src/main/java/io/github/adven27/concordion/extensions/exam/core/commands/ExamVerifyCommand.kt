package io.github.adven27.concordion.extensions.exam.core.commands

import io.github.adven27.concordion.extensions.exam.core.html.Html
import org.concordion.api.AbstractCommand
import org.concordion.api.CommandCall
import org.concordion.api.Element
import org.concordion.api.Evaluator
import org.concordion.api.Fixture
import org.concordion.api.Result.FAILURE
import org.concordion.api.Result.SUCCESS
import org.concordion.api.ResultRecorder
import org.concordion.api.listener.AbstractElementEvent
import org.concordion.api.listener.AssertEqualsListener
import org.concordion.api.listener.AssertFailureEvent
import org.concordion.api.listener.AssertSuccessEvent
import java.util.EventListener

class VerifyFailureEvent<E>(el: Element, val expected: E, val fail: Throwable) : AbstractElementEvent(el)
class VerifySuccessEvent<E, A>(el: Element, val expected: E, val actual: A) : AbstractElementEvent(el)

interface VerifyListener<E, A> : EventListener {
    fun successReported(event: VerifySuccessEvent<E, A>)
    fun failureReported(event: VerifyFailureEvent<E>)
}

class FirstSuitableResultRenderer<E, A>(private vararg val renderers: SuitableResultRenderer<E, A>) :
    VerifyListener<E, A> {

    override fun successReported(event: VerifySuccessEvent<E, A>) =
        renderers.first { it.isSuitFor(event.element) }.successReported(event)

    override fun failureReported(event: VerifyFailureEvent<E>) =
        renderers.first { it.isSuitFor(event.element) }.failureReported(event)
}

abstract class SuitableResultRenderer<E, A> : VerifyListener<E, A> {
    abstract fun isSuitFor(element: Element): Boolean
}

class FirstSuitableSetUpListener<T>(private vararg val renderers: SuitableSetUpListener<T>) : SetUpListener<T> {
    override fun setUpCompleted(event: SetUpEvent<T>) =
        renderers.first { it.isSuitFor(event.element) }.setUpCompleted(event)
}

abstract class SuitableSetUpListener<T> : SetUpListener<T> {
    abstract fun isSuitFor(element: Element): Boolean
}

open class ExamVerifyCommand(
    name: String,
    tag: String,
    private val listener: AssertEqualsListener
) : ExamCommand(name, tag) {

    protected fun success(resultRecorder: ResultRecorder, element: Element) {
        resultRecorder.record(SUCCESS)
        listener.successReported(AssertSuccessEvent(element))
    }

    protected fun success(resultRecorder: ResultRecorder, element: Html) = success(resultRecorder, element.el())

    protected fun failure(resultRecorder: ResultRecorder, element: Element, actual: Any, expected: String) {
        resultRecorder.record(FAILURE)
        listener.failureReported(AssertFailureEvent(element, expected, actual))
    }

    protected fun failure(resultRecorder: ResultRecorder, element: Html, actual: Any, expected: String) =
        failure(resultRecorder, element.el(), actual, expected)

    protected fun ResultRecorder.pass(element: Html): Html = element.also { success(this, it) }

    protected fun ResultRecorder.failure(element: Html, actual: String, expected: String) =
        element.also { failure(this, it, actual, expected) }

    protected fun ResultRecorder.check(
        root: Html,
        actual: String,
        expected: String,
        test: (String, String) -> Boolean = { a, e -> a == e }
    ) = if (test(actual, expected)) {
        root.text(expected)
        this.pass(root)
    } else this.failure(root, actual, expected)
}

abstract class ExamSetUpCommand<T>(
    private val parser: CommandParser<T>, private val listener: SetUpListener<T>
) : AbstractCommand() {
    protected fun setUpCompleted(element: Element, target: T) = listener.setUpCompleted(SetUpEvent(element, target))

    abstract fun setUp(target: T, eval: Evaluator)

    override fun setUp(cmd: CommandCall, evaluator: Evaluator, resultRecorder: ResultRecorder, fixture: Fixture) {
        parser.parse(cmd, evaluator).apply {
            setUp(this, evaluator)
            setUpCompleted(cmd.element, this)
        }
    }
}

open class ExamAssertCommand<E, A>(
    private val commandParser: CommandParser<E>,
    private val verifier: AwaitVerifier<E, A>,
    private val actualProvider: ActualProvider<E, Pair<Boolean, A>>,
    private val listener: VerifyListener<E, A>
) : AbstractCommand() {

    override fun verify(cmd: CommandCall, eval: Evaluator, resultRecorder: ResultRecorder, fixture: Fixture) {
        commandParser.parse(cmd, eval).apply {
            verifier.verify(this) { actualProvider.provide(this) }
                .onSuccess { success(resultRecorder, cmd.element, it.actual, it.expected) }
                .onFailure { failure(resultRecorder, cmd.element, this, it) }
        }
    }

    protected fun success(recorder: ResultRecorder, element: Element, actual: A, expected: E) {
        recorder.record(SUCCESS)
        listener.successReported(VerifySuccessEvent(element, expected, actual))
    }

    protected fun failure(recorder: ResultRecorder, element: Element, expected: E, fail: Throwable) {
        recorder.record(FAILURE)
        listener.failureReported(VerifyFailureEvent(element, expected, fail))
    }
}
