package io.github.adven27.concordion.extensions.exam.core.commands

import io.github.adven27.concordion.extensions.exam.core.ContentTypeConfig
import io.github.adven27.concordion.extensions.exam.core.ExamExtension
import io.github.adven27.concordion.extensions.exam.core.ExamResultRenderer
import io.github.adven27.concordion.extensions.exam.core.TextContentTypeConfig
import io.github.adven27.concordion.extensions.exam.core.content
import io.github.adven27.concordion.extensions.exam.core.html.Html
import io.github.adven27.concordion.extensions.exam.core.html.codeHighlight
import io.github.adven27.concordion.extensions.exam.core.html.descendantTextContainer
import io.github.adven27.concordion.extensions.exam.core.html.html
import io.github.adven27.concordion.extensions.exam.core.resolveForContentType
import io.github.adven27.concordion.extensions.exam.core.rootCauseMessage
import io.github.adven27.concordion.extensions.exam.core.vars
import mu.KLogging
import nu.xom.Attribute
import org.awaitility.Awaitility
import org.awaitility.core.ConditionFactory
import org.concordion.api.AbstractCommand
import org.concordion.api.Command
import org.concordion.api.CommandCall
import org.concordion.api.Evaluator
import org.concordion.api.Fixture
import org.concordion.api.ResultRecorder
import org.concordion.api.listener.ExecuteEvent
import org.concordion.api.listener.ExecuteListener
import org.concordion.internal.CatchAllExpectationChecker.normalize
import org.concordion.internal.command.AssertEqualsCommand
import java.util.concurrent.TimeUnit.MILLISECONDS
import java.util.concurrent.TimeUnit.SECONDS

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

open class ExamCommand(
    name: String,
    override val tag: String
) : BeforeParseExamCommand, BaseExamCommand(name) {
    private val listener = ExecuteListener {}

    override fun execute(
        commandCall: CommandCall,
        evaluator: Evaluator,
        resultRecorder: ResultRecorder,
        fixture: Fixture
    ) {
        commandCall.children.processSequentially(evaluator, resultRecorder, fixture)
        listener.executeCompleted(ExecuteEvent(commandCall.element))
    }
}

open class ExamAssertEqualsCommand(
    override val name: String,
    val config: ContentTypeConfig = TextContentTypeConfig(),
    val content: (text: String) -> String = { it }
) : NamedExamCommand, AssertEqualsCommand(
    Comparator { actual, expected ->
        config.verifier.verify(normalize(expected), normalize(actual)).fold({ 0 }, { -1 })
    }
) {
    init {
        addAssertEqualsListener(ExamResultRenderer())
    }

    override fun verify(command: CommandCall, evaluator: Evaluator, resultRecorder: ResultRecorder, fixture: Fixture) {
        super.verify(command.apply { resolve(evaluator) }, evaluator, resultRecorder, fixture)
    }

    protected fun CommandCall.resolve(eval: Evaluator) {
        swapText(config.resolver.resolve(content(normalize(element.text)), eval))
    }

    private fun CommandCall.swapText(value: String) {
        Html(descendantTextContainer(element)).removeChildren()(
            codeHighlight(value, config.printer.style()).apply { el.appendNonBreakingSpaceIfBlank() }
        )
    }

    companion object : KLogging()
}

fun CommandCall?.awaitConfig(): AwaitConfig? = html().awaitConfig()

fun Html.awaitConfig(prefix: String = "await") = AwaitConfig.build(
    takeAwayAttr("${prefix}AtMostSec".decap())?.toLong(),
    takeAwayAttr("${prefix}PollDelayMillis".decap())?.toLong(),
    takeAwayAttr("${prefix}PollIntervalMillis".decap())?.toLong()
)

fun String.decap() = replaceFirstChar { it.lowercase() }

data class AwaitConfig(
    val atMostSec: Long = DEFAULT_AT_MOST_SEC,
    val pollDelay: Long = DEFAULT_POLL_DELAY,
    val pollInterval: Long = DEFAULT_POLL_INTERVAL
) {
    fun timeoutMessage(e: Throwable) = "Check didn't complete within $atMostSec seconds " +
        "(poll delay $pollDelay ms, interval $pollInterval ms) because ${e.rootCauseMessage()}"

    fun await(desc: String? = null): ConditionFactory = Awaitility.await(desc)
        .atMost(atMostSec, SECONDS)
        .pollDelay(pollDelay, MILLISECONDS)
        .pollInterval(pollInterval, MILLISECONDS)

    companion object {
        var DEFAULT_AT_MOST_SEC = 4L
        var DEFAULT_POLL_DELAY = 0L
        var DEFAULT_POLL_INTERVAL = 1000L

        fun build(atMostSec: Long?, pollDelay: Long?, pollInterval: Long?): AwaitConfig? =
            if (enabled(atMostSec, pollDelay, pollInterval)) {
                AwaitConfig(
                    atMostSec ?: DEFAULT_AT_MOST_SEC,
                    pollDelay ?: DEFAULT_POLL_DELAY,
                    pollInterval ?: DEFAULT_POLL_INTERVAL
                )
            } else null

        private fun enabled(atMostSec: Long?, pollDelay: Long?, pollInterval: Long?) =
            !(atMostSec == null && pollDelay == null && pollInterval == null)
    }
}

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
