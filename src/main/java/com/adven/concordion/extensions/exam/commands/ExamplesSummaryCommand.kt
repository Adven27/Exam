package com.adven.concordion.extensions.exam.commands

import com.adven.concordion.extensions.exam.html.Html
import com.adven.concordion.extensions.exam.html.buttonCollapse
import com.adven.concordion.extensions.exam.html.div
import com.adven.concordion.extensions.exam.html.h
import org.concordion.api.CommandCall
import org.concordion.api.Evaluator
import org.concordion.api.ResultRecorder

class ExamplesSummaryCommand(name: String, tag: String) : ExamCommand(name, tag) {

    override fun setUp(cmd: CommandCall?, evaluator: Evaluator?, resultRecorder: ResultRecorder?) {
        val id = "summary"
        val el = Html(cmd!!.element)
        val title = el.takeAwayAttr("title", evaluator)
        el(
            h(4, if (title == null || title.isEmpty()) "Summary" else title)(
                buttonCollapse("collapse", id)
            ),
            div("id" to id).css("collapse show")
        )
    }
}