package io.github.adven27.concordion.extensions.exam.mq.commands.check

import io.github.adven27.concordion.extensions.exam.core.commands.SuitableCommandParser
import io.github.adven27.concordion.extensions.exam.core.commands.awaitConfig
import io.github.adven27.concordion.extensions.exam.core.html.html
import io.github.adven27.concordion.extensions.exam.mq.commands.QueueParser
import org.concordion.api.CommandCall
import org.concordion.api.Element
import org.concordion.api.Evaluator

class CheckParser(private val parser: QueueParser) : SuitableCommandParser<CheckCommand.Expected> {
    override fun isSuitFor(element: Element): Boolean = parser.isSuitFor(element)
    fun messages(command: CommandCall, eval: Evaluator) = parser.parse(command, eval)

    override fun parse(command: CommandCall, evaluator: Evaluator) = with(Attrs(command)) {
        CheckCommand.Expected(
            queue = mqName,
            messages = messages(command, evaluator),
            exact = contains.lowercase() == "exact",
            await = awaitConfig
        )
    }

    class Attrs(call: CommandCall) {
        val mqName: String = call.html().attr(NAME) ?: call.expression
        val vertically = call.html().takeAwayAttr("layout", "VERTICALLY").uppercase() == "VERTICALLY"
        val contains = call.html().takeAwayAttr("contains", "EXACT")
        val collapsable = call.html().takeAwayAttr("collapsable", "false").toBoolean()
        val awaitConfig = call.awaitConfig()

        companion object {
            private const val NAME = "name"
        }
    }
}
