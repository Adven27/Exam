package io.github.adven27.concordion.extensions.exam.core.commands

import io.github.adven27.concordion.extensions.exam.core.ExamExtension
import io.github.adven27.concordion.extensions.exam.core.JsonContentTypeConfig
import io.github.adven27.concordion.extensions.exam.core.XmlContentTypeConfig
import io.github.adven27.concordion.extensions.exam.core.html.Html
import io.github.adven27.concordion.extensions.exam.core.html.html
import io.github.adven27.concordion.extensions.exam.core.readFile
import io.github.adven27.concordion.extensions.exam.core.resolveNoType
import io.github.adven27.concordion.extensions.exam.core.resolveToObj
import io.github.adven27.concordion.extensions.exam.core.vars
import io.restassured.RestAssured
import nu.xom.Element
import nu.xom.XPathContext
import org.awaitility.core.ConditionFactory
import org.concordion.api.CommandCall
import org.concordion.api.Evaluator
import org.concordion.api.Fixture
import org.concordion.api.ResultRecorder
import org.concordion.internal.command.SetCommand
import org.junit.Assert.assertEquals

open class SetVarCommand(
    override val name: String,
    override val tag: String
) : SetCommand(), NamedExamCommand, BeforeParseExamCommand {

    override fun setUp(cmd: CommandCall, eval: Evaluator, resultRecorder: ResultRecorder, fixture: Fixture) {
        val el = cmd.html()
        val valueAttr = el.attr("value")
        val valueFrom = el.attr("from")
        val vars = el.takeAwayAttr("vars").vars(eval, separator = el.takeAwayAttr("varsSeparator", ","))
        vars.forEach {
            val key = "#${it.key}"
            if (eval.getVariable(key) != null) {
                throw IllegalStateException("Variable $key already exists and will be shadowed in set command context. Use different variable name.")
            }
            eval.setVariable(key, it.value)
        }
        val value = when {
            valueAttr != null -> eval.resolveToObj(valueAttr)
            valueFrom != null -> eval.resolveNoType(valueFrom.readFile())
            else -> eval.resolveToObj(el.text().trimIndent())
        }

        eval.setVariable(varExp(varAttr(el) ?: cmd.expression), value)
        vars.forEach { eval.setVariable("#${it.key}", null) }
        cmd.swapText(value.toString())
    }

    private fun varAttr(el: Html) = el.attr("var") ?: el.el.getAttributeValue("set", ExamExtension.NS)
    private fun varExp(varName: String) = if (varName.startsWith("#")) varName else "#$varName"

    override fun beforeParse(elem: Element) {
        if (elem.localName == "set") super.beforeParse(elem)
    }
}

class WaitCommand(tag: String) : ExamCommand("await", tag) {
    override fun setUp(cmd: CommandCall, eval: Evaluator, resultRecorder: ResultRecorder, fixture: Fixture) {
        val el = cmd.html()
        val untilTrue = el.takeAwayAttr("untilTrue")
        val untilGet = eval.resolveToObj(el.takeAwayAttr("untilHttpGet", "")).toString()
        val untilPost = eval.resolveToObj(el.takeAwayAttr("untilHttpPost", "")).toString()
        val withBody = (el.takeAwayAttr("withBodyFrom")?.readFile() ?: el.text()).let {
            eval.resolveToObj(it).toString()
        }
        val withContentType = eval.resolveToObj(el.takeAwayAttr("withContentType", "application/json")).toString()
        val hasBody = el.takeAwayAttr("hasBody") ?: el.takeAwayAttr("hasBodyFrom")?.readFile()?.let {
            eval.resolveToObj(it).toString()
        }
        val hasStatus = el.takeAwayAttr("hasStatusCode")

        el.removeChildren()

        Thread.sleep(1000L * eval.resolveToObj(el.takeAwayAttr("seconds", "0")).toString().toInt())
        (cmd.html().awaitConfig("") ?: AwaitConfig()).await().let { await ->
            when {
                untilTrue != null -> await.alias(untilTrue).until { eval.evaluate(untilTrue) == true }
                untilGet.isNotEmpty() -> await.get(eval, untilGet, hasBody, hasStatus)
                untilPost.isNotEmpty() -> await.post(eval, withBody, withContentType, untilPost, hasBody, hasStatus)
            }
        }
    }

    @Suppress("LongParameterList")
    private fun ConditionFactory.post(
        eval: Evaluator,
        body: String,
        contentType: String,
        url: String,
        expectedBody: String?,
        expectedStatus: String?
    ) = untilAsserted {
        RestAssured.given().body(body).contentType(contentType).post(url)
            .apply { eval.setVariable("#exam_response", this) }
            .then().let {
                if (expectedStatus != null) it.statusCode(expectedStatus.toInt())
                if (expectedBody != null) assertEquals(expectedBody, it.extract().body().asString())
            }
    }

    private fun ConditionFactory.get(
        eval: Evaluator,
        url: String,
        expectedBody: String?,
        expectedStatus: String?
    ) = untilAsserted {
        RestAssured.get(url)
            .apply { eval.setVariable("#exam_response", this) }
            .then().let {
                if (expectedStatus != null) it.statusCode(expectedStatus.toInt())
                if (expectedBody != null) assertEquals(expectedBody, it.extract().body().asString())
            }
    }
}

class BeforeEachExampleCommand(tag: String) : ExamCommand("before-each", tag) {
    override fun beforeParse(elem: Element) {
        super.beforeParse(elem)
        examples(elem).apply { elem.detach() }.forEach { (it as Element).insertChild(elem.copy(), 0) }
    }

    private fun examples(elem: Element) =
        elem.document.rootElement.getFirstChildElement("body")
            .query(".//e:example", XPathContext("e", ExamExtension.NS))
}

class XmlEqualsCommand : ExamAssertEqualsCommand("xmlEquals", XmlContentTypeConfig())
class XmlEqualsFileCommand : ExamAssertEqualsCommand("xmlEqualsFile", XmlContentTypeConfig(), { it.readFile() })
class JsonEqualsCommand : ExamAssertEqualsCommand("jsonEquals", JsonContentTypeConfig())
class JsonEqualsFileCommand : ExamAssertEqualsCommand("jsonEqualsFile", JsonContentTypeConfig(), { it.readFile() })
class TextEqualsCommand : ExamAssertEqualsCommand("equals")
class TextEqualsFileCommand : ExamAssertEqualsCommand("equalsFile", content = { it.readFile() })
