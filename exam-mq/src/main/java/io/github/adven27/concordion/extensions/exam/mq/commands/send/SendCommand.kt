package io.github.adven27.concordion.extensions.exam.mq.commands.send

import io.github.adven27.concordion.extensions.exam.core.commands.CommandParser
import io.github.adven27.concordion.extensions.exam.core.commands.ExamSetUpCommand
import io.github.adven27.concordion.extensions.exam.core.commands.FirsSuitableCommandParser
import io.github.adven27.concordion.extensions.exam.core.commands.NamedExamCommand
import io.github.adven27.concordion.extensions.exam.mq.MqCheckCommand
import io.github.adven27.concordion.extensions.exam.mq.MqTester
import io.github.adven27.concordion.extensions.exam.mq.commands.HtmlQueueParser
import io.github.adven27.concordion.extensions.exam.mq.commands.MdQueueParser
import io.github.adven27.concordion.extensions.exam.mq.commands.send.SendCommand.Send
import org.concordion.api.CommandCall
import org.concordion.api.Evaluator
import org.concordion.api.Fixture
import org.concordion.api.ResultRecorder

class SendCommand(
    override val name: String,
    private val mqTesters: Map<String, MqTester>,
    private val commandParser: CommandParser<Send> = FirsSuitableCommandParser(
        SendParser(MdQueueParser()),
        SendParser(HtmlQueueParser())
    ),
) : ExamSetUpCommand<Send>(SendRenderer()), NamedExamCommand {

    override fun setUp(cmd: CommandCall, eval: Evaluator, resultRecorder: ResultRecorder, fixture: Fixture) {
        commandParser.parse(cmd, eval).apply {
            messages.forEach { mqTesters[queue]!!.send(it, it.params) }
            setUpCompleted(cmd.element, this)
        }
    }

    data class Send(val queue: String, val messages: List<MqCheckCommand.ParametrizedTypedMessage> = listOf())
}
