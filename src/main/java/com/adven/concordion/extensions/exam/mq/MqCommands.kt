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
import java.util.concurrent.TimeUnit

interface MqTester {
    fun start()
    fun stop()
    fun send(message: String)
    fun receive(): String
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
        lateinit var actual: String
        val expected = eval.resolveJson(root.content(eval).trim())
        val container = pre(expected).css("json").attr("autoFormat", "true")
        val atMostSec = root.takeAwayAttr("awaitAtMostSec")
        val pollDelay = root.takeAwayAttr("awaitPollDelayMillis")
        val pollInterval = root.takeAwayAttr("awaitPollIntervalMillis")
        root.removeChildren()(
            tableSlim()(
                caption(mqName)(italic("", CLASS to "fa fa-envelope-open fa-pull-left fa-border")),
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
                        actual = mqTesters.getOrFail(mqName).receive()
                        JsonAssert.assertJsonEquals(expected, actual, cfg)
                    }
                resultRecorder.pass(container)
            } catch (e: Exception) {
                resultRecorder.failure(container, actual.prettyJson(), expected.prettyJson())
                container.below(
                    pre("DB check with poll delay $delay ms " +
                        "and poll interval $interval ms " +
                        "didn't complete within $atMost seconds because ${e.cause?.message}")
                        .css("alert alert-danger small")
                )
            }
        } else {
            checkJsonContent(mqTesters.getOrFail(mqName).receive(), expected, resultRecorder, container)
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
        val message = evaluator.resolveJson(root.content(evaluator).trim())
        root.removeChildren()(
            tableSlim()(
                caption(mqName)(italic("", CLASS to "fa fa-envelope fa-pull-left fa-border")),
                trWithTDs(
                    pre(message).css("json")
                )
            )
        )
        mqTesters.getOrFail(mqName).send(message)
    }
}

private fun Map<String, MqTester>.getOrFail(mqName: String?): MqTester = this[mqName]
    ?: throw IllegalArgumentException("MQ with name $mqName not registered in MqPlugin")