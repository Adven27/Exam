package io.github.adven27.concordion.extensions.exam.mq.commands.send

import io.github.adven27.concordion.extensions.exam.core.commands.SetUpEvent
import io.github.adven27.concordion.extensions.exam.core.commands.SetUpListener
import io.github.adven27.concordion.extensions.exam.core.escapeHtml
import io.github.adven27.concordion.extensions.exam.core.pretty
import io.github.adven27.concordion.extensions.exam.core.toHtml
import io.github.adven27.concordion.extensions.exam.mq.MqCheckCommand
import io.github.adven27.concordion.extensions.exam.mq.commands.send.SendCommand.Send

class SendRenderer : SetUpListener<Send> {
    override fun setUpCompleted(event: SetUpEvent<Send>) {
        event.element.parentElement.parentElement.let {
            it.addAttribute("hidden", "")
            it.appendSister(template(event.target.queue, event.target.messages).toHtml().el)
        }
    }
}

fun template(name: String, messages: List<MqCheckCommand.ParametrizedTypedMessage>) = //language=html
    """
<div class="mq-send">
    <table class="table table-sm caption-top">
        <caption><i class="fa fa-envelope me-1"> </i><span>$name</span></caption>
        <tbody> ${renderMessages(messages)} </tbody>
    </table>
</div>
""".trimIndent()

//language=html
fun renderMessages(messages: List<MqCheckCommand.ParametrizedTypedMessage>) = messages.joinToString("\n") {
    """
${renderHeaders(it.headers)}
<tr><td class='exp-body'>${renderBody(it)}</td></tr>
""".trimIndent()
}.ifEmpty { """<tr><td class='exp-body'>EMPTY</td></tr>""" }

// language=html
fun renderBody(msg: MqCheckCommand.ParametrizedTypedMessage): String =
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