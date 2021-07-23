package io.github.adven27.concordion.extensions.exam.core.commands

import io.github.adven27.concordion.extensions.exam.core.ExamExtension
import io.github.adven27.concordion.extensions.exam.core.html.Html
import io.github.adven27.concordion.extensions.exam.core.html.takeAttr
import io.github.adven27.concordion.extensions.exam.core.resolveForContentType
import io.github.adven27.concordion.extensions.exam.core.utils.content
import io.github.adven27.concordion.extensions.exam.core.vars
import nu.xom.Attribute
import org.awaitility.Awaitility
import org.awaitility.core.ConditionFactory
import org.concordion.api.AbstractCommand
import org.concordion.api.CommandCall
import org.concordion.api.Element
import org.concordion.api.Evaluator
import org.concordion.api.Fixture
import org.concordion.api.ResultRecorder
import org.concordion.api.listener.ExecuteEvent
import org.concordion.api.listener.ExecuteListener
import org.concordion.internal.util.Announcer
import java.util.concurrent.TimeUnit

open class ExamCommand(private val name: String, private val tag: String) : AbstractCommand() {
    private val listeners = Announcer.to(ExecuteListener::class.java)

    override fun execute(
        commandCall: CommandCall,
        evaluator: Evaluator,
        resultRecorder: ResultRecorder,
        fixture: Fixture
    ) {
        commandCall.children.processSequentially(evaluator, resultRecorder, fixture)
        announceExecuteCompleted(commandCall.element)
    }

    private fun announceExecuteCompleted(element: Element) =
        listeners.announce().executeCompleted(ExecuteEvent(element))

    fun tag(): String = tag

    fun name(): String = name

    open fun beforeParse(elem: nu.xom.Element) {
        val attr = Attribute(elem.localName, "")
        attr.setNamespace("e", ExamExtension.NS)
        elem.addAttribute(attr)

        elem.namespacePrefix = ""
        elem.namespaceURI = null
        elem.localName = tag
    }
}

fun CommandCall?.awaitConfig() = AwaitConfig(
    takeAttr("awaitAtMostSec", "0").toLong(),
    takeAttr("awaitPollDelayMillis", "0").toLong(),
    takeAttr("awaitPollIntervalMillis", "1000").toLong()
)

fun Html.awaitConfig() = AwaitConfig(
    takeAwayAttr("awaitAtMostSec", "0").toLong(),
    takeAwayAttr("awaitPollDelayMillis", "0").toLong(),
    takeAwayAttr("awaitPollIntervalMillis", "1000").toLong()
)

data class AwaitConfig(val atMostSec: Long, val pollDelay: Long, val pollInterval: Long) {
    fun enabled(): Boolean = atMostSec > 0
}

fun AwaitConfig.await(desc: String): ConditionFactory = Awaitility.await(desc)
    .atMost(atMostSec, TimeUnit.SECONDS)
    .pollDelay(pollDelay, TimeUnit.MILLISECONDS)
    .pollInterval(pollInterval, TimeUnit.MILLISECONDS)

fun AwaitConfig.timeoutMessage(e: Throwable) =
    "Check with poll delay $pollDelay ms and poll interval $pollInterval ms didn't complete within $atMostSec seconds because ${e.cause?.message}"

class VarsAttrs(root: Html, evaluator: Evaluator) {
    val vars: String? = root.takeAwayAttr(VARS)
    val varsSeparator: String = root.takeAwayAttr(VARS_SEPARATOR, ",")

    init {
        setVarsToContext(evaluator)
    }

    private fun setVarsToContext(evaluator: Evaluator) {
        vars.vars(evaluator, true, varsSeparator)
    }

    companion object {
        private const val VARS = "vars"
        private const val VARS_SEPARATOR = "varsSeparator"
    }
}

class FromAttrs(root: Html, evaluator: Evaluator, type: String? = null) {
    val from: String? = root.attr("from")
    val contentType: String = type ?: from?.substringAfterLast(".", "json") ?: "json"
    val content: String = evaluator.resolveForContentType(
        VarsAttrs(root, evaluator).let { root.content(from, evaluator) },
        contentType
    )
}
