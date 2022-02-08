package io.github.adven27.concordion.extensions.exam.mq.commands

import io.github.adven27.concordion.extensions.exam.core.commands.SuitableCommandParser
import io.github.adven27.concordion.extensions.exam.core.html.Html
import io.github.adven27.concordion.extensions.exam.core.resolveForContentType
import io.github.adven27.concordion.extensions.exam.core.resolveNoType
import io.github.adven27.concordion.extensions.exam.mq.MessageAttrs
import io.github.adven27.concordion.extensions.exam.mq.ParametrizedTypedMessage
import org.concordion.api.CommandCall
import org.concordion.api.Element
import org.concordion.api.Evaluator

interface QueueParser : SuitableCommandParser<List<ParametrizedTypedMessage>>

class MdQueueParser(private val typeAttr: String = "verifyAs") : QueueParser {
    override fun isSuitFor(element: Element): Boolean =
        element.localName != "div"

    @Suppress("ComplexMethod")
    override fun parse(command: CommandCall, evaluator: Evaluator) = root(command).childElements
        .filter { it.localName == "dd" }
        .mapNotNull { dd ->
            var type = "json"
            val headers: MutableMap<String, String> = mutableMapOf()
            val params: MutableMap<String, String> = mutableMapOf()
            var payload: String? = null

            dd.childElements.forEach { el ->
                when (el.localName) {
                    "pre" -> payload = el.text
                    "a" -> payload = parsePayload(el, evaluator)
                    "code" -> parseOption(el).also { (name, value) -> if (name == typeAttr) type = value }
                    "em" -> parseOption(el).also { (name, value) -> headers[name] = value }
                    "strong" -> parseOption(el).also { (name, value) -> params[name] = value }
                    "p" -> el.childElements.forEach { pChild ->
                        when (pChild.localName) {
                            "pre" -> payload = pChild.text
                            "a" -> payload = parsePayload(pChild, evaluator)
                            "code" -> parseOption(pChild).also { (name, value) -> if (name == typeAttr) type = value }
                            "em" -> parseOption(pChild).also { (name, value) -> headers[name] = value }
                            "strong" -> parseOption(pChild).also { (name, value) -> params[name] = value }
                        }
                    }
                }
            }
            payload?.let { body ->
                ParametrizedTypedMessage(
                    type,
                    evaluator.resolveForContentType(body, type),
                    headers.mapValues { evaluator.resolveNoType(it.value) },
                    params.mapValues { evaluator.resolveNoType(it.value) }
                )
            }
        }

    private fun root(command: CommandCall) = command.element.parentElement.parentElement
}

class HtmlQueueParser : QueueParser {
    override fun isSuitFor(element: Element): Boolean = element.localName == "div"

    override fun parse(command: CommandCall, evaluator: Evaluator): List<ParametrizedTypedMessage> =
        with(root(command)) {
            childElements
                .filter { it.localName == "message" }
                .map { toMsg(it, evaluator) }
                .ifEmpty {
                    if (getAttributeValue("from") != null || text.isNotEmpty()) listOf(toMsg(this, evaluator))
                    else listOf()
                }
        }

    private fun root(command: CommandCall) = command.element

    private fun toMsg(msg: Element, evaluator: Evaluator) = with(MessageAttrs(Html(msg), evaluator)) {
        ParametrizedTypedMessage(
            from.contentType,
            from.content,
            headers.mapValues { evaluator.resolveNoType(it.value) },
            params.mapValues { evaluator.resolveNoType(it.value) }
        )
    }
}
