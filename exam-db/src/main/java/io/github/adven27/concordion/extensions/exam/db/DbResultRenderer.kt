package io.github.adven27.concordion.extensions.exam.db

import io.github.adven27.concordion.extensions.exam.core.ExamResultRenderer
import org.concordion.api.listener.AssertEqualsListener
import org.concordion.api.listener.AssertFalseListener
import org.concordion.api.listener.AssertSuccessEvent
import org.concordion.api.listener.AssertTrueListener

class DbResultRenderer : ExamResultRenderer(), AssertEqualsListener, AssertTrueListener, AssertFalseListener {
    override fun successReported(event: AssertSuccessEvent) {
        event.element.addStyleClass("table-success").appendNonBreakingSpaceIfBlank()
    }
}
