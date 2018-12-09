package com.adven.concordion.extensions.exam.mq

import com.adven.concordion.extensions.exam.commands.ExamCommand
import com.adven.concordion.extensions.exam.commands.ExamVerifyCommand
import com.adven.concordion.extensions.exam.html.*
import com.adven.concordion.extensions.exam.resolveJson
import com.adven.concordion.extensions.exam.rest.RestResultRenderer
import com.adven.concordion.extensions.exam.utils.content
import com.adven.concordion.extensions.exam.utils.prettyPrintJson
import net.javacrumbs.jsonunit.JsonAssert
import net.javacrumbs.jsonunit.core.Configuration
import org.concordion.api.CommandCall
import org.concordion.api.Evaluator
import org.concordion.api.ResultRecorder
import java.util.*

interface MqTester {
    class DummyTester : MqTester {
        private val queue = Stack<String>()
        override fun start() {}
        override fun stop() {}
        override fun purge() = queue.clear()
        override fun receive(): String = queue.pop()
        override fun send(message: String) {
            queue.add(message)
        }
    }

    fun start()
    fun stop()
    fun send(message: String)
    fun receive(): String
    fun purge()
}

class MqCheckCommand(name: String, tag: String, private val cfg: Configuration, private val mqTesters: Map<String, MqTester>) :
        ExamVerifyCommand(name, tag, RestResultRenderer()) {

    override fun verify(cmd: CommandCall, eval: Evaluator, resultRecorder: ResultRecorder) {
        val root = cmd.html()
        val mqName = root.takeAwayAttr("name")
        val mqTester = mqTesters[mqName]
                ?: throw IllegalArgumentException("MQ with name $mqName not registered in Exam")
        val actual = mqTester.receive()
        val expected = eval.resolveJson(root.content().trim())
        val container = pre(expected).css("json").attr("autoFormat", "true")
        root.removeAllChild()(
                tableSlim()(
                        caption(mqName)(italic("", CLASS to "fa fa-envelope-open fa-pull-left fa-border")),
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

class MqSendCommand(name: String, tag: String, private val mqTesters: Map<String, MqTester>) : ExamCommand(name, tag) {
    override fun execute(cmd: CommandCall, eval: Evaluator, resultRecorder: ResultRecorder) {
        super.execute(cmd, eval, resultRecorder)
        val root = cmd.html()
        val mqName = root.takeAwayAttr("name")
        val message = eval.resolveJson(root.content().trim())
        root.removeAllChild()(
                tableSlim()(
                        caption(mqName)(italic("", CLASS to "fa fa-envelope fa-pull-left fa-border")),
                        trWithTDs(
                                pre(message).css("json")
                        )
                )
        )
        val mqTester = mqTesters[mqName]
                ?: throw IllegalArgumentException("MQ with name $mqName not registered in Exam")
        mqTester.send(message)
    }
}