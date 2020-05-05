package com.adven.concordion.extensions.exam.mq

import com.adven.concordion.extensions.exam.core.commands.ExamCommand
import com.adven.concordion.extensions.exam.core.commands.ExamVerifyCommand
import com.adven.concordion.extensions.exam.core.html.*
import com.adven.concordion.extensions.exam.core.resolveJson
import com.adven.concordion.extensions.exam.core.utils.content
import com.adven.concordion.extensions.exam.core.utils.prettyJson
import com.adven.concordion.extensions.exam.core.vars
import com.adven.concordion.extensions.exam.ws.RestResultRenderer
import net.javacrumbs.jsonunit.JsonAssert
import net.javacrumbs.jsonunit.core.Configuration
import net.javacrumbs.jsonunit.core.Option
import net.javacrumbs.jsonunit.core.internal.Options
import org.awaitility.Awaitility
import org.concordion.api.CommandCall
import org.concordion.api.Evaluator
import org.concordion.api.Result.FAILURE
import org.concordion.api.ResultRecorder
import org.junit.Assert
import java.util.concurrent.TimeUnit

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
) :
    ExamVerifyCommand(name, tag, RestResultRenderer()) {

    private lateinit var usedCfg: Configuration

    override fun verify(cmd: CommandCall, eval: Evaluator, resultRecorder: ResultRecorder) {
        val root = cmd.html()
        usedCfg = originalCfg;
        root.attr("jsonUnitOptions")?.let { attr -> overrideJsonUnitOption(attr) }
        val mqName = root.takeAwayAttr("name")!!
        val layout = root.takeAwayAttr("layout", "VERTICALLY")

        val atMostSec = root.takeAwayAttr("awaitAtMostSec")
        val pollDelay = root.takeAwayAttr("awaitPollDelayMillis")
        val pollInterval = root.takeAwayAttr("awaitPollIntervalMillis")

        val messageTags = root.childs().filter { it.localName() == "message" }.ifEmpty { listOf(root) }
        val expectedMessages = messageTags.map { html ->
            html.takeAwayAttr("vars").vars(eval).forEach { eval.setVariable("#${it.key}", it.value) }
            MqTester.Message(eval.resolveJson(html.content(eval).trim()), headers(html, eval))
        }
        val actualMessages: MutableList<MqTester.Message> = mqTesters.getOrFail(mqName).receive().toMutableList()

        try {
            if (isPollingEnabled(atMostSec, pollDelay, pollInterval)) {
                val atMost = atMostSec?.toLong() ?: 4
                val delay = pollDelay?.toLong() ?: 0
                val interval = pollInterval?.toLong() ?: 1000
                try {
                    Awaitility.await("Await MQ $mqName")
                        .atMost(atMost, TimeUnit.SECONDS)
                        .pollDelay(delay, TimeUnit.MILLISECONDS)
                        .pollInterval(interval, TimeUnit.MILLISECONDS)
                        .untilAsserted {
                            actualMessages.addAll(mqTesters.getOrFail(mqName).receive())
                            Assert.assertEquals(expectedMessages.size, actualMessages.size)
                        }
                } catch (e: Exception) {
                    resultRecorder.record(FAILURE)
                    root.removeChildren().below(div().css("rest-failure bd-callout bd-callout-danger")(
                        div(e.cause?.message),
                        *renderMessages("Expected: ", expectedMessages, mqName).toTypedArray(),
                        *renderMessages("but was: ", actualMessages, mqName).toTypedArray()
                    ))
                    root.below(
                        pre("MQ check with poll delay $delay ms " +
                            "and poll interval $interval ms " +
                            "didn't complete within $atMost seconds because ${e.cause?.message}")
                            .css("alert alert-danger small")
                    )
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
                ))
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

        expectedMessages.zip(actualMessages).forEach {
            val bodyContainer = jsonEl(it.first.body)
            val headersContainer = span("Headers: ${it.first.headers.entries.joinToString()}")(italic("", CLASS to "fa fa-border"))
            if (cnt != null) {
                cnt(
                    td()(tableSlim()(if (it.first.headers.isNotEmpty()) trWithTDs(headersContainer) else null, trWithTDs(bodyContainer)))
                )
            } else {
                tableContainer(
                    if (it.first.headers.isNotEmpty()) trWithTDs(headersContainer) else null, trWithTDs(bodyContainer)
                )
            }
            checkHeaders(it.second.headers, it.first.headers, resultRecorder, headersContainer)
            checkJsonContent(it.second.body, it.first.body, resultRecorder, bodyContainer)
        }
    }

    private fun isPollingEnabled(atMostSec: String?, pollDelay: String?, pollInterval: String?) =
        atMostSec != null || pollDelay != null || pollInterval != null

    private fun renderMessages(msg: String, messages: List<MqTester.Message>, mqName: String): List<Html> {
        return listOf(span(msg), tableSlim()(
            captionEnvelopOpen(mqName),
            tbody()(
                messages.map { trWithTDs(jsonEl(it.body)) }
            )
        ))
    }

    private fun jsonEl(txt: String) =
        pre(txt).css("json").style("margin: 0").attr("autoFormat", "true")

    private fun checkHeaders(actual: Map<String, String>, expected: Map<String, String>, resultRecorder: ResultRecorder, root: Html) {
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
            resultRecorder.pass(root)
        } catch (e: Throwable) {
            if (e is AssertionError || e is Exception) {
                resultRecorder.failure(root, actual.prettyJson(), expected.prettyJson())
                root.below(
                    pre(e.message).css("alert alert-danger small")
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
        val headers = headers(root, evaluator)
        val message = evaluator.resolveJson(root.content(evaluator).trim())
        root.removeChildren()(
            tableSlim()(
                captionEnvelopClosed(mqName),
                if (headers.isNotEmpty()) caption("Headers: ${headers.entries.joinToString()}")(italic("", CLASS to "fa fa-border")) else null,
                trWithTDs(
                    pre(message).css("json")
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
