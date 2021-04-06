package io.github.adven27.concordion.extensions.exam.core.commands

import io.github.adven27.concordion.extensions.exam.core.html.CLASS
import io.github.adven27.concordion.extensions.exam.core.html.ID
import io.github.adven27.concordion.extensions.exam.core.html.buttonCollapse
import io.github.adven27.concordion.extensions.exam.core.html.div
import io.github.adven27.concordion.extensions.exam.core.html.h
import io.github.adven27.concordion.extensions.exam.core.html.html
import io.github.adven27.concordion.extensions.exam.core.html.htmlCss
import org.concordion.api.CommandCall
import org.concordion.api.Evaluator
import org.concordion.api.Fixture
import org.concordion.api.ResultRecorder

class GivenCommand(tag: String) : ExamCommand("given", tag) {
    override fun setUp(cmd: CommandCall?, evaluator: Evaluator?, resultRecorder: ResultRecorder?, fixture: Fixture) {
        cmd.htmlCss("bd-callout bd-callout-info")
    }
}

class WhenCommand(tag: String) : ExamCommand("when", tag) {
    override fun setUp(cmd: CommandCall?, evaluator: Evaluator?, resultRecorder: ResultRecorder?, fixture: Fixture) {
        cmd.htmlCss("bd-callout bd-callout-warning")
    }
}

class ThenCommand(tag: String) : ExamCommand("then", tag) {
    override fun setUp(cmd: CommandCall?, evaluator: Evaluator?, resultRecorder: ResultRecorder?, fixture: Fixture) {
        cmd.htmlCss("bd-callout bd-callout-success")
    }
}

class ExamplesSummaryCommand(name: String, tag: String) : ExamCommand(name, tag) {
    override fun setUp(cmd: CommandCall?, evaluator: Evaluator?, resultRecorder: ResultRecorder?, fixture: Fixture) {
        val id = "summary"
        val html = cmd.html()
        val title = html.takeAwayAttr("title", evaluator)
        html(
            h(4, title ?: "Summary")(
                buttonCollapse("collapse", id)
            ),
            div(ID to id, CLASS to "collapse show")
        )
    }
}
