package io.github.adven27.concordion.extensions.exam.files

import io.github.adven27.concordion.extensions.exam.core.ExamResultRenderer
import io.github.adven27.concordion.extensions.exam.core.toStringOr
import org.concordion.api.listener.AssertFailureEvent

class FilesResultRenderer : ExamResultRenderer() {

    override fun actualText(event: AssertFailureEvent): String {
        return event.actual.toStringOr("(absent)") + if (event.expected == null) " (surplus)" else ""
    }
}
