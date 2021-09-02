package io.github.adven27.concordion.extensions.exam.core.commands

import io.github.adven27.concordion.extensions.exam.core.ContentVerifier
import io.github.adven27.concordion.extensions.exam.core.ExamResultRenderer
import io.github.adven27.concordion.extensions.exam.core.content
import io.github.adven27.concordion.extensions.exam.core.html.CLASS
import io.github.adven27.concordion.extensions.exam.core.html.Html
import io.github.adven27.concordion.extensions.exam.core.html.div
import io.github.adven27.concordion.extensions.exam.core.html.divCollapse
import io.github.adven27.concordion.extensions.exam.core.html.generateId
import io.github.adven27.concordion.extensions.exam.core.html.html
import io.github.adven27.concordion.extensions.exam.core.html.pre
import io.github.adven27.concordion.extensions.exam.core.html.table
import io.github.adven27.concordion.extensions.exam.core.html.td
import io.github.adven27.concordion.extensions.exam.core.html.tr
import io.github.adven27.concordion.extensions.exam.core.prettyJson
import io.github.adven27.concordion.extensions.exam.core.prettyXml
import io.github.adven27.concordion.extensions.exam.core.resolveJson
import io.github.adven27.concordion.extensions.exam.core.resolveXml
import org.concordion.api.CommandCall
import org.concordion.api.Evaluator
import org.concordion.api.Fixture
import org.concordion.api.ResultRecorder

class XmlCheckCommand(tag: String, private val verifier: ContentVerifier) :
    ExamVerifyCommand("xml-check", tag, ExamResultRenderer()) {

    override fun verify(cmd: CommandCall, eval: Evaluator, resultRecorder: ResultRecorder, fixture: Fixture) {
        val root = cmd.html()
        val actual = eval.evaluate(root.takeAwayAttr("actual")).toString().prettyXml()
        val expected = eval.resolveXml(root.content(eval).trim()).prettyXml()
        checkXmlContent(
            actual,
            expected,
            resultRecorder,
            container(root, "xml", root.takeAwayAttr("collapsable", "false").toBoolean())
        )
    }

    private fun checkXmlContent(act: String, exp: String, resultRecorder: ResultRecorder, root: Html) {
        try {
            verifier.verify(exp, act)
            root.text(exp)
            resultRecorder.pass(root)
        } catch (expected: Throwable) {
            resultRecorder.failure(root, act, exp)
            root.below(
                pre(expected.message, CLASS to "exceptionMessage")
            )
        }
    }
}

class JsonCheckCommand(tag: String, private val verifier: ContentVerifier) :
    ExamVerifyCommand("json-check", tag, ExamResultRenderer()) {

    override fun verify(cmd: CommandCall, eval: Evaluator, resultRecorder: ResultRecorder, fixture: Fixture) {
        val root = cmd.html()
        val actual = eval.evaluate(root.attr("actual")).toString().prettyJson()
        val expected = eval.resolveJson(root.content()).prettyJson()
        checkJsonContent(
            actual,
            expected,
            resultRecorder,
            container(root, "json", root.takeAwayAttr("collapsable", "false").toBoolean())
        )
    }

    private fun checkJsonContent(act: String, exp: String, resultRecorder: ResultRecorder, root: Html) {
        verifier.verify(exp, act).fail.map { f ->
            resultRecorder.failure(root, f.actual, f.expected)
            root.below(
                pre(f.details, CLASS to "exceptionMessage")
            )
        }.orElseGet {
            root.text(exp)
            resultRecorder.pass(root)
        }
    }
}

private fun container(root: Html, type: String, collapsable: Boolean): Html {
    return if (collapsable) collapsableContainer(root, type) else fixedContainer(root, type)
}

private fun fixedContainer(root: Html, type: String): Html {
    val container = td().css("$type exp-body")
    root.removeChildren()(
        table()(
            tr()(container)
        )
    )
    return container
}

private fun collapsableContainer(root: Html, type: String): Html {
    val id = generateId()
    val container = div("id" to id).css("$type file collapse")
    root.removeChildren()(
        table()(
            tr()(
                td("class" to "exp-body")(
                    div().style("position: relative")(
                        divCollapse("", id),
                        container
                    )
                )
            )
        )
    )
    return container
}
