package io.github.adven27.concordion.extensions.exam.core.commands

import io.github.adven27.concordion.extensions.exam.core.html.html
import io.github.adven27.concordion.extensions.exam.core.html.tag
import org.concordion.api.CommandCall
import org.concordion.api.Evaluator
import org.concordion.api.Fixture
import org.concordion.api.ResultRecorder

class GivenCommand : ExamCommand("given", "div") {
    override fun setUp(cmd: CommandCall?, evaluator: Evaluator?, resultRecorder: ResultRecorder?, fixture: Fixture) =
        pLeadAndHr(cmd, "text-primary", "Given")
}

class WhenCommand : ExamCommand("when", "div") {
    override fun setUp(cmd: CommandCall?, evaluator: Evaluator?, resultRecorder: ResultRecorder?, fixture: Fixture) =
        pLeadAndHr(cmd, "text-warning", "When")
}

class ThenCommand : ExamCommand("then", "div") {
    override fun setUp(cmd: CommandCall?, evaluator: Evaluator?, resultRecorder: ResultRecorder?, fixture: Fixture) =
        pLeadAndHr(cmd, "text-success", "Then")
}

private fun pLeadAndHr(cmd: CommandCall?, style: String, title: String) {
    cmd.html()
        .prependChild(tag("hr"))
        .prependChild(tag("p").css("lead $style").text(title))
}
