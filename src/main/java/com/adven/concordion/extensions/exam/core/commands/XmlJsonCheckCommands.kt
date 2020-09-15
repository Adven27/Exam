package com.adven.concordion.extensions.exam.core.commands

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
import com.adven.concordion.extensions.exam.core.utils.equalToXml
import com.adven.concordion.extensions.exam.core.utils.prettyJson
import com.adven.concordion.extensions.exam.core.utils.prettyXml
import com.adven.concordion.extensions.exam.ws.RestResultRenderer
import net.javacrumbs.jsonunit.JsonAssert
import net.javacrumbs.jsonunit.core.Configuration
import net.javacrumbs.jsonunit.core.Option
import net.javacrumbs.jsonunit.core.internal.Options
import org.concordion.api.CommandCall
import org.concordion.api.Evaluator
import org.concordion.api.ResultRecorder
import org.xmlunit.diff.NodeMatcher
import java.util.UUID

class XmlCheckCommand(name: String, tag: String, private val cfg: Configuration, private val nodeMatcher: NodeMatcher) :
    ExamVerifyCommand(name, tag, RestResultRenderer()) {

    override fun verify(cmd: CommandCall, eval: Evaluator, resultRecorder: ResultRecorder) {
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

    private fun checkXmlContent(actual: String, expected: String, resultRecorder: ResultRecorder, root: Html) {
        try {
            actual.equalToXml(expected, nodeMatcher, cfg)
            root.text(expected)
            resultRecorder.pass(root)
        } catch (e: Throwable) {
            resultRecorder.failure(root, actual, expected)
            root.below(
                span(e.message, CLASS to "exceptionMessage")
            )
        }
    }
}

class JsonCheckCommand(name: String, tag: String, private val originalCfg: Configuration) :
    ExamVerifyCommand(name, tag, RestResultRenderer()) {

    private lateinit var usedCfg: Configuration

    override fun verify(cmd: CommandCall, eval: Evaluator, resultRecorder: ResultRecorder) {
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

    private fun overrideJsonUnitOption(attr: String) {
        val first = usedCfg.options.values().first()
        val other = usedCfg.options.values();
        other.remove(first);
        other.addAll(attr.split(";").filter { it.isNotEmpty() }.map { Option.valueOf(it) }.toSet())
        usedCfg = usedCfg.withOptions(Options(first, *other.toTypedArray()))
    }

    private fun checkJsonContent(actual: String, expected: String, resultRecorder: ResultRecorder, root: Html) {
        try {
            JsonAssert.assertJsonEquals(expected, actual, usedCfg)
            resultRecorder.pass(root)
            root.text(expected)
        } catch (e: Throwable) {
            if (e is AssertionError || e is Exception) {
                resultRecorder.failure(root, actual.prettyJson(), expected.prettyJson())
                root.below(
                    span(e.message, CLASS to "exceptionMessage")
                )
            } else throw e
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