package com.adven.concordion.extensions.exam.core.commands

import com.adven.concordion.extensions.exam.core.html.*
import com.adven.concordion.extensions.exam.core.resolveJson
import com.adven.concordion.extensions.exam.core.resolveXml
import com.adven.concordion.extensions.exam.ws.RestResultRenderer
import com.adven.concordion.extensions.exam.core.utils.content
import com.adven.concordion.extensions.exam.core.utils.equalToXml
import com.adven.concordion.extensions.exam.core.utils.prettyPrintJson
import com.adven.concordion.extensions.exam.core.utils.prettyPrintXml
import net.javacrumbs.jsonunit.JsonAssert
import net.javacrumbs.jsonunit.core.Configuration
import org.concordion.api.CommandCall
import org.concordion.api.Evaluator
import org.concordion.api.ResultRecorder
import org.xmlunit.diff.NodeMatcher

class XmlCheckCommand(name: String, tag: String, private val cfg: Configuration, private val nodeMatcher: NodeMatcher) :
        ExamVerifyCommand(name, tag, RestResultRenderer()) {

    override fun verify(cmd: CommandCall, eval: Evaluator, resultRecorder: ResultRecorder) {
        val root = cmd.html()
        val actual = eval.evaluate(root.takeAwayAttr("actual")).toString().prettyPrintXml()
        val expected = eval.resolveXml(root.content(eval).trim()).prettyPrintXml()
        val container = pre(expected).css("xml")
        root.removeAllChild()(
                tableSlim()(
                        trWithTDs(
                                container
                        )
                )
        )
        checkXmlContent(actual, expected, resultRecorder, container)
    }

    private fun checkXmlContent(actual: String, expected: String, resultRecorder: ResultRecorder, root: Html) {
        try {
            resultRecorder.check(root, actual, expected) { a, e ->
                a.equalToXml(e, nodeMatcher, cfg)
            }
        } catch (e: Exception) {
            resultRecorder.failure(root, actual, expected)
            root.below(
                    span(e.message, CLASS to "exceptionMessage")
            )
        }
    }
}

class JsonCheckCommand(name: String, tag: String, private val cfg: Configuration) :
        ExamVerifyCommand(name, tag, RestResultRenderer()) {

    override fun verify(cmd: CommandCall, eval: Evaluator, resultRecorder: ResultRecorder) {
        val root = cmd.html()
        val actual = eval.evaluate(root.attr("actual")).toString().prettyPrintJson()
        val expected = eval.resolveJson(root.content(eval).trim()).prettyPrintJson()
        val container = pre(expected).css("json")
        root.removeAllChild()(
                tableSlim()(
                        trWithTDs(
                                container
                        )
                )
        )
        checkJsonContent(actual, expected, resultRecorder, container)
    }

    private fun checkJsonContent(actual: String, expected: String, resultRecorder: ResultRecorder, root: Html) {
        try {
            JsonAssert.assertJsonEquals(expected, actual, cfg)
            resultRecorder.pass(root)
        } catch (e: Throwable) {
            if (e is AssertionError || e is Exception) {
                resultRecorder.failure(root, actual.prettyPrintJson(), expected.prettyPrintJson())
                root.below(
                        span(e.message, CLASS to "exceptionMessage")
                )
            } else throw e
        }
    }
}