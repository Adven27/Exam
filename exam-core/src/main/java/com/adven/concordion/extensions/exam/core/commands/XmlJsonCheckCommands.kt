package com.adven.concordion.extensions.exam.core.commands

import com.adven.concordion.extensions.exam.core.ContentVerifier
import com.adven.concordion.extensions.exam.core.ExamResultRenderer
import com.adven.concordion.extensions.exam.core.html.CLASS
import com.adven.concordion.extensions.exam.core.html.Html
import com.adven.concordion.extensions.exam.core.html.div
import com.adven.concordion.extensions.exam.core.html.divCollapse
import com.adven.concordion.extensions.exam.core.html.html
import com.adven.concordion.extensions.exam.core.html.span
import com.adven.concordion.extensions.exam.core.html.tableSlim
import com.adven.concordion.extensions.exam.core.html.td
import com.adven.concordion.extensions.exam.core.html.tr
import com.adven.concordion.extensions.exam.core.resolveJson
import com.adven.concordion.extensions.exam.core.resolveXml
import com.adven.concordion.extensions.exam.core.utils.content
import com.adven.concordion.extensions.exam.core.utils.prettyJson
import com.adven.concordion.extensions.exam.core.utils.prettyXml
import net.javacrumbs.jsonunit.JsonAssert
import net.javacrumbs.jsonunit.core.Configuration
import net.javacrumbs.jsonunit.core.Option
import net.javacrumbs.jsonunit.core.internal.Options
import org.concordion.api.CommandCall
import org.concordion.api.Evaluator
import org.concordion.api.Fixture
import org.concordion.api.ResultRecorder
import org.xmlunit.diff.NodeMatcher
import java.util.UUID

class XmlCheckCommand(name: String, tag: String, private val cfg: Configuration, private val nodeMatcher: NodeMatcher) :
    ExamVerifyCommand(name, tag, ExamResultRenderer()) {

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
            ContentVerifier.Xml().verify(exp, act, arrayOf(cfg, nodeMatcher))
            root.text(exp)
            resultRecorder.pass(root)
        } catch (expected: Throwable) {
            resultRecorder.failure(root, act, exp)
            root.below(
                span(expected.message, CLASS to "exceptionMessage")
            )
        }
    }
}

class JsonCheckCommand(name: String, tag: String, private val originalCfg: Configuration) :
    ExamVerifyCommand(name, tag, ExamResultRenderer()) {

    private lateinit var usedCfg: Configuration

    override fun verify(cmd: CommandCall, eval: Evaluator, resultRecorder: ResultRecorder, fixture: Fixture) {
        val root = cmd.html()
        usedCfg = originalCfg
        root.attr("jsonUnitOptions")?.let { attr -> overrideJsonUnitOption(attr) }
        val actual = eval.evaluate(root.attr("actual")).toString().prettyJson()
        val expected = eval.resolveJson(root.content(eval).trim()).prettyJson()
        checkJsonContent(
            actual,
            expected,
            resultRecorder,
            container(root, "json", root.takeAwayAttr("collapsable", "false").toBoolean())
        )
    }

    @Suppress("SpreadOperator")
    private fun overrideJsonUnitOption(attr: String) {
        val first = usedCfg.options.values().first()
        val other = usedCfg.options.values()
        other.remove(first)
        other.addAll(attr.split(";").filter { it.isNotEmpty() }.map { Option.valueOf(it) }.toSet())
        usedCfg = usedCfg.withOptions(Options(first, *other.toTypedArray()))
    }

    private fun checkJsonContent(act: String, exp: String, resultRecorder: ResultRecorder, root: Html) {
        try {
            JsonAssert.assertJsonEquals(exp, act, usedCfg)
            resultRecorder.pass(root)
            root.text(exp)
        } catch (expected: Throwable) {
            if (expected is AssertionError || expected is Exception) {
                resultRecorder.failure(root, act.prettyJson(), exp.prettyJson())
                root.below(
                    span(expected.message, CLASS to "exceptionMessage")
                )
            } else throw expected
        }
    }
}

private fun container(root: Html, type: String, collapsable: Boolean): Html {
    return if (collapsable) collapsableContainer(root, type) else fixedContainer(root, type)
}

private fun fixedContainer(root: Html, type: String): Html {
    val container = td().css("$type exp-body")
    root.removeChildren()(
        tableSlim()(
            tr()(container)
        )
    )
    return container
}

private fun collapsableContainer(root: Html, type: String): Html {
    val id = UUID.randomUUID().toString()
    val container = div("id" to id).css("$type file collapse")
    root.removeChildren()(
        tableSlim()(
            tr()(
                td("class" to "exp-body")(
                    div().style("position: relative")(
                        divCollapse("", id).css("fa fa-expand collapsed"),
                        container
                    )
                )
            )
        )
    )
    return container
}
