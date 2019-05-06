package com.adven.concordion.extensions.exam.commands

import com.adven.concordion.extensions.exam.html.*
import com.adven.concordion.extensions.exam.resolveJson
import com.adven.concordion.extensions.exam.rest.RestResultRenderer
import com.adven.concordion.extensions.exam.utils.content
import com.adven.concordion.extensions.exam.utils.equalToXml
import com.adven.concordion.extensions.exam.utils.prettyPrintJson
import com.adven.concordion.extensions.exam.utils.prettyPrintXml
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
        val actual = eval.evaluate(root.takeAwayAttr("actual")).toString()
        val expected = eval.resolveJson( root.content(eval).trim())
        val container = pre(expected).css("json").attr("autoFormat", "true")
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
        val prettyActual = actual.prettyPrintXml()
        try {
            resultRecorder.check(root, prettyActual, expected) { a, e ->
                a.equalToXml(e, nodeMatcher, cfg)
            }
        } catch (e: Exception) {
            resultRecorder.failure(root, prettyActual, expected)
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
        val actual = eval.evaluate(root.attr("actual")).toString()
        val expected = eval.resolveJson(root.content(eval).trim())
        val container = pre(expected).css("json").attr("autoFormat", "true")
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