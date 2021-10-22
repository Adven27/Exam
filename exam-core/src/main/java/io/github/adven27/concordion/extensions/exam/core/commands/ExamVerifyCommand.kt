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

class VerifyFailureEvent(el: Element, val expected: Any?, val actual: Any?, error: Throwable) : AbstractElementEvent(el)
class VerifySuccessEvent(el: Element, val expected: Any? = null, val actual: Any? = null) : AbstractElementEvent(el)

interface VerifyListener : EventListener {
    fun successReported(event: VerifySuccessEvent)
    fun failureReported(event: VerifyFailureEvent)
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

open class ExamAssertCommand(private val listener: VerifyListener) : AbstractCommand() {

    protected fun success(resultRecorder: ResultRecorder, element: Element) {
        resultRecorder.record(SUCCESS)
        listener.successReported(VerifySuccessEvent(element))
    }

    protected fun success(resultRecorder: ResultRecorder, element: Html) = success(resultRecorder, element.el())

    protected fun failure(
        resultRecorder: ResultRecorder,
        element: Element,
        actual: Any,
        expected: Any,
        error: Throwable
    ) {
        resultRecorder.record(FAILURE)
        listener.failureReported(VerifyFailureEvent(element, expected, actual, error))
    }

    protected fun failure(
        resultRecorder: ResultRecorder,
        element: Html,
        actual: Any,
        expected: Any,
        error: Throwable
    ) = failure(resultRecorder, element.el(), actual, expected, error)

    protected fun ResultRecorder.pass(element: Html): Html = element.also { success(this, it) }

    protected fun ResultRecorder.failure(element: Html, actual: String, expected: String, error: Throwable) =
        element.also { failure(this, it, actual, expected, error) }
}
