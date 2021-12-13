package io.github.adven27.concordion.extensions.exam.mq.commands.send

import io.github.adven27.concordion.extensions.exam.core.commands.SuitableCommandParser
import io.github.adven27.concordion.extensions.exam.core.html.takeAttr
import io.github.adven27.concordion.extensions.exam.mq.commands.QueueParser
import io.github.adven27.concordion.extensions.exam.mq.commands.send.SendCommand.Send
import org.concordion.api.CommandCall
import org.concordion.api.Element
import org.concordion.api.Evaluator

class SendParser(private val parser: QueueParser) : SuitableCommandParser<Send> {
    override fun isSuitFor(element: Element): Boolean = parser.isSuitFor(element)

    override fun parse(command: CommandCall, evaluator: Evaluator) =
        Send(queue = command.takeAttr("name") ?: command.expression, messages = parser.parse(command, evaluator))
}
