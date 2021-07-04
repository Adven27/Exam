package io.github.adven27.concordion.extensions.exam.core.commands

import io.github.adven27.concordion.extensions.exam.core.html.CLASS
import io.github.adven27.concordion.extensions.exam.core.html.Html
import io.github.adven27.concordion.extensions.exam.core.html.ID
import io.github.adven27.concordion.extensions.exam.core.html.buttonCollapse
import io.github.adven27.concordion.extensions.exam.core.html.div
import io.github.adven27.concordion.extensions.exam.core.html.h
import io.github.adven27.concordion.extensions.exam.core.html.html
import io.github.adven27.concordion.extensions.exam.core.html.tag
import org.concordion.api.CommandCall
import org.concordion.api.Evaluator
import org.concordion.api.Fixture
import org.concordion.api.ResultRecorder

class MainCommand() : ExamCommand("main", "div") {
    override fun setUp(cmd: CommandCall?, evaluator: Evaluator?, resultRecorder: ResultRecorder?, fixture: Fixture) {
        val div1 = div("class" to "bd-content ps-lg-4")
        val nav = div("class" to "bd-toc mt-4 mb-5 my-md-0 ps-xl-3 mb-lg-5 text-muted")(
            tag("strong").css("d-block h6 my-2 pb-2 border-bottom").text("On this page")
        )
        val div = div("class" to "container-xxl my-md-4 bd-layout")(
            Html("main").css("bd-main order-1")(
                nav, div1
            )
        )

        cmd.html().css("container-fluid").moveChildrenTo(div1)(div)
    }
}

class GivenCommand : ExamCommand("given", "div") {
    override fun setUp(cmd: CommandCall?, evaluator: Evaluator?, resultRecorder: ResultRecorder?, fixture: Fixture) {
        cmd.html().above(
            tag("hr")
        ).above(tag("p").css("lead text-primary").text("Given"))
    }
}

class WhenCommand(tag: String) : ExamCommand("when", tag) {
    override fun setUp(cmd: CommandCall?, evaluator: Evaluator?, resultRecorder: ResultRecorder?, fixture: Fixture) {
        cmd.html().above(
            tag("hr")
        ).above(tag("p").css("lead text-dark").text("When"))
    }
}

class ThenCommand(tag: String) : ExamCommand("then", tag) {
    override fun setUp(cmd: CommandCall?, evaluator: Evaluator?, resultRecorder: ResultRecorder?, fixture: Fixture) {
        cmd.html().above(
            tag("hr")
        ).above(tag("p").css("lead text-success").text("Then"))
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
