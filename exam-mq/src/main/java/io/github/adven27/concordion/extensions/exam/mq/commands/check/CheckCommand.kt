package io.github.adven27.concordion.extensions.exam.mq.commands.check

import io.github.adven27.concordion.extensions.exam.core.commands.ActualProvider
import io.github.adven27.concordion.extensions.exam.core.commands.AwaitConfig
import io.github.adven27.concordion.extensions.exam.core.commands.BeforeParseExamCommand
import io.github.adven27.concordion.extensions.exam.core.commands.CommandParser
import io.github.adven27.concordion.extensions.exam.core.commands.ExamAssertCommand
import io.github.adven27.concordion.extensions.exam.core.commands.FirstSuitableCommandParser
import io.github.adven27.concordion.extensions.exam.core.commands.FirstSuitableResultRenderer
import io.github.adven27.concordion.extensions.exam.core.commands.NamedExamCommand
import io.github.adven27.concordion.extensions.exam.core.commands.VerifyListener
import io.github.adven27.concordion.extensions.exam.mq.MqTester
import io.github.adven27.concordion.extensions.exam.mq.TypedMessage
import io.github.adven27.concordion.extensions.exam.mq.commands.HtmlQueueParser
import io.github.adven27.concordion.extensions.exam.mq.commands.MdQueueParser
import io.github.adven27.concordion.extensions.exam.mq.commands.check.CheckCommand.Actual
import io.github.adven27.concordion.extensions.exam.mq.commands.check.CheckCommand.Expected

@Suppress("LongParameterList")
class CheckCommand(
    override val name: String,
    mqTesters: Map<String, MqTester>,
    verifier: MqVerifier = MqVerifier(),
    actualProvider: ActualProvider<Expected, Pair<Boolean, Actual>> = MqActualProvider(mqTesters),
    commandParser: CommandParser<Expected> = FirstSuitableCommandParser(
        CheckParser(MdQueueParser()),
        CheckParser(HtmlQueueParser())
    ),
    resultRenderer: VerifyListener<Expected, Actual> = FirstSuitableResultRenderer(
        MdResultRenderer(),
        HtmlResultRenderer()
    ),
) : ExamAssertCommand<Expected, Actual>(commandParser, verifier, actualProvider, resultRenderer),
    NamedExamCommand,
    BeforeParseExamCommand {
    override val tag: String = "div"

    data class Actual(val messages: List<MqTester.Message> = listOf())
    data class Expected(
        val queue: String,
        val messages: List<TypedMessage> = listOf(),
        val exact: Boolean = true,
        val await: AwaitConfig?
    ) {
        override fun toString() =
            "Expected '$queue' has messages $messages in ${if (exact) "exact" else "any"} order. Await $await"
    }
}

class MqActualProvider(private val mqTesters: Map<String, MqTester>) : ActualProvider<Expected, Pair<Boolean, Actual>> {
    override fun provide(source: Expected) =
        mqTesters.getOrFail(source.queue).let { it.accumulateOnRetries() to Actual(it.receive()) }

    private fun Map<String, MqTester>.getOrFail(mqName: String?): MqTester = this[mqName]
        ?: throw IllegalArgumentException("MQ with name $mqName not registered in MqPlugin")
}
