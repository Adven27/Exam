package io.github.adven27.concordion.extensions.exam.mq.commands.check

import io.github.adven27.concordion.extensions.exam.core.ContentVerifier
import io.github.adven27.concordion.extensions.exam.core.ContentVerifier.ExpectedContent
import io.github.adven27.concordion.extensions.exam.core.commands.SuitableResultRenderer
import io.github.adven27.concordion.extensions.exam.core.commands.VerifyFailureEvent
import io.github.adven27.concordion.extensions.exam.core.commands.VerifySuccessEvent
import io.github.adven27.concordion.extensions.exam.core.errorMessage
import io.github.adven27.concordion.extensions.exam.core.escapeHtml
import io.github.adven27.concordion.extensions.exam.core.html.Html
import io.github.adven27.concordion.extensions.exam.core.html.div
import io.github.adven27.concordion.extensions.exam.core.html.pre
import io.github.adven27.concordion.extensions.exam.core.html.span
import io.github.adven27.concordion.extensions.exam.core.toHtml
import io.github.adven27.concordion.extensions.exam.mq.commands.check.CheckCommand.Actual
import io.github.adven27.concordion.extensions.exam.mq.commands.check.CheckCommand.Expected
import io.github.adven27.concordion.extensions.exam.mq.commands.check.MqVerifier.MessageVerifyResult
import org.concordion.api.Element
import org.concordion.api.listener.AbstractElementEvent

class HtmlResultRenderer : SuitableResultRenderer<Expected, Actual>() {
    override fun isSuitFor(element: Element) = element.localName == "div"
    private fun root(event: AbstractElementEvent) = event.element.parentElement.parentElement

    override fun successReported(event: VerifySuccessEvent<Expected, Actual>) = with(root(event)) {
        appendSister(
            template(
                event.expected.queue,
                event.expected.messages.map {
                    MessageVerifyResult(
                        Result.success(it.headers),
                        Result.success(ExpectedContent(it.type, it.body))
                    )
                }
            ).toHtml().el
        )
        parentElement.removeChild(this)
    }

    override fun failureReported(event: VerifyFailureEvent<Expected>) = with(root(event)) {
        appendSister(
            when (event.fail) {
                is MqVerifier.SizeVerifyingError -> renderSizeError(event)
                is MqVerifier.MessageVerifyingError -> renderMessageContentError(event)
                else -> div()(
                    pre(event.fail.toString()),
                    pre(event.expected.toString()),
                ).el
            }
        )
        parentElement.removeChild(this)
    }

    private fun renderMessageContentError(event: VerifyFailureEvent<Expected>) =
        template(event.expected.queue, (event.fail as MqVerifier.MessageVerifyingError).expected).toHtml().el

    private fun renderSizeError(event: VerifyFailureEvent<Expected>) =
        errorMessage(
            message = event.fail.message ?: "",
            type = "json",
            html = div()(
                span("Expected:"),
                template(
                    event.expected.queue,
                    event.expected.messages.map {
                        MessageVerifyResult(
                            Result.success(it.headers),
                            Result.success(ExpectedContent(it.type, it.body))
                        )
                    }
                ).toHtml(),
                span("but was:"),
                template(
                    event.expected.queue,
                    (event.fail as MqVerifier.SizeVerifyingError).actual.map {
                        MessageVerifyResult(
                            Result.success(it.headers),
                            Result.success(ExpectedContent("text", it.body))
                        )
                    }
                ).toHtml(),
            )
        ).second.el
}

class MdResultRenderer : SuitableResultRenderer<Expected, Actual>() {
    override fun isSuitFor(element: Element) = element.localName != "div"

    private fun root(event: AbstractElementEvent) = event.element.parentElement.parentElement

    override fun successReported(event: VerifySuccessEvent<Expected, Actual>) = with(root(event)) {
        addAttribute("hidden", "")
        appendSister(
            div()(
                pre(event.expected.toString()),
                pre(event.actual.toString())
            ).el
        )
    }

    override fun failureReported(event: VerifyFailureEvent<Expected>) = with(root(event)) {
        addAttribute("hidden", "")
        appendSister(
            div()(
                pre(event.fail.toString()),
                pre(event.expected.toString()),
            ).el
        )
    }
}

private fun template(name: String, messages: List<MessageVerifyResult>) = //language=html
    """
    <div class="mq-check">
        <table class="table table-sm caption-top">
            <caption><i class="fa fa-envelope-open me-1"> </i><span>$name</span></caption>
            <tbody> ${renderMessages(messages)} </tbody>
        </table>
    </div>
    """.trimIndent()

// language=html
private fun renderMessages(messages: List<MessageVerifyResult>) = messages.joinToString("\n") { result ->
    """
    ${result.headers.fold(::renderHeaders, ::renderHeadersError)}
    <tr><td class='exp-body'>${result.content.fold(::renderBody, ::renderBodyError)}</td></tr>
    """.trimIndent()
}.ifEmpty { """<tr><td class='exp-body'>EMPTY</td></tr>""" }

// language=html
private fun renderBody(body: ExpectedContent) =
    """<div class="${body.type} rest-success"></div>""".toHtml().text(body.pretty()).el.toXML()

// language=html
private fun renderBodyError(error: Throwable) = when (error) {
    is ContentVerifier.Fail -> errorMessage(
        message = error.details,
        type = "json",
        html = div("class" to "${error.type} rest-failure")(
            Html("del", error.expected, "class" to "expected"),
            Html("ins", error.actual, "class" to "actual"),
        )
    ).second.el.toXML()
    else -> """<div class="json exp-body rest-failure">${error.message?.escapeHtml()}</div>"""
}

// language=html
private fun renderHeaders(headers: Map<String, String>) = if (headers.isNotEmpty())
    """
    <tr><td> 
        <table class="table table-sm caption-top">
            <caption class="small">Headers</caption>
            <tbody> ${toRows(headers)} </tbody>
        </table> 
    </td></tr>
    """.trimIndent()
else ""

// language=html
private fun renderHeadersError(error: Throwable) = when (error) {
    is MqVerifier.HeadersSizeVerifyingError -> "<tr><td> ${expectedButWas(error)} </td></tr>"
    is MqVerifier.HeadersVerifyingError -> "<tr><td> ${templateHeaders { toRows(error.result) }} </td></tr>"
    else -> "<tr><td> <code>${error.message?.escapeHtml()}</code> </td></tr>"
}

// language=html
private fun expectedButWas(error: MqVerifier.HeadersSizeVerifyingError) = errorMessage(
    message = error.message ?: "",
    type = "json",
    html = div()(
        span("Expected:"),
        templateHeaders { toRows(error.expected) }.toHtml(),
        span("but was:"),
        templateHeaders { toRows(error.actual) }.toHtml(),
    )
).second.el.toXML()

// language=html
private fun templateHeaders(rows: () -> String) = """
<table class="table table-sm caption-top">
    <caption class="small"> Headers </caption>
    <tbody> ${rows()} </tbody>
</table>
""".trimIndent()

// language=html
private fun toRows(headers: Map<String, String>) = headers.entries.joinToString("\n") { (k, v) ->
    """<tr><td class="rest-success">$k</td><td class="rest-success">${v.escapeHtml()}</td></tr>"""
}

// language=html
private fun toRows(headers: List<MqVerifier.HeaderCheckResult>) = headers.joinToString("\n") {
    "<tr> ${tdResult(it.actualKey, it.header.first)} ${tdResult(it.actualValue, it.header.second)} </tr>"
}

// language=html
private fun tdResult(actual: String?, expected: String) =
    if (actual == null) """<td class="rest-success">${expected.escapeHtml()}</td>"""
    else """<td class="rest-failure"><del>${expected.escapeHtml()}</del><ins>${actual.escapeHtml()}</ins></td>"""
