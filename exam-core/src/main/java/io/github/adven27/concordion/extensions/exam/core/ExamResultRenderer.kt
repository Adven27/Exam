package io.github.adven27.concordion.extensions.exam.core

import org.concordion.api.Element
import org.concordion.api.listener.AssertEqualsListener
import org.concordion.api.listener.AssertFailureEvent
import org.concordion.api.listener.AssertFalseListener
import org.concordion.api.listener.AssertSuccessEvent
import org.concordion.api.listener.AssertTrueListener

open class ExamResultRenderer : AssertEqualsListener, AssertTrueListener, AssertFalseListener {

    override fun failureReported(event: AssertFailureEvent) {
        val element = event.element
        addFailMarker(event)
        element.addStyleClass("rest-failure")

        val expected = Element("del")
        expected.addStyleClass("expected")
        expected.appendText(event.expected)
        element.moveChildrenTo(expected)
        element.appendChild(expected)
        expected.appendNonBreakingSpaceIfBlank()

        val actual = Element("ins")
        actual.addStyleClass("actual")
        actual.appendText(actualText(event))
        actual.appendNonBreakingSpaceIfBlank()

        element.appendText("\n")
        element.appendChild(actual)
    }

    private fun addFailMarker(event: AssertFailureEvent) {
        event.element.appendChild(Element("fail").apply { addAttribute("style", "display: none;") })
    }

    protected open fun actualText(event: AssertFailureEvent): String {
        return event.actual.toStringOr("(null)")
    }

    override fun successReported(event: AssertSuccessEvent) {
        event.element.addStyleClass("rest-success").appendNonBreakingSpaceIfBlank()
    }
}

fun Any?.toStringOr(absent: String): String = this?.toString() ?: absent
