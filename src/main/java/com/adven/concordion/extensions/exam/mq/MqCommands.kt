package com.adven.concordion.extensions.exam.mq

import com.adven.concordion.extensions.exam.core.commands.ExamCommand
import com.adven.concordion.extensions.exam.core.commands.ExamVerifyCommand
import com.adven.concordion.extensions.exam.core.commands.await
import com.adven.concordion.extensions.exam.core.commands.awaitConfig
import com.adven.concordion.extensions.exam.core.commands.timeoutMessage
import com.adven.concordion.extensions.exam.core.html.CLASS
import com.adven.concordion.extensions.exam.core.html.Html
import com.adven.concordion.extensions.exam.core.html.caption
import com.adven.concordion.extensions.exam.core.html.div
import com.adven.concordion.extensions.exam.core.html.divCollapse
import com.adven.concordion.extensions.exam.core.html.html
import com.adven.concordion.extensions.exam.core.html.italic
import com.adven.concordion.extensions.exam.core.html.pre
import com.adven.concordion.extensions.exam.core.html.span
import com.adven.concordion.extensions.exam.core.html.tableSlim
import com.adven.concordion.extensions.exam.core.html.tbody
import com.adven.concordion.extensions.exam.core.html.td
import com.adven.concordion.extensions.exam.core.html.tr
import com.adven.concordion.extensions.exam.core.html.trWithTDs
import com.adven.concordion.extensions.exam.core.resolveJson
import com.adven.concordion.extensions.exam.core.utils.content
import com.adven.concordion.extensions.exam.core.utils.prettyJson
import com.adven.concordion.extensions.exam.core.vars
import com.adven.concordion.extensions.exam.ws.RestResultRenderer
import net.javacrumbs.jsonunit.JsonAssert
import net.javacrumbs.jsonunit.core.Configuration
import net.javacrumbs.jsonunit.core.Option
import net.javacrumbs.jsonunit.core.internal.Options
import org.concordion.api.CommandCall
import org.concordion.api.Evaluator
import org.concordion.api.Result.FAILURE
import org.concordion.api.ResultRecorder
import org.junit.Assert
import java.util.UUID

interface MqTester {
    fun start()
    fun stop()
    fun send(message: String, headers: Map<String, String>)
    fun receive(): List<Message>
    fun purge()

    data class Message @JvmOverloads constructor(val body: String = "", val headers: Map<String, String> = emptyMap())
}

open class MqTesterAdapter : MqTester {
    override fun start() = Unit
    override fun stop() = Unit
    override fun send(message: String, headers: Map<String, String>) = Unit
    override fun receive(): List<MqTester.Message> = listOf()
    override fun purge() = Unit
}

class MqCheckCommand(
    name: String,
    tag: String,
    private val originalCfg: Configuration,
    private val mqTesters: Map<String, MqTester>
) : ExamVerifyCommand(name, tag, RestResultRenderer()) {

    private lateinit var usedCfg: Configuration

    override fun verify(cmd: CommandCall, eval: Evaluator, resultRecorder: ResultRecorder) {
        val root = cmd.html()
        usedCfg = originalCfg;
        root.attr("jsonUnitOptions")?.let { attr -> overrideJsonUnitOption(attr) }
        val mqName = root.takeAwayAttr("name")!!
        val layout = root.takeAwayAttr("layout", "VERTICALLY")
        val contains = root.takeAwayAttr("contains", "EXACT")
        val collapsable = root.takeAwayAttr("collapsable", "false").toBoolean()
        val awaitConfig = cmd.awaitConfig()

        val messageTags = root.childs().filter { it.localName() == "message" }.ifEmpty { listOf(root) }
        val expectedMessages = messageTags.map { html ->
            html.takeAwayAttr("vars").vars(eval, true, html.takeAwayAttr("varsSeparator", ","))
            val content = html.content(eval)
            if (content.isEmpty()) return@map null else MqTester.Message(
                eval.resolveJson(content.trim()),
                headers(html, eval)
            )
        }.filterNotNull()
        val actualMessages: MutableList<MqTester.Message> = mqTesters.getOrFail(mqName).receive().toMutableList()

        try {
            if (awaitConfig.enabled()) {
                try {
                    awaitConfig.await("Await MQ $mqName").untilAsserted {
                        actualMessages.addAll(mqTesters.getOrFail(mqName).receive())
                        Assert.assertEquals(expectedMessages.size, actualMessages.size)
                    }
                } catch (e: Exception) {
                    resultRecorder.record(FAILURE)
                    root.removeChildren().below(
                        div().css("rest-failure bd-callout bd-callout-danger")(
                            div(e.cause?.message),
                            *renderMessages("Expected: ", expectedMessages, mqName).toTypedArray(),
                            *renderMessages("but was: ", actualMessages, mqName).toTypedArray()
                        )
                    )
                    root.below(pre(awaitConfig.timeoutMessage(e)).css("alert alert-danger small"))
                }
            } else {
                Assert.assertEquals(expectedMessages.size, actualMessages.size)
            }
        } catch (e: java.lang.AssertionError) {
            resultRecorder.record(FAILURE)
            root.below(
                div().css("rest-failure bd-callout bd-callout-danger")(
                    div(e.message),
                    *renderMessages("Expected: ", expectedMessages, mqName).toTypedArray(),
                    *renderMessages("but was: ", actualMessages, mqName).toTypedArray()
                )
            )
            root.parent().remove(root)
            return
        }
        val tableContainer = tableSlim()(captionEnvelopOpen(mqName))
        root.removeChildren()(tableContainer)

        var cnt: Html? = null
        if (layout.toUpperCase() != "VERTICALLY") {
            cnt = tr()
            tableContainer(cnt)
        }

        prepared(expectedMessages, contains).zip(prepared(actualMessages, contains)).forEach {
            val bodyContainer = jsonEl("", collapsable)
            val headersContainer =
                span("Headers: ${it.first.headers.entries.joinToString()}")(italic("", CLASS to "fa fa-border"))
            if (cnt != null) {
                cnt(
                    td()(
                        tableSlim()(
                            if (it.first.headers.isNotEmpty()) trWithTDs(headersContainer) else null,
                            tr()(if (collapsable) collapsed(bodyContainer) else bodyContainer)
                        )
                    )
                )
            } else {
                tableContainer(
                    if (it.first.headers.isNotEmpty()) trWithTDs(headersContainer) else null,
                    tr()(if (collapsable) collapsed(bodyContainer) else bodyContainer)
                )
            }
            checkHeaders(it.second.headers, it.first.headers, resultRecorder, headersContainer)
            checkJsonContent(it.second.body, it.first.body, resultRecorder, bodyContainer)
        }
    }

    private fun prepared(origin: List<MqTester.Message>, contains: String) =
        if (needSort(contains)) origin.sortedBy { it.body } else origin

    private fun needSort(contains: String) = "EXACT" != contains

    private fun renderMessages(msg: String, messages: List<MqTester.Message>, mqName: String): List<Html> {
        return listOf(span(msg), tableSlim()(
            captionEnvelopOpen(mqName),
            tbody()(
                messages.map { tr()(jsonEl(it.body)) }
            )
        ))
    }

    private fun jsonEl(txt: String, collapsable: Boolean = false) =
        container(txt, "json", collapsable).style("margin: 0").attr("autoFormat", "true")

    private fun checkHeaders(
        actual: Map<String, String>, expected: Map<String, String>, resultRecorder: ResultRecorder, root: Html
    ) {
        try {
            Assert.assertEquals(expected, actual)
            resultRecorder.pass(root)
        } catch (e: Throwable) {
            if (e is AssertionError || e is Exception) {
                resultRecorder.failure(root, actual.toString(), expected.toString())
                root.below(
                    pre(e.message).css("alert alert-danger small")
                )
            } else throw e
        }
    }

    private fun checkJsonContent(actual: String, expected: String, resultRecorder: ResultRecorder, root: Html) =
        try {
            JsonAssert.assertJsonEquals(expected, actual, usedCfg)
            root.text(expected.prettyJson())
            resultRecorder.pass(root)
        } catch (e: Throwable) {
            if (e is AssertionError || e is Exception) {
                resultRecorder.failure(root, actual.prettyJson(), expected.prettyJson())
                root.parent().above(
                    trWithTDs(pre(e.message).css("alert alert-danger small"))
                )
            } else throw e
        }

    private fun overrideJsonUnitOption(attr: String) {
        val first = usedCfg.options.values().first()
        val other = usedCfg.options.values();
        other.remove(first);
        other.addAll(attr.split(";").filter { it.isNotEmpty() }.map { Option.valueOf(it) }.toSet())
        usedCfg = usedCfg.withOptions(Options(first, *other.toTypedArray()))
    }
}

class MqSendCommand(name: String, tag: String, private val mqTesters: Map<String, MqTester>) : ExamCommand(name, tag) {
    override fun execute(commandCall: CommandCall, evaluator: Evaluator, resultRecorder: ResultRecorder) {
        super.execute(commandCall, evaluator, resultRecorder)
        val root = commandCall.html()
        val mqName = root.takeAwayAttr("name")
        val collapsable = root.takeAwayAttr("collapsable", "false").toBoolean()
        val headers = headers(root, evaluator)
        root.takeAwayAttr("vars").vars(evaluator, true, root.takeAwayAttr("varsSeparator", ","))
        val message = evaluator.resolveJson(root.content(evaluator).trim())
        root.removeChildren()(
            tableSlim()(
                captionEnvelopClosed(mqName),
                if (headers.isNotEmpty()) caption("Headers: ${headers.entries.joinToString()}")(
                    italic("", CLASS to "fa fa-border")
                ) else null,
                tr()(
                    if (collapsable) collapsed(collapsableContainer(message, "json"))
                    else td(message).css("json")//exp-body
                )
            )
        )
        mqTesters.getOrFail(mqName).send(message, headers)
    }
}

class MqPurgeCommand(name: String, tag: String, private val mqTesters: Map<String, MqTester>) : ExamCommand(name, tag) {
    override fun execute(commandCall: CommandCall, evaluator: Evaluator, resultRecorder: ResultRecorder) {
        super.execute(commandCall, evaluator, resultRecorder)
        val root = commandCall.html()
        val mqName = root.takeAwayAttr("name")
        mqTesters.getOrFail(mqName).purge()
        root.removeChildren()(
            tableSlim()(
                caption()(italic(" $mqName purged", CLASS to "fa fa-envelope fa-pull-left fa-border"))
            )
        )
    }
}

private fun headers(root: Html, eval: Evaluator): Map<String, String> =
    root.takeAwayAttr("headers")?.vars(eval)?.mapValues { it.value.toString() } ?: emptyMap()

private fun Map<String, MqTester>.getOrFail(mqName: String?): MqTester = this[mqName]
    ?: throw IllegalArgumentException("MQ with name $mqName not registered in MqPlugin")

private fun captionEnvelopOpen(mqName: String) =
    caption()(italic(" $mqName", CLASS to "fa fa-envelope-open fa-pull-left fa-border"))

private fun captionEnvelopClosed(mqName: String?) =
    caption()(italic(" $mqName", CLASS to "fa fa-envelope fa-pull-left fa-border"))

private fun container(text: String, type: String, collapsable: Boolean): Html {
    return if (collapsable) collapsableContainer(text, type) else fixedContainer(text, type)
}

private fun collapsed(container: Html): Html {
    return td("class" to "exp-body")(
        div().style("position: relative")(
            divCollapse("", container.attr("id").toString()).css("fa fa-expand collapsed"),
            container
        )
    )
}

private fun fixedContainer(text: String, type: String): Html {
    return td(text).css("$type exp-body")
}

private fun collapsableContainer(text: String, type: String): Html {
    val id = UUID.randomUUID().toString()
    return div(text, "id" to id).css("$type file collapse")
}

