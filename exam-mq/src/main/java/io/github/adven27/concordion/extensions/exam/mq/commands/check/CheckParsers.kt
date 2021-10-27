package io.github.adven27.concordion.extensions.exam.mq.commands.check

import io.github.adven27.concordion.extensions.exam.core.commands.SuitableCommandParser
import io.github.adven27.concordion.extensions.exam.mq.MqCheckCommand
import io.github.adven27.concordion.extensions.exam.mq.commands.QueueParser
import org.concordion.api.CommandCall
import org.concordion.api.Element
import org.concordion.api.Evaluator

class CheckParser(private val parser: QueueParser) : SuitableCommandParser<CheckCommand.Expected> {
    override fun isSuitFor(element: Element): Boolean = parser.isSuitFor(element)
    fun messages(command: CommandCall, eval: Evaluator) = parser.parse(command, eval)

    override fun parse(command: CommandCall, evaluator: Evaluator) = with(MqCheckCommand.Attrs(command)) {
        CheckCommand.Expected(
            queue = mqName,
            messages = messages(command, evaluator),
            exact = contains.lowercase() == "exact",
            await = awaitConfig
        )
    }
}
