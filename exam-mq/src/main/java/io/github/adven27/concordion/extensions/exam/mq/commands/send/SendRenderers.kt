package io.github.adven27.concordion.extensions.exam.mq.commands.send

import io.github.adven27.concordion.extensions.exam.core.commands.SetUpEvent
import io.github.adven27.concordion.extensions.exam.core.commands.SuitableSetUpListener
import io.github.adven27.concordion.extensions.exam.core.escapeHtml
import io.github.adven27.concordion.extensions.exam.core.pretty
import io.github.adven27.concordion.extensions.exam.core.toHtml
import io.github.adven27.concordion.extensions.exam.mq.ParametrizedTypedMessage
import io.github.adven27.concordion.extensions.exam.mq.commands.send.SendCommand.Send
import org.concordion.api.Element
import org.concordion.api.listener.AbstractElementEvent

class MdSendRenderer : BaseSendRenderer() {
    override fun root(event: AbstractElementEvent): Element = event.element.parentElement.parentElement
    override fun isSuitFor(element: Element) = element.localName != "div"
}

class HtmlSendRenderer : BaseSendRenderer() {
    override fun root(event: AbstractElementEvent): Element = event.element
    override fun isSuitFor(element: Element) = element.localName == "div"
}

abstract class BaseSendRenderer : SuitableSetUpListener<Send>() {
    abstract fun root(event: AbstractElementEvent): Element

    override fun setUpCompleted(event: SetUpEvent<Send>) {
        with(root(event)) {
            appendSister(template(event.target.queue, event.target.messages).toHtml().el)
            parentElement.removeChild(this)
        }
    }
}

fun template(name: String, messages: List<ParametrizedTypedMessage>) = //language=html
    """
<div class="mq-send">
    <table class="table table-sm caption-top">
        <caption><i class="fa fa-envelope me-1"> </i><span>$name</span></caption>
        <tbody> ${renderMessages(messages)} </tbody>
    </table>
</div>
""".trimIndent()

//language=html
fun renderMessages(messages: List<ParametrizedTypedMessage>) = messages.joinToString("\n") {
    """
${renderHeaders(it.headers)}
<tr><td class='exp-body'>${renderBody(it)}</td></tr>
""".trimIndent()
}.ifEmpty { """<tr><td class='exp-body'>EMPTY</td></tr>""" }

// language=html
fun renderBody(msg: ParametrizedTypedMessage): String =
    """<div class="${msg.type}"></div>""".toHtml().text(msg.body.pretty(msg.type)).el.toXML()

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
private fun toRows(headers: Map<String, String>) = headers.entries.joinToString("\n") { (k, v) ->
    """<tr><td>$k</td><td>${v.escapeHtml()}</td></tr>"""
}