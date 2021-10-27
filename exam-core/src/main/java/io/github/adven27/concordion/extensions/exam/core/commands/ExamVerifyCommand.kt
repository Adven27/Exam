package io.github.adven27.concordion.extensions.exam.core.commands

import io.github.adven27.concordion.extensions.exam.core.html.Html
import org.concordion.api.AbstractCommand
import org.concordion.api.Element
import org.concordion.api.Result.FAILURE
import org.concordion.api.Result.SUCCESS
import org.concordion.api.ResultRecorder
import org.concordion.api.listener.AbstractElementEvent
import org.concordion.api.listener.AssertEqualsListener
import org.concordion.api.listener.AssertFailureEvent
import org.concordion.api.listener.AssertSuccessEvent
import java.util.EventListener

class VerifyFailureEvent<E>(el: Element, val expected: E, val fail: Throwable) :
    AbstractElementEvent(el)

class VerifySuccessEvent<E, A>(el: Element, val expected: E, val actual: A) : AbstractElementEvent(el)

interface VerifyListener<E, A> : EventListener {
    fun successReported(event: VerifySuccessEvent<E, A>)
    fun failureReported(event: VerifyFailureEvent<E>)
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

open class ExamAssertCommand<E, A>(private val listener: VerifyListener<E, A>) : AbstractCommand() {

    protected fun success(resultRecorder: ResultRecorder, element: Element, actual: A, expected: E) {
        resultRecorder.record(SUCCESS)
        listener.successReported(VerifySuccessEvent(element, expected, actual))
    }

    protected fun success(resultRecorder: ResultRecorder, element: Html, actual: A, expected: E) =
        success(resultRecorder, element.el(), actual, expected)

    protected fun failure(recorder: ResultRecorder, element: Element, expected: E, fail: Throwable) {
        recorder.record(FAILURE)
        listener.failureReported(VerifyFailureEvent(element, expected, fail))
    }

    protected fun failure(recorder: ResultRecorder, element: Html, expected: E, fail: Throwable) =
        failure(recorder, element.el(), expected, fail)

    protected fun ResultRecorder.pass(element: Html, actual: A, expected: E): Html =
        element.also { success(this, it, actual, expected) }

    protected fun ResultRecorder.failure(element: Html, expected: E, fail: Throwable) =
        element.also { failure(this, it, expected, fail) }
}
