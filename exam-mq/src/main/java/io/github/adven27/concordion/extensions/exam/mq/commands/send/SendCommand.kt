package io.github.adven27.concordion.extensions.exam.mq.commands.send

import io.github.adven27.concordion.extensions.exam.core.commands.BeforeParseExamCommand
import io.github.adven27.concordion.extensions.exam.core.commands.CommandParser
import io.github.adven27.concordion.extensions.exam.core.commands.ExamSetUpCommand
import io.github.adven27.concordion.extensions.exam.core.commands.FirstSuitableCommandParser
import io.github.adven27.concordion.extensions.exam.core.commands.FirstSuitableSetUpListener
import io.github.adven27.concordion.extensions.exam.core.commands.NamedExamCommand
import io.github.adven27.concordion.extensions.exam.core.commands.SetUpListener
import io.github.adven27.concordion.extensions.exam.mq.MqTester
import io.github.adven27.concordion.extensions.exam.mq.ParametrizedTypedMessage
import io.github.adven27.concordion.extensions.exam.mq.commands.HtmlQueueParser
import io.github.adven27.concordion.extensions.exam.mq.commands.MdQueueParser
import io.github.adven27.concordion.extensions.exam.mq.commands.send.SendCommand.Send
import org.concordion.api.Evaluator

class SendCommand(
    override val name: String,
    private val mqTesters: Map<String, MqTester>,
    commandParser: CommandParser<Send> = FirstSuitableCommandParser(
        SendParser(MdQueueParser()),
        SendParser(HtmlQueueParser())
    ),
    listener: SetUpListener<Send> = FirstSuitableSetUpListener(MdSendRenderer(), HtmlSendRenderer())
) : ExamSetUpCommand<Send>(commandParser, listener), NamedExamCommand, BeforeParseExamCommand {

    override val tag: String = "div"

    override fun setUp(target: Send, eval: Evaluator) {
        target.messages.forEach { mqTesters[target.queue]!!.send(it, it.params) }
    }

    data class Send(val queue: String, val messages: List<ParametrizedTypedMessage> = listOf())
}
