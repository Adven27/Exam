package io.github.adven27.concordion.extensions.exam.mq.commands

import io.github.adven27.concordion.extensions.exam.core.commands.ActualProvider
import io.github.adven27.concordion.extensions.exam.core.commands.BeforeParseExamCommand
import io.github.adven27.concordion.extensions.exam.core.commands.CommandParser
import io.github.adven27.concordion.extensions.exam.core.commands.ExamAssertCommand
import io.github.adven27.concordion.extensions.exam.core.commands.FirsSuitableCommandParser
import io.github.adven27.concordion.extensions.exam.core.commands.NamedExamCommand
import io.github.adven27.concordion.extensions.exam.core.commands.VerifyListener
import io.github.adven27.concordion.extensions.exam.mq.FirsSuitableResultRenderer
import io.github.adven27.concordion.extensions.exam.mq.HtmlCheckParser
import io.github.adven27.concordion.extensions.exam.mq.HtmlResultRenderer
import io.github.adven27.concordion.extensions.exam.mq.MdCheckParser
import io.github.adven27.concordion.extensions.exam.mq.MdResultRenderer
import io.github.adven27.concordion.extensions.exam.mq.MdSendParser
import io.github.adven27.concordion.extensions.exam.mq.MqActualProvider
import io.github.adven27.concordion.extensions.exam.mq.MqCheckCommand.TypedMessage
import io.github.adven27.concordion.extensions.exam.mq.MqTester
import io.github.adven27.concordion.extensions.exam.mq.MqTester.Message
import io.github.adven27.concordion.extensions.exam.mq.MqVerifier
import io.github.adven27.concordion.extensions.exam.mq.commands.CheckCommand.Actual
import io.github.adven27.concordion.extensions.exam.mq.commands.CheckCommand.Expected
import org.awaitility.core.ConditionFactory
import org.concordion.api.AbstractCommand
import org.concordion.api.CommandCall
import org.concordion.api.Evaluator
import org.concordion.api.Fixture
import org.concordion.api.ResultRecorder
import org.concordion.internal.util.Check

class CheckCommand(
    override val name: String,
    mqTesters: Map<String, MqTester>,
    private val verifier: MqVerifier = MqVerifier(),
    private val actualProvider: ActualProvider<String, Pair<Boolean, Actual>> = MqActualProvider(mqTesters),
    private val commandParser: CommandParser<Expected> = FirsSuitableCommandParser(MdCheckParser(), HtmlCheckParser()),
    resultRenderer: VerifyListener<Expected, Actual> = FirsSuitableResultRenderer(
        MdResultRenderer(),
        HtmlResultRenderer()
    ),
) : ExamAssertCommand<Expected, Actual>(resultRenderer), NamedExamCommand, BeforeParseExamCommand {

    override fun verify(cmd: CommandCall, eval: Evaluator, resultRecorder: ResultRecorder, fixture: Fixture) {
        Check.isFalse(cmd.hasChildCommands(), "Nesting commands inside an '$name' is not supported")

        val expected = commandParser.parse(cmd, eval)

        verifier.verify(expected, { actualProvider.provide(expected.queue) }, expected.await)
            .onSuccess { success(resultRecorder, cmd.element, it.actual, it.expected) }
            .onFailure { failure(resultRecorder, cmd.element, expected, it) }
    }

    data class Expected(
        val queue: String,
        val messages: List<TypedMessage> = listOf(),
        val exact: Boolean = true,
        val await: ConditionFactory,
    ) {
        override fun toString() =
            "Expected '$queue' has messages $messages in ${if (exact) "exact" else "any"} order. Await $await"
    }

    data class Actual(val messages: List<Message> = listOf())

    override val tag: String = "div"
}

class SendCommand(
    override val name: String,
    private val mqTesters: Map<String, MqTester>,
    private val commandParser: CommandParser<Send> = FirsSuitableCommandParser(MdSendParser()),
) : AbstractCommand(), NamedExamCommand {

    override fun setUp(cmd: CommandCall, eval: Evaluator, resultRecorder: ResultRecorder, fixture: Fixture) {
        Check.isFalse(cmd.hasChildCommands(), "Nesting commands inside an '$name' is not supported")

        val send = commandParser.parse(cmd, eval)
        send.messages.forEach {
            mqTesters[send.queue]!!.send(it, it.params)
        }
    }

    data class Send(
        val queue: String,
        val messages: List<ParametrizedTypedMessage> = listOf(),
    )

    open class ParametrizedTypedMessage(
        type: String,
        body: String = "",
        headers: Map<String, String> = emptyMap(),
        val params: Map<String, String> = emptyMap()
    ) : TypedMessage(type, body, headers)
}
