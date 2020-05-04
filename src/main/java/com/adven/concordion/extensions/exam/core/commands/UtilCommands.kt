package com.adven.concordion.extensions.exam.core.commands

import com.adven.concordion.extensions.exam.core.*
import com.adven.concordion.extensions.exam.core.html.html
import com.adven.concordion.extensions.exam.core.utils.readFile
import io.restassured.RestAssured
import nu.xom.Attribute
import nu.xom.Element
import nu.xom.XPathContext
import org.awaitility.Awaitility
import org.awaitility.core.ConditionFactory
import org.concordion.api.CommandCall
import org.concordion.api.Evaluator
import org.concordion.api.ResultRecorder
import org.concordion.internal.ConcordionBuilder
import org.junit.Assert.assertEquals
import java.lang.Boolean
import java.util.concurrent.TimeUnit

class SetVarCommand(tag: String) : ExamCommand("set", tag) {
    override fun setUp(cmd: CommandCall, eval: Evaluator, resultRecorder: ResultRecorder) {
        val el = cmd.html()
        val valueAttr = el.attr("value")
        val valueFrom = el.attr("from")
        val vars = el.takeAwayAttr("vars").vars(eval)
        vars.forEach {
            val key = "#${it.key}"
            if (eval.getVariable(key) != null)
                throw IllegalStateException("Variable $key already exists and will be shadowed in set command context. Use different variable name.")
            eval.setVariable(key, it.value)
        }
        eval.setVariable(
            "#${el.attr("var")!!}",
            when {
                valueAttr != null -> eval.resolveToObj(valueAttr)
                valueFrom != null -> eval.resolveXml(valueFrom.readFile())
                else -> {
                    val body = el.text()
                    val silent = el.attr("silent")
                    if (silent != null && silent == "true") {
                        el.removeChildren()
                    }
                    eval.resolveXml(body)
                }
            }
        )
        vars.forEach { eval.setVariable("#${it.key}", null) }
    }
}

class WaitCommand(tag: String) : ExamCommand("await", tag) {
    override fun setUp(cmd: CommandCall, eval: Evaluator, resultRecorder: ResultRecorder) {
        val el = cmd.html()
        val untilTrue = el.takeAwayAttr("untilTrue")
        val untilGet = eval.resolve(el.takeAwayAttr("untilHttpGet", ""))
        val untilPost = eval.resolve(el.takeAwayAttr("untilHttpPost", ""))
        val withBodyFrom = eval.resolve(el.takeAwayAttr("withBodyFrom", ""))
        val withContentType = eval.resolve(el.takeAwayAttr("withContentType", "application/json"))
        val hasBody = el.takeAwayAttr("hasBody")
        val hasBodyFrom = el.takeAwayAttr("hasBodyFrom")
        val expectedStatus = el.takeAwayAttr("hasStatusCode")

        val body = if (withBodyFrom.isEmpty()) el.text() else eval.resolve(withBodyFrom.readFile())
        el.removeChildren()

        val await = Awaitility.await()
            .atMost(el.takeAwayAttr("atMostSec", "4").toLong(), TimeUnit.SECONDS)
            .pollDelay(el.takeAwayAttr("pollDelayMillis", "0").toLong(), TimeUnit.MILLISECONDS)
            .pollInterval(el.takeAwayAttr("pollIntervalMillis", "1000").toLong(), TimeUnit.MILLISECONDS)

        Thread.sleep(1000L * eval.resolve(el.takeAwayAttr("seconds", "0")).toInt())
        when {
            untilTrue != null -> await.alias(untilTrue).until { Boolean.TRUE == eval.evaluate(untilTrue) }
            untilGet.isNotEmpty() && hasAny(hasBody, hasBodyFrom, expectedStatus) -> {
                when {
                    hasBody != null ->
                        await.awaitGet(eval, untilGet, eval.resolve(hasBody), expectedStatus)
                    hasBodyFrom != null ->
                        await.awaitGet(eval, untilGet, eval.resolve(hasBodyFrom.readFile()), expectedStatus)
                    expectedStatus != null -> await.untilAsserted {
                        RestAssured.get(untilGet)
                            .apply { eval.setVariable("#exam_response", this) }
                            .then().statusCode(expectedStatus.toInt())
                    }
                }
            }
            untilPost.isNotEmpty() && hasAny(hasBody, hasBodyFrom, expectedStatus) -> {
                when {
                    hasBody != null ->
                        await.awaitPost(eval, body, withContentType, untilPost, eval.resolve(hasBody), expectedStatus)
                    hasBodyFrom != null ->
                        await.awaitPost(eval, body, withContentType, untilPost, eval.resolve(hasBodyFrom.readFile()), expectedStatus)
                    expectedStatus != null -> await.untilAsserted {
                        RestAssured.given().body(eval.resolve(body)).contentType(withContentType).post(untilPost)
                            .apply { eval.setVariable("#exam_response", this) }
                            .then().statusCode(expectedStatus.toInt())
                    }
                }
            }
        }
    }

    private fun hasAny(hasBody: String?, hasBodyFrom: String?, expectedStatus: String?) =
        hasBody != null || hasBodyFrom != null || expectedStatus != null

    private fun ConditionFactory.awaitPost(
        eval: Evaluator, body: String, contentType: String, url: String, expectedBody: String, expectedStatus: String?
    ) = untilAsserted {
        val response = RestAssured.given().body(eval.resolve(body)).contentType(contentType).post(url)
            .apply { eval.setVariable("#exam_response", this) }
            .then()
        if (expectedStatus != null) response.statusCode(expectedStatus.toInt())
        assertEquals(expectedBody, response.extract().body().asString())
    }

    private fun ConditionFactory.awaitGet(
        eval: Evaluator, url: String, expectedBody: String, expectedStatus: String?
    ) = untilAsserted {
        val response = RestAssured.get(url)
            .apply { eval.setVariable("#exam_response", this) }
            .then()
        if (expectedStatus != null) response.statusCode(expectedStatus.toInt())
        assertEquals(expectedBody, response.extract().body().asString())
    }
}

class BeforeEachExampleCommand(tag: String) : ExamCommand("before-each", tag) {
    override fun beforeParse(elem: Element) {
        super.beforeParse(elem)
        val ns = XPathContext("e", ExamExtension.NS)
        val examples = elem.document.rootElement.getFirstChildElement("body").query(".//e:example", ns)
        elem.detach()
        for (i in 0 until examples.size()) {
            val example = examples.get(i) as Element
            if (example.childElements.size() > 0) {
                example.insertChild(elem.copy(), 0)
            }
        }
    }
}

class ExamBeforeExampleCommand(tag: String) : ExamCommand("before", tag) {
    override fun beforeParse(elem: Element) {
        transformToConcordionExample(elem)
        super.beforeParse(elem)
    }

    private fun transformToConcordionExample(elem: Element) {
        val attr = Attribute("example", "before")
        attr.setNamespace("c", ConcordionBuilder.NAMESPACE_CONCORDION_2007)
        elem.addAttribute(attr)
    }
}