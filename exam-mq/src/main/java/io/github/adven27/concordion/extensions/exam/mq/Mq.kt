package io.github.adven27.concordion.extensions.exam.mq

import io.github.adven27.concordion.extensions.exam.core.commands.ActualProvider
import io.github.adven27.concordion.extensions.exam.core.commands.SuitableCommandParser
import io.github.adven27.concordion.extensions.exam.core.commands.await
import io.github.adven27.concordion.extensions.exam.core.html.Html
import io.github.adven27.concordion.extensions.exam.core.readFile
import io.github.adven27.concordion.extensions.exam.mq.MqCheckCommand.MessageAttrs
import io.github.adven27.concordion.extensions.exam.mq.MqCheckCommand.TypedMessage
import io.github.adven27.concordion.extensions.exam.mq.commands.CheckCommand
import io.github.adven27.concordion.extensions.exam.mq.commands.CheckCommand.Actual
import io.github.adven27.concordion.extensions.exam.mq.commands.SendCommand
import io.github.adven27.concordion.extensions.exam.mq.commands.SendCommand.ParametrizedTypedMessage
import org.concordion.api.CommandCall
import org.concordion.api.Element
import org.concordion.api.Evaluator

class MdSendParser : SuitableCommandParser<SendCommand.Send>() {
    override fun isSuitFor(element: Element): Boolean = element.localName != "div"

    override fun parse(command: CommandCall, evaluator: Evaluator) =
        SendCommand.Send(queue = command.expression, messages = messages(command, evaluator))

    private fun messages(command: CommandCall, eval: Evaluator): List<ParametrizedTypedMessage> {
        return command.element.parentElement.parentElement.childElements.filter { it.localName == "dd" }.map { dd ->
            var formatAs = "json"
            val headers: MutableMap<String, String> = mutableMapOf()
            val params: MutableMap<String, String> = mutableMapOf()
            lateinit var payload: String

            dd.childElements.forEach { el ->
                when (el.localName) {
                    "pre" -> payload = el.text
                    "a" -> payload = parsePayload(el, eval)
                    "code" -> parseOption(el).also { (name, value) -> if (name == "formatAs") formatAs = value }
                    "em" -> parseOption(el).also { (name, value) -> headers[name] = value }
                    "strong" -> parseOption(el).also { (name, value) -> params[name] = value }
                    "p" -> el.childElements.forEach { pChild ->
                        when (pChild.localName) {
                            "pre" -> payload = pChild.text
                            "a" -> payload = parsePayload(pChild, eval)
                            "code" -> parseOption(pChild).also { (name, value) ->
                                if (name == "formatAs") formatAs = value
                            }
                            "em" -> parseOption(pChild).also { (name, value) -> headers[name] = value }
                            "strong" -> parseOption(pChild).also { (name, value) -> params[name] = value }
                        }
                    }
                }
            }
            ParametrizedTypedMessage(formatAs, payload, headers, params)
        }
    }
}

class MdCheckParser : SuitableCommandParser<CheckCommand.Expected>() {
    override fun isSuitFor(element: Element): Boolean = element.localName != "div"

    override fun parse(command: CommandCall, evaluator: Evaluator): CheckCommand.Expected {
        val attrs = MqCheckCommand.Attrs(command)
        return CheckCommand.Expected(
            queue = command.expression,
            messages = messagesFromDd(command, evaluator),
            exact = attrs.contains.lowercase() == "exact",
            await = attrs.awaitConfig.await("Await MQ ${command.expression}")
        )
    }

    private fun messagesFromDd(command: CommandCall, eval: Evaluator): List<TypedMessage> =
        command.element.parentElement.parentElement.childElements.filter { it.localName == "dd" }.map { dd ->
            var verifyAs = "json"
            val headers: MutableMap<String, String> = mutableMapOf()
            lateinit var payload: String

            dd.childElements.forEach { el ->
                when (el.localName) {
                    "pre" -> payload = el.text
                    "a" -> payload = parsePayload(el, eval)
                    "code" -> parseOption(el).also { (name, value) -> if (name == "verifyAs") verifyAs = value }
                    "em" -> parseOption(el).also { (name, value) -> headers[name] = value }
                    "p" -> el.childElements.forEach { pChild ->
                        when (pChild.localName) {
                            "pre" -> payload = pChild.text
                            "a" -> payload = parsePayload(pChild, eval)
                            "code" -> parseOption(pChild).also { (name, value) ->
                                if (name == "verifyAs") verifyAs = value
                            }
                            "em" -> parseOption(pChild).also { (name, value) -> headers[name] = value }
                        }
                    }
                }
            }
            TypedMessage(verifyAs, payload, headers)
        }
}

class HtmlCheckParser : SuitableCommandParser<CheckCommand.Expected>() {
    override fun isSuitFor(element: Element): Boolean = element.localName == "div"

    override fun parse(command: CommandCall, evaluator: Evaluator): CheckCommand.Expected {
        val attrs = MqCheckCommand.Attrs(command)
        return CheckCommand.Expected(
            queue = attrs.mqName,
            messages = messages(evaluator, command.element),
            exact = attrs.contains.lowercase() == "exact",
            await = attrs.awaitConfig.await("Await MQ ${command.expression}")
        )
    }

    private fun messages(evaluator: Evaluator, element: Element): List<TypedMessage> =
        element.childElements.filter { it.localName == "message" }
            .map { toMsg(it, evaluator) }
            .ifEmpty {
                if (element.getAttributeValue("from") != null || element.text.isNotEmpty())
                    listOf(toMsg(element, evaluator))
                else
                    listOf()
            }

    private fun toMsg(msg: Element, evaluator: Evaluator) = MessageAttrs(Html(msg), evaluator).let {
        TypedMessage(it.from.contentType, it.from.content, it.headers)
    }
}

class MqActualProvider(private val mqTesters: Map<String, MqTester>) : ActualProvider<String, Pair<Boolean, Actual>> {
    override fun provide(source: String) =
        mqTesters.getOrFail(source).let { it.accumulateOnRetries() to Actual(it.receive()) }

    private fun Map<String, MqTester>.getOrFail(mqName: String?): MqTester = this[mqName]
        ?: throw IllegalArgumentException("MQ with name $mqName not registered in MqPlugin")
}

private fun parsePayload(link: Element, eval: Evaluator): String {
    link.childElements.filter { it.localName == "code" }.forEach {
        parseOption(it).also { (name, value) -> eval.setVariable("#$name", value) }
    }
    return link.getAttributeValue("href").readFile(eval)
}

private fun parseOption(el: Element) = el.text.split("=", limit = 2).let { it[0] to it[1] }
