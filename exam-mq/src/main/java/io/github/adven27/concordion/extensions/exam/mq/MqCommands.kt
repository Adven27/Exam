package io.github.adven27.concordion.extensions.exam.mq

import io.github.adven27.concordion.extensions.exam.core.ContentVerifier
import io.github.adven27.concordion.extensions.exam.core.ExamResultRenderer
import io.github.adven27.concordion.extensions.exam.core.commands.AwaitConfig
import io.github.adven27.concordion.extensions.exam.core.commands.ExamCommand
import io.github.adven27.concordion.extensions.exam.core.commands.ExamVerifyCommand
import io.github.adven27.concordion.extensions.exam.core.commands.await
import io.github.adven27.concordion.extensions.exam.core.commands.awaitConfig
import io.github.adven27.concordion.extensions.exam.core.commands.timeoutMessage
import io.github.adven27.concordion.extensions.exam.core.errorMessage
import io.github.adven27.concordion.extensions.exam.core.html.CLASS
import io.github.adven27.concordion.extensions.exam.core.html.Html
import io.github.adven27.concordion.extensions.exam.core.html.caption
import io.github.adven27.concordion.extensions.exam.core.html.div
import io.github.adven27.concordion.extensions.exam.core.html.divCollapse
import io.github.adven27.concordion.extensions.exam.core.html.generateId
import io.github.adven27.concordion.extensions.exam.core.html.html
import io.github.adven27.concordion.extensions.exam.core.html.italic
import io.github.adven27.concordion.extensions.exam.core.html.pre
import io.github.adven27.concordion.extensions.exam.core.html.span
import io.github.adven27.concordion.extensions.exam.core.html.table
import io.github.adven27.concordion.extensions.exam.core.html.tbody
import io.github.adven27.concordion.extensions.exam.core.html.td
import io.github.adven27.concordion.extensions.exam.core.html.tr
import io.github.adven27.concordion.extensions.exam.core.html.trWithTDs
import io.github.adven27.concordion.extensions.exam.core.resolveForContentType
import io.github.adven27.concordion.extensions.exam.core.resolveJson
import io.github.adven27.concordion.extensions.exam.core.utils.content
import io.github.adven27.concordion.extensions.exam.core.utils.pretty
import io.github.adven27.concordion.extensions.exam.core.vars
import mu.KLogging
import net.javacrumbs.jsonunit.core.Configuration
import net.javacrumbs.jsonunit.core.Option
import net.javacrumbs.jsonunit.core.internal.Options
import org.concordion.api.CommandCall
import org.concordion.api.Evaluator
import org.concordion.api.Fixture
import org.concordion.api.Result.FAILURE
import org.concordion.api.ResultRecorder
import org.junit.Assert
import org.xmlunit.diff.NodeMatcher

interface MqTester {
    fun start()
    fun stop()
    fun send(message: Message, params: Map<String, String>)
    fun receive(): List<Message>
    fun purge()
    fun accumulateOnRetries(): Boolean = true

    open class NOOP : MqTester {
        override fun start() = Unit
        override fun stop() = Unit
        override fun send(message: Message, params: Map<String, String>) = Unit
        override fun receive(): List<Message> = listOf()
        override fun purge() = Unit
    }

    data class Message @JvmOverloads constructor(val body: String = "", val headers: Map<String, String> = emptyMap())
}

@Suppress("TooManyFunctions")
class MqCheckCommand(
    name: String,
    tag: String,
    private val originalCfg: Configuration,
    private val nodeMatcher: NodeMatcher,
    private val mqTesters: Map<String, MqTester>,
    private val contentVerifiers: Map<String, ContentVerifier>
) : ExamVerifyCommand(name, tag, ExamResultRenderer()) {

    companion object : KLogging()

    private lateinit var usedCfg: Configuration

    @Suppress("LongMethod", "TooGenericExceptionCaught")
    override fun verify(cmd: CommandCall, eval: Evaluator, resultRecorder: ResultRecorder, fixture: Fixture) {
        val root = cmd.html()
        usedCfg = originalCfg
        root.attr("jsonUnitOptions")?.let { attr -> overrideJsonUnitOption(attr) }
        val mqName = root.takeAwayAttr("name")!!
        val layout = root.takeAwayAttr("layout", "VERTICALLY")
        val contains = root.takeAwayAttr("contains", "EXACT")
        val collapsable = root.takeAwayAttr("collapsable", "false").toBoolean()
        val awaitConfig = cmd.awaitConfig()

        val expectedMessages: List<TypedMessage> = expectedMessages(root, eval)
        var actualMessages: List<MqTester.Message> = mqTesters.getOrFail(mqName).receive().toMutableList()

        try {
            try {
                Assert.assertEquals(expectedMessages.size, actualMessages.size)
            } catch (e: java.lang.AssertionError) {
                if (awaitConfig.enabled()) {
                    actualMessages =
                        awaitExpectedSize(expectedMessages, actualMessages, mqName, awaitConfig)
                } else {
                    throw e
                }
            }
        } catch (s: SizeMismatchError) {
            resultRecorder.record(FAILURE)
            root.removeChildren().below(
                sizeCheckError(s.mqName, s.expected, s.actual, s.message)
            )
            root.below(pre(awaitConfig.timeoutMessage(s.cause!!)).css("alert alert-danger small"))
            return
        } catch (e: java.lang.AssertionError) {
            resultRecorder.record(FAILURE)
            root.below(sizeCheckError(mqName, expectedMessages, actualMessages, e.message))
            root.parent().remove(root)
            return
        }

        val tableContainer = table()(captionEnvelopOpen(mqName))
        root.removeChildren().attrs("class" to "mq-check")(tableContainer)

        var cnt: Html? = null
        if (layout.toUpperCase() != "VERTICALLY") {
            cnt = tr()
            tableContainer(cnt)
        }

        if (expectedMessages.isEmpty()) {
            if (cnt == null) tableContainer(tr()(td("<EMPTY>"))) else cnt(td("<EMPTY>"))
        }

        expectedMessages.sortedTyped(contains).zip(actualMessages.sorted(contains)) { e, a -> VerifyPair(a, e) }
            .forEach {
                logger.info("Verifying {}", it)
                val bodyContainer = container("", collapsable, it.expected.type)
                val headersContainer =
                    span("Headers: ${it.expected.message.headers.entries.joinToString()}")(
                        italic("", CLASS to "fa fa-border")
                    )
                if (cnt != null) {
                    cnt(
                        td()(
                            table()(
                                if (it.expected.message.headers.isNotEmpty()) trWithTDs(headersContainer) else null,
                                tr()(if (collapsable) collapsed(bodyContainer) else bodyContainer)
                            )
                        )
                    )
                } else {
                    tableContainer(
                        if (it.expected.message.headers.isNotEmpty()) trWithTDs(headersContainer) else null,
                        tr()(if (collapsable) collapsed(bodyContainer) else bodyContainer)
                    )
                }
                checkHeaders(it.actual.headers, it.expected.message.headers, resultRecorder, headersContainer)
                checkContent(it.expected.type, it.actual.body, it.expected.message.body, resultRecorder, bodyContainer)
            }
    }

    private fun expectedMessages(root: Html, eval: Evaluator) = messageTags(root).mapNotNull { html ->
        setVarsIfPresent(html, eval)
        nullOrMessage(
            html.content(eval),
            html.takeAwayAttr("verifyAs", "json"),
            eval,
            attrToMap(html, eval, "headers")
        )
    }

    private fun awaitExpectedSize(
        expected: List<TypedMessage>,
        originalActual: List<MqTester.Message>,
        mqName: String,
        awaitConfig: AwaitConfig
    ): List<MqTester.Message> {
        val actual = originalActual.toMutableList()
        val prevActual: MutableList<MqTester.Message> = mutableListOf()
        try {
            val tester = mqTesters.getOrFail(mqName)
            awaitConfig.await("Await MQ $mqName").untilAsserted {
                actual
                    .apply { prevActual.apply { clear(); addAll(this) } }
                    .apply { if (!tester.accumulateOnRetries()) clear() }
                    .addAll(tester.receive())
                Assert.assertEquals(expected.size, actual.size)
            }
            return actual
        } catch (e: Exception) {
            throw SizeMismatchError(
                mqName,
                expected,
                prevActual,
                e.cause?.message ?: e.message ?: "$e",
                e
            )
        }
    }

    class SizeMismatchError(
        val mqName: String,
        val expected: List<TypedMessage>,
        val actual: List<MqTester.Message>,
        message: String,
        exception: Throwable
    ) : java.lang.AssertionError(message, exception)

    @Suppress("SpreadOperator")
    private fun sizeCheckError(
        mqName: String,
        expected: List<TypedMessage>,
        actual: List<MqTester.Message>,
        msg: String?
    ): Html = div().css("rest-failure bd-callout bd-callout-danger")(
        div(msg),
        *renderMessages("Expected: ", expected, mqName).toTypedArray(),
        *renderMessages("but was: ", actual.map { TypedMessage("xml", it) }, mqName).toTypedArray()
    )

    private fun messageTags(root: Html) =
        root.childs().filter { it.localName() == "message" }.ifEmpty { listOf(root) }

    private fun setVarsIfPresent(html: Html, eval: Evaluator) {
        html.takeAwayAttr("vars").vars(eval, true, html.takeAwayAttr("varsSeparator", ","))
    }

    private fun nullOrMessage(content: String, type: String, eval: Evaluator, headers: Map<String, String>) =
        if (content.isEmpty()) null
        else TypedMessage(type, MqTester.Message(eval.resolveForContentType(content.trim(), type), headers))

    private fun List<TypedMessage>.sortedTyped(contains: String) =
        if (needSort(contains)) this.sortedBy { it.message.body } else this

    private fun List<MqTester.Message>.sorted(contains: String) =
        if (needSort(contains)) this.sortedBy { it.body } else this

    private fun needSort(contains: String) = "EXACT" != contains

    private fun renderMessages(msg: String, messages: List<TypedMessage>, mqName: String) =
        listOf(
            span(msg),
            table()(
                captionEnvelopOpen(mqName),
                tbody()(
                    messages.map { tr()(container(it.message.body, type = it.type)) }
                )
            )
        )

    private fun container(txt: String, collapsable: Boolean = false, type: String) =
        container(txt, type, collapsable).style("margin: 0").attr("autoFormat", "true")

    @Suppress("TooGenericExceptionCaught")
    private fun checkHeaders(
        actual: Map<String, String>,
        expected: Map<String, String>,
        resultRecorder: ResultRecorder,
        root: Html
    ) {
        try {
            Assert.assertEquals(expected, actual)
            resultRecorder.pass(root)
        } catch (e: Throwable) {
            if (e is AssertionError || e is Exception) {
                resultRecorder.failure(root, actual.toString(), expected.toString())
                root.below(pre(e.message).css("alert alert-danger small"))
            } else throw e
        }
    }

    @Suppress("TooGenericExceptionCaught")
    private fun checkContent(
        type: String,
        actual: String,
        expected: String,
        resultRecorder: ResultRecorder,
        root: Html
    ) = try {
        checkAs(type, expected, actual)
        root.text(expected.pretty(type))
        resultRecorder.pass(root)
    } catch (e: Throwable) {
        if (e is AssertionError || e is Exception) {
            root.attr("class", "")
            val diff = div().css(type)
            val (_, errorMsg) = errorMessage(message = e.message ?: "", html = diff)
            resultRecorder.failure(diff, actual.pretty(type), expected.pretty(type))
            root(errorMsg)
        } else throw e
    }

    private fun checkAs(type: String, expected: String, actual: String) {
        contentVerifiers[type]?.verify(expected, actual, arrayOf(usedCfg, nodeMatcher))
            ?: ContentVerifier.Default().verify(expected, actual, emptyArray())
    }

    @Suppress("SpreadOperator")
    private fun overrideJsonUnitOption(attr: String) {
        val first = usedCfg.options.values().first()
        val other = usedCfg.options.values()
        other.remove(first)
        other.addAll(attr.split(";").filter { it.isNotEmpty() }.map { Option.valueOf(it) }.toSet())
        usedCfg = usedCfg.withOptions(Options(first, *other.toTypedArray()))
    }

    data class TypedMessage(val type: String, val message: MqTester.Message)
    data class VerifyPair(val actual: MqTester.Message, val expected: TypedMessage)
}

class MqSendCommand(name: String, tag: String, private val mqTesters: Map<String, MqTester>) : ExamCommand(name, tag) {
    override fun execute(
        commandCall: CommandCall,
        evaluator: Evaluator,
        resultRecorder: ResultRecorder,
        fixture: Fixture
    ) {
        super.execute(commandCall, evaluator, resultRecorder, fixture)
        commandCall.html().also { root ->
            Attrs.from(root, evaluator).also { attrs ->
                renderAndSend(root, attrs, evaluator.resolveJson(root.content(attrs.from, evaluator).trim()))
            }
        }
    }

    private fun renderAndSend(root: Html, attrs: Attrs, message: String) {
        renderCommand(root, attrs, message)
        sendMessage(message, attrs.mqName, attrs.headers, attrs.params)
    }

    private fun sendMessage(message: String, mq: String, headers: Map<String, String>, params: Map<String, String>) =
        mqTesters.getOrFail(mq).send(MqTester.Message(message, headers), params)

    private fun renderCommand(root: Html, attrs: Attrs, message: String) {
        root.removeChildren().attrs("class" to "mq-send")(
            table()(
                captionEnvelopClosed(attrs.mqName),
                renderHeaders(attrs.headers),
                renderMessage(message, attrs.collapsable, attrs.formatAs)
            )
        )
    }

    private fun renderMessage(message: String, collapsable: Boolean, formatAs: String) = tr()(
        if (collapsable) collapsed(collapsableContainer(message, formatAs))
        else td(message).css(formatAs) // exp-body
    )

    private fun renderHeaders(headers: Map<String, String>) = if (headers.isNotEmpty())
        caption("Headers: ${headers.entries.joinToString()}")(
            italic("", CLASS to "fa fa-border")
        )
    else null

    data class Attrs(
        val mqName: String,
        val from: String?,
        val formatAs: String = "json",
        val headers: Map<String, String>,
        val params: Map<String, String>,
        val vars: String?,
        val varsSeparator: String = ",",
        val collapsable: Boolean = false,
    ) {
        fun setVarsToContext(evaluator: Evaluator) {
            vars.vars(evaluator, true, varsSeparator)
        }

        companion object {
            private const val NAME = "name"
            private const val FROM = "from"
            private const val FORMAT_AS = "formatAs"
            private const val HEADERS = "headers"
            private const val PARAMS = "params"
            private const val VARS = "vars"
            private const val VARS_SEPARATOR = "varsSeparator"
            private const val COLLAPSABLE = "collapsable"

            fun from(root: Html, evaluator: Evaluator): Attrs {
                return Attrs(
                    root.attrOrFail(NAME),
                    root.takeAwayAttr(FROM),
                    root.takeAwayAttr(
                        FORMAT_AS, root.attr(FROM)?.substringAfterLast(".", "json") ?: "json"
                    ),
                    root.takeAwayAttr(HEADERS).attrToMap(evaluator),
                    root.takeAwayAttr(PARAMS).attrToMap(evaluator),
                    root.takeAwayAttr(VARS),
                    root.takeAwayAttr(VARS_SEPARATOR, ","),
                    root.takeAwayAttr(COLLAPSABLE, "false").toBoolean(),
                ).apply { setVarsToContext(evaluator) }
            }
        }
    }
}

class MqPurgeCommand(name: String, tag: String, private val mqTesters: Map<String, MqTester>) : ExamCommand(name, tag) {
    override fun execute(
        commandCall: CommandCall,
        evaluator: Evaluator,
        resultRecorder: ResultRecorder,
        fixture: Fixture
    ) {
        super.execute(commandCall, evaluator, resultRecorder, fixture)
        commandCall.html().also { root ->
            Attrs.from(root).also {
                mqTesters.getOrFail(it.mqName).purge()
                renderCommand(root, it.mqName)
            }
        }
    }

    private fun renderCommand(root: Html, mqName: String) {
        root.removeChildren()(
            table()(
                caption()(
                    italic(" $mqName purged", CLASS to "fa fa-envelope ml-1")
                )
            )
        )
    }

    data class Attrs(val mqName: String) {
        companion object {
            private const val NAME = "name"
            fun from(root: Html) = Attrs(root.attrOrFail(NAME))
        }
    }
}

private fun attrToMap(root: Html, eval: Evaluator, attr: String): Map<String, String> =
    root.takeAwayAttr(attr).attrToMap(eval)

private fun String?.attrToMap(eval: Evaluator): Map<String, String> =
    this?.vars(eval)?.mapValues { it.value.toString() } ?: emptyMap()

private fun Map<String, MqTester>.getOrFail(mqName: String?): MqTester = this[mqName]
    ?: throw IllegalArgumentException("MQ with name $mqName not registered in MqPlugin")

private fun captionEnvelopOpen(mqName: String) =
    caption()(italic(" $mqName", CLASS to "fa fa-envelope-open ml-1"))

private fun captionEnvelopClosed(mqName: String?) =
    caption()(italic(" $mqName", CLASS to "fa fa-envelope ml-1"))

private fun container(text: String, type: String, collapsable: Boolean) =
    if (collapsable) collapsableContainer(text, type) else fixedContainer(text, type)

private fun collapsed(container: Html) = td("class" to "exp-body")(
    div().style("position: relative")(
        divCollapse("", container.attr("id").toString()),
        container
    )
)

private fun fixedContainer(text: String, type: String) = td(text).css("$type exp-body")

private fun collapsableContainer(text: String, type: String) =
    div(text, "id" to generateId()).css("$type file collapse")
