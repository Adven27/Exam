package com.adven.concordion.extensions.exam.files

import com.adven.concordion.extensions.exam.core.ExamResultRenderer
import com.adven.concordion.extensions.exam.core.toStringOr
import org.concordion.api.listener.AssertFailureEvent

class FilesResultRenderer : ExamResultRenderer() {

    override fun actualText(event: AssertFailureEvent): String {
        return event.actual.toStringOr("(absent)") + if (event.expected == null) " (surplus)" else ""
    }
}
