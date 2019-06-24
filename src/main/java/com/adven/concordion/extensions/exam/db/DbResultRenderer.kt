package com.adven.concordion.extensions.exam.db

import com.adven.concordion.extensions.exam.ws.RestResultRenderer
import org.concordion.api.listener.AssertEqualsListener
import org.concordion.api.listener.AssertFalseListener
import org.concordion.api.listener.AssertSuccessEvent
import org.concordion.api.listener.AssertTrueListener

class DbResultRenderer : RestResultRenderer(), AssertEqualsListener, AssertTrueListener, AssertFalseListener {
    override fun successReported(event: AssertSuccessEvent) {
        event.element.addStyleClass("table-success").appendNonBreakingSpaceIfBlank()
    }
}