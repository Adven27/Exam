package io.github.adven27.concordion.extensions.exam.mq

import io.github.adven27.concordion.extensions.exam.core.ContentVerifier
import io.github.adven27.concordion.extensions.exam.core.ExamExtension
import io.github.adven27.concordion.extensions.exam.core.commands.AwaitConfig
import io.github.adven27.concordion.extensions.exam.core.commands.ExamAssertCommand
import io.github.adven27.concordion.extensions.exam.core.commands.NamedExamCommand
import io.github.adven27.concordion.extensions.exam.core.commands.VerifyFailureEvent
import io.github.adven27.concordion.extensions.exam.core.commands.VerifyListener
import io.github.adven27.concordion.extensions.exam.core.commands.VerifySuccessEvent
import io.github.adven27.concordion.extensions.exam.core.commands.await
import io.github.adven27.concordion.extensions.exam.mq.MqCheckCommand.TypedMessage
import io.github.adven27.concordion.extensions.exam.mq.MqCheckCommand.VerifyPair
import io.github.adven27.concordion.extensions.exam.mq.MqTester.Message
import io.github.adven27.concordion.extensions.exam.mq.MqVerifier.MessageVerifyResult
import org.awaitility.core.ConditionFactory
import org.concordion.api.CommandCall
import org.concordion.api.Element
import org.concordion.api.Evaluator
import org.concordion.api.Fixture
import org.concordion.api.ResultRecorder
import org.concordion.internal.util.Check
import org.junit.Assert

class MqResultRenderer : VerifyListener {
    override fun successReported(event: VerifySuccessEvent) {
        // TODO("Not yet implemented")
    }

    override fun failureReported(event: VerifyFailureEvent) {
        // TODO("Not yet implemented")
    }
}

interface Verifier<E, A, R> {
    fun verify(expected: E, actual: A): Result<R>
}

interface AwaitVerifier<E, A, R> : Verifier<E, A, R> {
    fun verify(expected: E, getActual: () -> A, await: ConditionFactory): Result<R>
}

class MqVerifier(var exactMatch: Boolean = true) :
    AwaitVerifier<List<TypedMessage>, List<Message>, List<MessageVerifyResult>> {

    fun withExactMatch(exact: Boolean) = this.apply { exactMatch = exact }

    override fun verify(
        expected: List<TypedMessage>,
        getActual: () -> List<Message>,
        await: ConditionFactory
    ): Result<List<MessageVerifyResult>> {
        val actual: List<Message>
        try {
            actual = awaitExpectedSize(expected, emptyList(), getActual, await)
        } catch (e: java.lang.AssertionError) {
            return Result.failure(e)
        }

        return expected.sortedTyped()
            .zip(actual.sorted()) { e, a -> VerifyPair(a, e) }
            .map {
                MqCheckCommand.logger.info("Verifying {}", it)
                val typeConfig = ExamExtension.contentTypeConfig(it.expected.type)
                MessageVerifyResult(
                    checkHeaders(
                        it.actual.headers,
                        it.expected.headers,
                    ),
                    typeConfig.let { (_, verifier, _) ->
                        verifier.verify(it.expected.body, it.actual.body)
                    }
                )
            }.let { results ->
                if (results.any { it.unmatchedHeaders.isNotEmpty() || it.content.fail.isPresent })
                    Result.success(results)
                else
                    Result.failure(MessageVerifyingError(results))
            }
    }

    @Suppress("SpreadOperator")
    private fun checkHeaders(
        actual: Map<String, String>,
        expected: Map<String, String>,
    ): List<Pair<Map.Entry<String, String>, Map.Entry<String, String>>> =
        if (expected.isEmpty())
            emptyList()
        else try {
            Assert.assertEquals("Different headers size", expected.size, actual.size)
            expected.entries.partition { actual[it.key] != null }.let { (m, u) ->
                u.zip(unmatchedActual(actual, m))
            }
        } catch (e: AssertionError) {
            throw HeadersSizeVerifyingError(expected, actual, e.message!!, e)
        }

    private fun unmatchedActual(actual: Map<String, String>, matched: List<Map.Entry<String, String>>) =
        actual.entries.filterNot { matched.hasKey(it) }

    private fun List<Map.Entry<String, String>>.hasKey(it: Map.Entry<String, String>) = map { it.key }.contains(it.key)

    private fun List<Message>.sorted() = if (!exactMatch) sortedBy { it.body } else this
    private fun List<TypedMessage>.sortedTyped() = if (!exactMatch) sortedBy { it.body } else this

    @Suppress("TooGenericExceptionCaught")
    private fun awaitExpectedSize(
        expected: List<TypedMessage>,
        originalActual: List<Message>,
        receive: () -> List<Message>,
        await: ConditionFactory,
    ): List<Message> {
        val actual = originalActual.toMutableList()
        val prevActual: MutableList<Message> = mutableListOf()
        try {
            await.untilAsserted {
                actual.apply { prevActual.apply { clear(); addAll(this) } }.addAll(receive())
                Assert.assertEquals(expected.size, actual.size)
            }
            return actual
        } catch (e: Exception) {
            throw SizeVerifyingError(expected, prevActual, e.cause?.message ?: e.message ?: "$e", e)
        }
    }

    override fun verify(expected: List<TypedMessage>, actual: List<Message>) =
        verify(expected, { actual }, AwaitConfig().await())

    data class MessageVerifyResult(
        val unmatchedHeaders: List<Pair<Map.Entry<String, String>, Map.Entry<String, String>>>,
        val content: ContentVerifier.Result
    )

    class MessageVerifyingError(val expected: List<MessageVerifyResult>) : java.lang.AssertionError()

    class SizeVerifyingError(
        val expected: List<Message>,
        val actual: List<Message>,
        message: String,
        exception: Throwable
    ) : java.lang.AssertionError(message, exception)

    class HeadersSizeVerifyingError(
        val expected: Map<String, String>,
        val actual: Map<String, String>,
        message: String,
        exception: Throwable
    ) : java.lang.AssertionError(message, exception)
}

class CheckCommand(
    override val name: String,
    private val mqTesters: Map<String, MqTester>,
    private val verifier: MqVerifier = MqVerifier(),
    resultRenderer: VerifyListener = MqResultRenderer(),
) : ExamAssertCommand(resultRenderer), NamedExamCommand {

    override fun verify(cmd: CommandCall, eval: Evaluator, resultRecorder: ResultRecorder, fixture: Fixture) {
        Check.isFalse(cmd.hasChildCommands(), "Nesting commands inside an 'mq-check' is not supported")

        val element: Element = cmd.element
        val attrs = MqCheckCommand.Attrs(cmd)
        val queue: String = cmd.expression
        val tester = mqTesters.getOrFail(queue)
        val expected: List<TypedMessage> = listOf()
        val actual: MutableList<Message> = mutableListOf()
        val getActual: () -> MutableList<Message> = {
            actual
                .apply { if (!tester.accumulateOnRetries()) clear() }
                .apply { addAll(tester.receive()) }
        }

        verifier.withExactMatch("EXACT" == attrs.contains)
            .verify(expected, getActual, attrs.awaitConfig.await("Await MQ $queue"))
            .onSuccess { success(resultRecorder, element) }
            .onFailure { failure(resultRecorder, element, actual, expected, it) }
    }
}

private fun Map<String, MqTester>.getOrFail(mqName: String?): MqTester = this[mqName]
    ?: throw IllegalArgumentException("MQ with name $mqName not registered in MqPlugin")
