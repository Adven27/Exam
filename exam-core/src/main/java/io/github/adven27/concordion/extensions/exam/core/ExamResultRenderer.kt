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

        val expected = expectedElement(event)
        element.moveChildrenTo(expected)
        element.appendChild(expected)

        element.appendText("\n")
        element.appendChild(actualElement(event))
    }

    private fun expectedElement(event: AssertFailureEvent) = Element("del").apply {
        addStyleClass("expected")
        appendText(event.expected)
        appendNonBreakingSpaceIfBlank()
    }

    private fun actualElement(event: AssertFailureEvent) = Element("ins").apply {
        addStyleClass("actual")
        appendText(actualText(event))
        appendNonBreakingSpaceIfBlank()
    }

    private fun addFailMarker(event: AssertFailureEvent) =
        event.element.appendChild(Element("fail").apply { addAttribute("style", "display: none;") })

    protected open fun actualText(event: AssertFailureEvent): String = event.actual.toStringOr("(null)")

    override fun successReported(event: AssertSuccessEvent) {
        event.element.addStyleClass("rest-success").appendNonBreakingSpaceIfBlank()
    }
}

fun Any?.toStringOr(absent: String): String = this?.toString() ?: absent
