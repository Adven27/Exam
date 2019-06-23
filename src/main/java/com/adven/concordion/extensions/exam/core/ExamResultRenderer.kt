package com.adven.concordion.extensions.exam.core

import org.concordion.api.Element
import org.concordion.api.listener.*

open class ExamResultRenderer : AssertEqualsListener, AssertTrueListener, AssertFalseListener {

    override fun failureReported(event: AssertFailureEvent) {
        val element = event.element
        element.addStyleClass("rest-failure")

        val expected = Element("del")
        expected.addStyleClass("expected")
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

    protected open fun actualText(event: AssertFailureEvent): String {
        return event.actual.toStringOr("(null)")
    }

    override fun successReported(event: AssertSuccessEvent) {
        event.element.addStyleClass("rest-success").appendNonBreakingSpaceIfBlank()
    }
}

fun Any?.toStringOr(absent: String): String = this?.toString() ?: absent