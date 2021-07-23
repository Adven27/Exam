package io.github.adven27.concordion.extensions.exam.mq

import io.github.adven27.concordion.extensions.exam.core.ContentVerifier
import io.github.adven27.concordion.extensions.exam.core.ExamResultRenderer
import io.github.adven27.concordion.extensions.exam.core.commands.AwaitConfig
import io.github.adven27.concordion.extensions.exam.core.commands.ExamCommand
import io.github.adven27.concordion.extensions.exam.core.commands.ExamVerifyCommand
import io.github.adven27.concordion.extensions.exam.core.commands.FromAttrs
import io.github.adven27.concordion.extensions.exam.core.commands.VarsAttrs
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
import io.github.adven27.concordion.extensions.exam.core.utils.pretty
import io.github.adven27.concordion.extensions.exam.core.vars
import mu.KLogging
import net.javacrumbs.jsonunit.core.Configuration
import net.javacrumbs.jsonunit.core.Option
import org.concordion.api.CommandCall
import org.concordion.api.Evaluator
import org.concordion.api.Fixture
import org.concordion.api.Result.FAILURE
import org.concordion.api.ResultRecorder
import org.junit.Assert.assertEquals
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
        Attrs(root).let { attrs ->
            usedCfg = attrs.jsonUnitOptions?.let { originalCfg.overrideJsonUnitOption(it) } ?: originalCfg

            val expectedMessages: List<TypedMessage> = expectedMessages(root, eval)
            var actualMessages: List<MqTester.Message> = mqTesters.getOrFail(attrs.mqName).receive().toMutableList()

            try {
                try {
                    assertEquals(expectedMessages.size, actualMessages.size)
                } catch (e: java.lang.AssertionError) {
                    if (attrs.awaitConfig.enabled()) {
                        actualMessages =
                            awaitExpectedSize(expectedMessages, actualMessages, attrs.mqName, attrs.awaitConfig)
                    } else {
                        throw e
                    }
                }
            } catch (s: SizeMismatchError) {
                resultRecorder.record(FAILURE)
                root.removeChildren().below(
                    sizeCheckError(s.mqName, s.expected, s.actual, s.message)
                )
                root.below(pre(attrs.awaitConfig.timeoutMessage(s.cause!!)).css("alert alert-danger small"))
                return
            } catch (e: java.lang.AssertionError) {
                resultRecorder.record(FAILURE)
                root.below(sizeCheckError(attrs.mqName, expectedMessages, actualMessages, e.message))
                root.parent().remove(root)
                return
            }

            val tableContainer = table()(captionEnvelopOpen(attrs.mqName))
            root.removeChildren().attrs("class" to "mq-check")(tableContainer)

            var cnt: Html? = null
            if (!attrs.vertically) {
                cnt = tr()
                tableContainer(cnt)
            }

            if (expectedMessages.isEmpty()) {
                if (cnt == null) tableContainer(tr()(td("<EMPTY>"))) else cnt(td("<EMPTY>"))
            }

            expectedMessages.sortedTyped(attrs.contains)
                .zip(actualMessages.sorted(attrs.contains)) { e, a -> VerifyPair(a, e) }
                .forEach {
                    logger.info("Verifying {}", it)
                    val bodyContainer = container("", attrs.collapsable, it.expected.type)
                    val headersContainer = it.expected.message.headers.renderHeaders()
                    if (cnt != null) {
                        cnt(
                            td()(
                                table()(
                                    tr()(td()(headersContainer)),
                                    tr()(if (attrs.collapsable) collapsed(bodyContainer) else bodyContainer)
                                )
                            )
                        )
                    } else {
                        tableContainer(
                            tr()(td()(headersContainer)),
                            tr()(if (attrs.collapsable) collapsed(bodyContainer) else bodyContainer)
                        )
                    }
                    headersContainer?.let { container ->
                        checkHeaders(
                            it.actual.headers,
                            it.expected.message.headers,
                            resultRecorder,
                            container
                        )
                    }
                    checkContent(
                        it.expected.type,
                        it.actual.body,
                        it.expected.message.body,
                        resultRecorder,
                        bodyContainer
                    )
                }
        }
    }

    private fun expectedMessages(root: Html, eval: Evaluator) = messageTags(root).mapNotNull { html ->
        MessageAttrs(html, eval).let { nullOrMessage(it.from, it.headers) }
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
                assertEquals(expected.size, actual.size)
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

    private fun nullOrMessage(from: FromAttrs, headers: Map<String, String>) =
        if (from.content.isEmpty()) null
        else TypedMessage(from.contentType, MqTester.Message(from.content, headers))

    private fun List<TypedMessage>.sortedTyped(contains: String) =
        if (needSort(contains)) sortedBy { it.message.body } else this

    private fun List<MqTester.Message>.sorted(contains: String) =
        if (needSort(contains)) sortedBy { it.body } else this

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
            assertEquals(expected, actual)
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

    private fun checkAs(type: String, expected: String, actual: String) =
        contentVerifiers[type]?.verify(expected, actual, arrayOf(usedCfg, nodeMatcher))
            ?: ContentVerifier.Default().verify(expected, actual, emptyArray())

    @Suppress("SpreadOperator")
    private fun Configuration.overrideJsonUnitOption(attr: String): Configuration =
        (options.values() + attr.split(";").filter { it.isNotEmpty() }.map { Option.valueOf(it) }).let {
            withOptions(it.first(), *it.toTypedArray())
        }

    data class TypedMessage(val type: String, val message: MqTester.Message)
    data class VerifyPair(val actual: MqTester.Message, val expected: TypedMessage)

    class MessageAttrs(root: Html, evaluator: Evaluator) {
        private val verifyAs: String? = root.takeAwayAttr(VERIFY_AS)
        val from: FromAttrs = FromAttrs(root, evaluator, verifyAs)
        val vars: VarsAttrs = VarsAttrs(root, evaluator)
        val headers: Map<String, String> = root.takeAwayAttr(HEADERS).attrToMap(evaluator)

        companion object {
            private const val VERIFY_AS = "verifyAs"
            private const val HEADERS = "headers"
        }
    }

    class Attrs(root: Html) {
        val mqName: String = root.attrOrFail(NAME)
        val vertically = root.takeAwayAttr("layout", "VERTICALLY").uppercase() == "VERTICALLY"
        val contains = root.takeAwayAttr("contains", "EXACT")
        val collapsable = root.takeAwayAttr("collapsable", "false").toBoolean()
        val jsonUnitOptions = root.attr("jsonUnitOptions")
        val awaitConfig = root.awaitConfig()

        companion object {
            private const val NAME = "name"
        }
    }

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
            Attrs(root, evaluator).also { attrs ->
                renderAndSend(root, attrs, attrs.from.content)
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
            table().css("table-borderless")(
                captionEnvelopClosed(attrs.mqName),
                tr()(td()(attrs.headers.renderHeaders())),
                renderMessage(message, attrs.collapsable, attrs.formatAs ?: attrs.from.contentType)
            )
        )
    }

    private fun renderMessage(message: String, collapsable: Boolean, formatAs: String) = tr()(
        if (collapsable) collapsed(collapsableContainer(message, formatAs))
        else td()(pre(message).css(formatAs))
    )

    class Attrs(root: Html, evaluator: Evaluator) {
        val mqName: String = root.attrOrFail(NAME)
        val from: FromAttrs = FromAttrs(root, evaluator)
        val formatAs: String? = root.takeAwayAttr(FORMAT_AS)
        val headers: Map<String, String> = root.takeAwayAttr(HEADERS).attrToMap(evaluator)
        val params: Map<String, String> = root.takeAwayAttr(PARAMS).attrToMap(evaluator)
        val vars: VarsAttrs = VarsAttrs(root, evaluator)
        val collapsable: Boolean = root.takeAwayAttr(COLLAPSABLE, "false").toBoolean()

        companion object {
            private const val NAME = "name"
            private const val FORMAT_AS = "formatAs"
            private const val HEADERS = "headers"
            private const val PARAMS = "params"
            private const val COLLAPSABLE = "collapsable"
        }
    }
}

private fun Map<String, String>.renderHeaders() = if (isNotEmpty())
    table()(
        caption("Headers").css("small"),
        *this.map { tr()(td(it.key), td(it.value)) }.toTypedArray()
    )
else null

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
        divCollapse("", container.attr("id").toString()).css("default-collapsed"),
        container
    )
)

private fun fixedContainer(text: String, type: String) = td(text).css("$type exp-body")

private fun collapsableContainer(text: String, type: String) =
    div(text, "id" to generateId()).css("$type file collapse show")
