package com.adven.concordion.extensions.exam.mq

import com.adven.concordion.extensions.exam.core.commands.ExamCommand
import com.adven.concordion.extensions.exam.core.commands.ExamVerifyCommand
import com.adven.concordion.extensions.exam.core.html.*
import com.adven.concordion.extensions.exam.core.resolveJson
import com.adven.concordion.extensions.exam.core.utils.content
import com.adven.concordion.extensions.exam.core.utils.prettyJson
import com.adven.concordion.extensions.exam.ws.RestResultRenderer
import net.javacrumbs.jsonunit.JsonAssert
import net.javacrumbs.jsonunit.core.Configuration
import org.awaitility.Awaitility
import org.concordion.api.CommandCall
import org.concordion.api.Evaluator
import org.concordion.api.ResultRecorder
import org.junit.Assert
import java.util.concurrent.TimeUnit

interface MqTester {
    fun start()
    fun stop()
    @Deprecated("should use send with headers")
    fun send(message: String)
    fun send(message:String, headers: Map<String, Any>) = send(message)
    @Deprecated("should use receive with headers")
    fun receive(): String
    fun receiveWithHeaders(): Pair<Map<String, Any>, String> = emptyMap<String, Any>() to receive()
    fun purge()
}

open class MqTesterAdapter : MqTester {
    override fun start() = Unit
    override fun stop() = Unit
    override fun send(message: String) = Unit
    override fun receive(): String = ""
    override fun purge() = Unit
}

class MqCheckCommand(
    name: String,
    tag: String,
    private val cfg: Configuration,
    private val mqTesters: Map<String, MqTester>
) :
    ExamVerifyCommand(name, tag, RestResultRenderer()) {

    override fun verify(cmd: CommandCall, eval: Evaluator, resultRecorder: ResultRecorder) {
        val root = cmd.html()
        val mqName = root.takeAwayAttr("name")
        lateinit var actualBody: String
        val expectedBody = eval.resolveJson(root.content(eval).trim())
        val expectedHeaders = headers(root)
        val container = pre(expectedBody).css("json").attr("autoFormat", "true")
        val atMostSec = root.takeAwayAttr("awaitAtMostSec")
        val pollDelay = root.takeAwayAttr("awaitPollDelayMillis")
        val pollInterval = root.takeAwayAttr("awaitPollIntervalMillis")
        root.removeChildren()(
            tableSlim()(
                caption(mqName)(italic("", CLASS to "fa fa-envelope-open fa-pull-left fa-border")),
                    caption("Headers: ${expectedHeaders.entries.joinToString()}")(italic("", CLASS to "fa fa-border")),
                    trWithTDs(
                    container
                )
            )
        )
        if (atMostSec != null || pollDelay != null || pollInterval != null) {
            val atMost = atMostSec?.toLong() ?: 4
            val delay = pollDelay?.toLong() ?: 0
            val interval = pollInterval?.toLong() ?: 1000
            try {
                Awaitility.await("Await MQ $mqName")
                    .atMost(atMost, TimeUnit.SECONDS)
                    .pollDelay(delay, TimeUnit.MILLISECONDS)
                    .pollInterval(interval, TimeUnit.MILLISECONDS)
                    .untilAsserted {
                        val (actualHeaders, receivedBody) = mqTesters.getOrFail(mqName).receiveWithHeaders()
                        actualBody = receivedBody
                        Assert.assertEquals(actualHeaders, expectedHeaders)
                        JsonAssert.assertJsonEquals(expectedBody, actualBody, cfg)
                    }
                resultRecorder.pass(container)
            } catch (e: Exception) {
                resultRecorder.failure(container, actualBody.prettyJson(), expectedBody.prettyJson())
                container.below(
                    pre("MQ check with poll delay $delay ms " +
                        "and poll interval $interval ms " +
                        "didn't complete within $atMost seconds because ${e.cause?.message}")
                        .css("alert alert-danger small")
                )
            }
        } else {
            val (actualHeaders, receivedBody) = mqTesters.getOrFail(mqName).receiveWithHeaders()
            Assert.assertEquals(actualHeaders, expectedHeaders)
            checkJsonContent(receivedBody, expectedBody, resultRecorder, container)
        }
    }

    private fun checkJsonContent(actual: String, expected: String, resultRecorder: ResultRecorder, root: Html) {
        try {
            JsonAssert.assertJsonEquals(expected, actual, cfg)
            resultRecorder.pass(root)
        } catch (e: Throwable) {
            if (e is AssertionError || e is Exception) {
                resultRecorder.failure(root, actual.prettyJson(), expected.prettyJson())
                root.below(
                    pre(e.message).css("alert alert-danger small")
                )
            } else throw e
        }
    }
}

class MqSendCommand(name: String, tag: String, private val mqTesters: Map<String, MqTester>) : ExamCommand(name, tag) {
    override fun execute(commandCall: CommandCall, evaluator: Evaluator, resultRecorder: ResultRecorder) {
        super.execute(commandCall, evaluator, resultRecorder)
        val root = commandCall.html()
        val mqName = root.takeAwayAttr("name")
        val headers = headers(root)
        val message = evaluator.resolveJson(root.content(evaluator).trim())
        root.removeChildren()(
            tableSlim()(
                caption(mqName)(italic("", CLASS to "fa fa-envelope fa-pull-left fa-border")),
                caption("Headers: ${headers.entries.joinToString()}")(italic("", CLASS to "fa fa-border")),
                trWithTDs(
                    pre(message).css("json")
                )
            )
        )
        mqTesters.getOrFail(mqName).send(message, headers)
    }

}

private fun headers(root: Html): Map<String, Any> {
    return root.takeAwayAttr("headers")
            ?.split(',')
            ?.map { it.split('=')[0] to it.split('=')[1] }
            ?.toMap()
            ?: emptyMap()
}

private fun Map<String, MqTester>.getOrFail(mqName: String?): MqTester = this[mqName]
    ?: throw IllegalArgumentException("MQ with name $mqName not registered in MqPlugin")