package io.github.adven27.concordion.extensions.exam.core.commands

import io.github.adven27.concordion.extensions.exam.core.ContentTypeConfig
import io.github.adven27.concordion.extensions.exam.core.ExamExtension
import io.github.adven27.concordion.extensions.exam.core.ExamResultRenderer
import io.github.adven27.concordion.extensions.exam.core.TextContentTypeConfig
import io.github.adven27.concordion.extensions.exam.core.content
import io.github.adven27.concordion.extensions.exam.core.html.Html
import io.github.adven27.concordion.extensions.exam.core.html.takeAttr
import io.github.adven27.concordion.extensions.exam.core.resolveForContentType
import io.github.adven27.concordion.extensions.exam.core.vars
import mu.KLogging
import nu.xom.Attribute
import org.awaitility.Awaitility
import org.awaitility.core.ConditionFactory
import org.concordion.api.AbstractCommand
import org.concordion.api.Command
import org.concordion.api.CommandCall
import org.concordion.api.Element
import org.concordion.api.Evaluator
import org.concordion.api.Fixture
import org.concordion.api.ResultRecorder
import org.concordion.api.listener.ExecuteEvent
import org.concordion.api.listener.ExecuteListener
import org.concordion.internal.CatchAllExpectationChecker.normalize
import org.concordion.internal.command.AssertEqualsCommand
import org.concordion.internal.util.Announcer
import java.util.concurrent.TimeUnit

interface NamedExamCommand : Command {
    val name: String
}

interface BeforeParseExamCommand {
    val tag: String

    fun beforeParse(elem: nu.xom.Element) {
        val attr = Attribute(elem.localName, "")
        attr.setNamespace("e", ExamExtension.NS)
        elem.addAttribute(attr)

        elem.namespacePrefix = ""
        elem.namespaceURI = null
        elem.localName = tag
    }
}

open class BaseExamCommand(override val name: String) : NamedExamCommand, AbstractCommand()

open class ExamCommand(override val name: String, override val tag: String) : BeforeParseExamCommand,
    BaseExamCommand(name) {
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
}

open class ExamAssertEqualsCommand(
    override val name: String,
    val config: ContentTypeConfig = TextContentTypeConfig(),
    val content: (text: String) -> String = { it }
) : NamedExamCommand, AssertEqualsCommand(
    Comparator { actual, expected ->
        config.verifier.verify(normalize(expected), normalize(actual)).fail.map { -1 }.orElse(0)
    }
) {
    init {
        addAssertEqualsListener(ExamResultRenderer())
    }

    override fun verify(command: CommandCall, evaluator: Evaluator, resultRecorder: ResultRecorder, fixture: Fixture) {
        super.verify(command.apply { resolve(evaluator) }, evaluator, resultRecorder, fixture)
    }

    protected fun CommandCall.resolve(eval: Evaluator) {
        Html(element.localName).text(
            config.resolver.resolve(content(normalize(element.text)), eval)
        ).el.also {
            element.appendSister(it)
            element.parentElement.removeChild(element)
            element = it
        }
    }

    companion object : KLogging()
}

fun CommandCall?.awaitConfig() = AwaitConfig(
    takeAttr("awaitAtMostSec")?.toLong(),
    takeAttr("awaitPollDelayMillis")?.toLong(),
    takeAttr("awaitPollIntervalMillis")?.toLong()
)

fun Html.awaitConfig(prefix: String = "await") = AwaitConfig(
    takeAwayAttr("${prefix}AtMostSec")?.toLong(),
    takeAwayAttr("${prefix}PollDelayMillis")?.toLong(),
    takeAwayAttr("${prefix}PollIntervalMillis")?.toLong()
)

data class AwaitConfig(val atMostSec: Long?, val pollDelay: Long?, val pollInterval: Long?) {
    fun enabled(): Boolean = !(atMostSec == null && pollDelay == null && pollInterval == null)
}

fun AwaitConfig.await(desc: String? = null): ConditionFactory = Awaitility.await(desc)
    .atMost(atMostSec ?: 4, TimeUnit.SECONDS)
    .pollDelay(pollDelay ?: 0, TimeUnit.MILLISECONDS)
    .pollInterval(pollInterval ?: 1000, TimeUnit.MILLISECONDS)

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
