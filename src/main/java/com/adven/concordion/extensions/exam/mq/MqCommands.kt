package com.adven.concordion.extensions.exam.mq

import com.adven.concordion.extensions.exam.core.commands.ExamCommand
import com.adven.concordion.extensions.exam.core.commands.ExamVerifyCommand
import com.adven.concordion.extensions.exam.core.html.*
import com.adven.concordion.extensions.exam.core.resolve
import com.adven.concordion.extensions.exam.core.resolveJson
import com.adven.concordion.extensions.exam.core.utils.content
import com.adven.concordion.extensions.exam.core.utils.prettyJson
import com.adven.concordion.extensions.exam.ws.RestResultRenderer
import net.javacrumbs.jsonunit.JsonAssert
import net.javacrumbs.jsonunit.core.Configuration
import net.javacrumbs.jsonunit.core.Option
import net.javacrumbs.jsonunit.core.internal.Options
import org.awaitility.Awaitility
import org.concordion.api.CommandCall
import org.concordion.api.Evaluator
import org.concordion.api.ResultRecorder
import org.junit.Assert
import java.util.concurrent.TimeUnit

interface MqTester {
    fun start()
    fun stop()
    fun send(message: String, headers: Map<String, String>)
    fun receive(): Message
    fun purge()

    data class Message @JvmOverloads constructor(val body: String = "", val headers: Map<String, String> = emptyMap())
}

open class MqTesterAdapter : MqTester {
    override fun start() = Unit
    override fun stop() = Unit
    override fun send(message: String, headers: Map<String, String>) = Unit
    override fun receive(): MqTester.Message = MqTester.Message()
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
        val mqName = root.takeAwayAttr("name")
        lateinit var actualBody: String
        val expectedBody = eval.resolveJson(root.content(eval).trim())
        val expectedHeaders = headers(root, eval)
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
                        val message = mqTesters.getOrFail(mqName).receive()
                        actualBody = message.body
                        Assert.assertEquals(message.headers, expectedHeaders)
                        JsonAssert.assertJsonEquals(expectedBody, actualBody, usedCfg)
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
            val message = mqTesters.getOrFail(mqName).receive()
            Assert.assertEquals(message.headers, expectedHeaders)
            checkJsonContent(message.body, expectedBody, resultRecorder, container)
        }
    }

    private fun checkJsonContent(actual: String, expected: String, resultRecorder: ResultRecorder, root: Html) {
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
    override fun execute(commandCall: CommandCall, eval: Evaluator, resultRecorder: ResultRecorder) {
        super.execute(commandCall, eval, resultRecorder)
        val root = commandCall.html()
        val mqName = root.takeAwayAttr("name")
        val headers = headers(root, eval)
        val message = eval.resolveJson(root.content(eval).trim())
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

private fun headers(root: Html, eval: Evaluator): Map<String, String> {
    return root.takeAwayAttr("headers")
        ?.split(',')
        ?.map { it.split('=')[0] to eval.resolve(it.split('=')[1]) }
        ?.toMap()
        ?: emptyMap()
}

private fun Map<String, MqTester>.getOrFail(mqName: String?): MqTester = this[mqName]
    ?: throw IllegalArgumentException("MQ with name $mqName not registered in MqPlugin")