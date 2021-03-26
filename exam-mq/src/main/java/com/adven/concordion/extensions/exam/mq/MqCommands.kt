package com.adven.concordion.extensions.exam.mq

import com.adven.concordion.extensions.exam.core.ContentVerifier
import com.adven.concordion.extensions.exam.core.ExamResultRenderer
import com.adven.concordion.extensions.exam.core.commands.AwaitConfig
import com.adven.concordion.extensions.exam.core.commands.ExamCommand
import com.adven.concordion.extensions.exam.core.commands.ExamVerifyCommand
import com.adven.concordion.extensions.exam.core.commands.await
import com.adven.concordion.extensions.exam.core.commands.awaitConfig
import com.adven.concordion.extensions.exam.core.commands.timeoutMessage
import com.adven.concordion.extensions.exam.core.errorMessage
import com.adven.concordion.extensions.exam.core.html.CLASS
import com.adven.concordion.extensions.exam.core.html.Html
import com.adven.concordion.extensions.exam.core.html.caption
import com.adven.concordion.extensions.exam.core.html.div
import com.adven.concordion.extensions.exam.core.html.divCollapse
import com.adven.concordion.extensions.exam.core.html.html
import com.adven.concordion.extensions.exam.core.html.italic
import com.adven.concordion.extensions.exam.core.html.pre
import com.adven.concordion.extensions.exam.core.html.span
import com.adven.concordion.extensions.exam.core.html.table
import com.adven.concordion.extensions.exam.core.html.tbody
import com.adven.concordion.extensions.exam.core.html.td
import com.adven.concordion.extensions.exam.core.html.tr
import com.adven.concordion.extensions.exam.core.html.trWithTDs
import com.adven.concordion.extensions.exam.core.resolveForContentType
import com.adven.concordion.extensions.exam.core.resolveJson
import com.adven.concordion.extensions.exam.core.utils.content
import com.adven.concordion.extensions.exam.core.utils.pretty
import com.adven.concordion.extensions.exam.core.vars
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
import java.util.UUID

interface MqTester {
    fun start()
    fun stop()
    fun send(message: String, headers: Map<String, String>)
    fun receive(): List<Message>
    fun purge()
    fun accumulateOnRetries(): Boolean = true

    open class NOOP : MqTester {
        override fun start() = Unit
        override fun stop() = Unit
        override fun send(message: String, headers: Map<String, String>) = Unit
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

    private lateinit var usedCfg: Configuration

    override fun verify(cmd: CommandCall, eval: Evaluator, resultRecorder: ResultRecorder, fixture: Fixture) {
        val root = cmd.html()
        usedCfg = originalCfg
        root.attr("jsonUnitOptions")?.let { attr -> overrideJsonUnitOption(attr) }
        val mqName = root.takeAwayAttr("name")!!
        val layout = root.takeAwayAttr("layout", "VERTICALLY")
        val contains = root.takeAwayAttr("contains", "EXACT")
        val collapsable = root.takeAwayAttr("collapsable", "false").toBoolean()
        val awaitConfig = cmd.awaitConfig()

        val expectedMessages: List<TypedMessage> = messageTags(root).mapNotNull { html ->
            setVarsIfPresent(html, eval)
            nullOrMessage(html.content(eval), html.takeAwayAttr("verifyAs", "json"), eval, headers(html, eval))
        }
        val actualMessages: MutableList<MqTester.Message> = mqTesters.getOrFail(mqName).receive().toMutableList()

        try {
            checkSize(awaitConfig, mqName, actualMessages, expectedMessages, resultRecorder, root)
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

    @Suppress("LongParameterList", "TooGenericExceptionCaught")
    private fun checkSize(
        awaitConfig: AwaitConfig,
        mqName: String,
        actual: MutableList<MqTester.Message>,
        expected: List<TypedMessage>,
        resultRecorder: ResultRecorder,
        root: Html
    ) {
        var result = actual
        if (awaitConfig.enabled()) {
            try {
                awaitConfig.await("Await MQ $mqName").untilAsserted {
                    val list = mqTesters.getOrFail(mqName).receive().toMutableList()
                    if (mqTesters.getOrFail(mqName).accumulateOnRetries()) {
                        result.addAll(list)
                    } else {
                        result = list
                    }
                    Assert.assertEquals(expected.size, result.size)
                }
            } catch (e: Exception) {
                resultRecorder.record(FAILURE)
                root.removeChildren().below(
                    sizeCheckError(mqName, expected, result, e.cause?.message)
                )
                root.below(pre(awaitConfig.timeoutMessage(e)).css("alert alert-danger small"))
            }
        } else {
            Assert.assertEquals(expected.size, result.size)
        }
    }

    @Suppress("SpreadOperator")
    private fun sizeCheckError(
        mqName: String,
        expected: List<TypedMessage>,
        actual: MutableList<MqTester.Message>,
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
            val errorMsg = errorMessage(message = e.message ?: "", html = diff)
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
        val root = commandCall.html()
        val mqName = root.takeAwayAttr("name")
        val formatAs = root.takeAwayAttr("formatAs", root.attr("from")?.substringAfterLast(".", "json") ?: "json")
        val collapsable = root.takeAwayAttr("collapsable", "false").toBoolean()
        val headers = headers(root, evaluator)
        root.takeAwayAttr("vars").vars(evaluator, true, root.takeAwayAttr("varsSeparator", ","))
        val message = evaluator.resolveJson(root.content(evaluator).trim())
        root.removeChildren().attrs("class" to "mq-send")(
            table()(
                captionEnvelopClosed(mqName),
                if (headers.isNotEmpty()) caption("Headers: ${headers.entries.joinToString()}")(
                    italic("", CLASS to "fa fa-border")
                ) else null,
                tr()(
                    if (collapsable) collapsed(collapsableContainer(message, formatAs))
                    else td(message).css(formatAs) // exp-body
                )
            )
        )
        mqTesters.getOrFail(mqName).send(message, headers)
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
        val root = commandCall.html()
        val mqName = root.takeAwayAttr("name")
        mqTesters.getOrFail(mqName).purge()
        root.removeChildren()(
            table()(
                caption()(italic(" $mqName purged", CLASS to "fa fa-envelope ml-1"))
            )
        )
    }
}

private fun headers(root: Html, eval: Evaluator): Map<String, String> =
    root.takeAwayAttr("headers")?.vars(eval)?.mapValues { it.value.toString() } ?: emptyMap()

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
        divCollapse("", container.attr("id").toString()).css("fa fa-expand collapsed"),
        container
    )
)

private fun fixedContainer(text: String, type: String) = td(text).css("$type exp-body")

private fun collapsableContainer(text: String, type: String) =
    div(text, "id" to UUID.randomUUID().toString()).css("$type file collapse")
