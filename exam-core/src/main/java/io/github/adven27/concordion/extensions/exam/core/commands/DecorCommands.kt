package io.github.adven27.concordion.extensions.exam.core.commands

import io.github.adven27.concordion.extensions.exam.core.html.Html
import io.github.adven27.concordion.extensions.exam.core.html.html
import io.github.adven27.concordion.extensions.exam.core.html.tag
import org.concordion.api.CommandCall
import org.concordion.api.Evaluator
import org.concordion.api.Fixture
import org.concordion.api.ResultRecorder

class GivenCommand : ExamCommand("given", "div") {
    override fun setUp(cmd: CommandCall?, evaluator: Evaluator?, resultRecorder: ResultRecorder?, fixture: Fixture) =
        pLeadAndHr(cmd.html().css("given"), "text-primary", "Given")
}

class WhenCommand : ExamCommand("when", "div") {
    override fun setUp(cmd: CommandCall?, evaluator: Evaluator?, resultRecorder: ResultRecorder?, fixture: Fixture) =
        pLeadAndHr(cmd.html().css("when"), "text-warning", "When")
}

class ThenCommand : ExamCommand("then", "div") {
    override fun setUp(cmd: CommandCall?, evaluator: Evaluator?, resultRecorder: ResultRecorder?, fixture: Fixture) =
        pLeadAndHr(cmd.html().css("then"), "text-success", "Then")
}

private fun pLeadAndHr(html: Html, style: String, title: String) {
    html.prependChild(tag("hr")).prependChild(tag("p").css("lead $style").text(title))
}
