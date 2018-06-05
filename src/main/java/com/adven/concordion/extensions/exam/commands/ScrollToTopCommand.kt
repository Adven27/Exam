package com.adven.concordion.extensions.exam.commands

import com.adven.concordion.extensions.exam.html.Html
import com.adven.concordion.extensions.exam.html.button
import com.adven.concordion.extensions.exam.html.italic
import org.concordion.api.CommandCall
import org.concordion.api.Evaluator
import org.concordion.api.ResultRecorder

class ScrollToTopCommand(name: String, tag: String) : ExamCommand(name, tag) {

    override fun setUp(cmd: CommandCall?, evaluator: Evaluator?, resultRecorder: ResultRecorder?) {
        Html(cmd!!.element)(
            button("", "id" to "btnToTop", "onclick" to "topFunction()")(
                italic("").css("fa fa-arrow-up fa-3x")
            )
        )
    }
}
