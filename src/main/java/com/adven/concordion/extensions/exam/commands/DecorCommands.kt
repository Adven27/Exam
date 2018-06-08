package com.adven.concordion.extensions.exam.commands

import com.adven.concordion.extensions.exam.html.*
import org.concordion.api.CommandCall
import org.concordion.api.Evaluator
import org.concordion.api.ResultRecorder

class GivenCommand(tag: String) : ExamCommand("given", tag) {
    override fun setUp(cmd: CommandCall?, evaluator: Evaluator?, resultRecorder: ResultRecorder?) {
        cmd.htmlCss("bd-callout bd-callout-info")
    }
}

class WhenCommand(tag: String) : ExamCommand("when", tag) {
    override fun setUp(cmd: CommandCall?, evaluator: Evaluator?, resultRecorder: ResultRecorder?) {
        cmd.htmlCss("bd-callout bd-callout-warning")
    }
}

class ThenCommand(tag: String) : ExamCommand("then", tag) {
    override fun setUp(cmd: CommandCall?, evaluator: Evaluator?, resultRecorder: ResultRecorder?) {
        cmd.htmlCss("bd-callout bd-callout-success")
    }
}

class ScrollToTopCommand(name: String, tag: String) : ExamCommand(name, tag) {
    override fun setUp(cmd: CommandCall?, evaluator: Evaluator?, resultRecorder: ResultRecorder?) {
        cmd.html()(
            button("", ID to "btnToTop", ONCLICK to "topFunction()")(
                italic("").css("fa fa-arrow-up fa-3x")))
    }
}

class ExamplesSummaryCommand(name: String, tag: String) : ExamCommand(name, tag) {
    override fun setUp(cmd: CommandCall?, evaluator: Evaluator?, resultRecorder: ResultRecorder?) {
        val id = "summary"
        val html = cmd.html()
        val title = html.takeAwayAttr("title", evaluator)
        html(
            h(4, title ?: "Summary")(
                buttonCollapse("collapse", id)),
            div(ID to id, CLASS to "collapse show"))
    }
}