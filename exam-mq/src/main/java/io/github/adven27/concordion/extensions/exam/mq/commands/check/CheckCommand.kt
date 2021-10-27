package io.github.adven27.concordion.extensions.exam.mq.commands.check

import io.github.adven27.concordion.extensions.exam.core.commands.ActualProvider
import io.github.adven27.concordion.extensions.exam.core.commands.AwaitConfig
import io.github.adven27.concordion.extensions.exam.core.commands.BeforeParseExamCommand
import io.github.adven27.concordion.extensions.exam.core.commands.CommandParser
import io.github.adven27.concordion.extensions.exam.core.commands.ExamAssertCommand
import io.github.adven27.concordion.extensions.exam.core.commands.FirsSuitableCommandParser
import io.github.adven27.concordion.extensions.exam.core.commands.FirsSuitableResultRenderer
import io.github.adven27.concordion.extensions.exam.core.commands.NamedExamCommand
import io.github.adven27.concordion.extensions.exam.core.commands.VerifyListener
import io.github.adven27.concordion.extensions.exam.mq.MqCheckCommand
import io.github.adven27.concordion.extensions.exam.mq.MqTester
import io.github.adven27.concordion.extensions.exam.mq.commands.HtmlQueueParser
import io.github.adven27.concordion.extensions.exam.mq.commands.MdQueueParser
import org.concordion.api.CommandCall
import org.concordion.api.Evaluator
import org.concordion.api.Fixture
import org.concordion.api.ResultRecorder

class CheckCommand(
    override val name: String,
    mqTesters: Map<String, MqTester>,
    private val verifier: MqVerifier = MqVerifier(),
    private val actualProvider: ActualProvider<String, Pair<Boolean, Actual>> = MqActualProvider(mqTesters),
    private val commandParser: CommandParser<Expected> = FirsSuitableCommandParser(
        CheckParser(MdQueueParser()),
        CheckParser(HtmlQueueParser())
    ),
    resultRenderer: VerifyListener<Expected, Actual> = FirsSuitableResultRenderer(
        MdResultRenderer(),
        HtmlResultRenderer()
    ),
) : ExamAssertCommand<CheckCommand.Expected, CheckCommand.Actual>(resultRenderer), NamedExamCommand,
    BeforeParseExamCommand {
    override val tag: String = "div"

    override fun verify(cmd: CommandCall, eval: Evaluator, resultRecorder: ResultRecorder, fixture: Fixture) {
        commandParser.parse(cmd, eval).apply {
            verifier.verify(this) { actualProvider.provide(queue) }
                .onSuccess { success(resultRecorder, cmd.element, it.actual, it.expected) }
                .onFailure { failure(resultRecorder, cmd.element, this, it) }
        }
    }

    data class Actual(val messages: List<MqTester.Message> = listOf())
    data class Expected(
        val queue: String,
        val messages: List<MqCheckCommand.TypedMessage> = listOf(),
        val exact: Boolean = true,
        val await: AwaitConfig?,
    ) {
        override fun toString() =
            "Expected '$queue' has messages $messages in ${if (exact) "exact" else "any"} order. Await $await"
    }
}