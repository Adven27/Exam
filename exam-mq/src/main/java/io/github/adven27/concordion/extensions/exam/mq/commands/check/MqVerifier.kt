package io.github.adven27.concordion.extensions.exam.mq.commands.check

import io.github.adven27.concordion.extensions.exam.core.ContentVerifier.ExpectedContent
import io.github.adven27.concordion.extensions.exam.core.ExamExtension
import io.github.adven27.concordion.extensions.exam.core.commands.AwaitVerifier
import io.github.adven27.concordion.extensions.exam.core.commands.Verifier.Success
import io.github.adven27.concordion.extensions.exam.core.commands.checkAndSet
import io.github.adven27.concordion.extensions.exam.core.resolveNoType
import io.github.adven27.concordion.extensions.exam.core.resolveToObj
import io.github.adven27.concordion.extensions.exam.core.rootCauseMessage
import io.github.adven27.concordion.extensions.exam.mq.MqTester.Message
import io.github.adven27.concordion.extensions.exam.mq.TypedMessage
import io.github.adven27.concordion.extensions.exam.mq.VerifyPair
import io.github.adven27.concordion.extensions.exam.mq.commands.check.CheckCommand.Actual
import io.github.adven27.concordion.extensions.exam.mq.commands.check.CheckCommand.Expected
import mu.KLogging
import org.concordion.api.Evaluator
import org.junit.Assert.assertEquals

class MqVerifier : AwaitVerifier<Expected, Actual> {
    companion object : KLogging()

    @Suppress("NestedBlockDepth")
    override fun verify(
        eval: Evaluator,
        expected: Expected,
        getActual: () -> Pair<Boolean, Actual>
    ): Result<Success<Expected, Actual>> {
        try {
            return awaitSize(expected, getActual).let { actual ->
                expected.messages.sortedTyped(expected.exact)
                    .zip(actual.messages.sorted(expected.exact)) { e, a -> VerifyPair(a, e) }
                    .map {
                        logger.info("Verifying {}", it)
                        val typeConfig = ExamExtension.contentTypeConfig(it.expected.type)
                        MessageVerifyResult(
                            checkHeaders(it.actual.headers, it.expected.headers, eval),
                            typeConfig.let { (_, verifier, _) -> verifier.verify(it.expected.body, it.actual.body) }
                        )
                    }.let { results ->
                        if (results.any { it.headers.isFailure || it.content.isFailure }) {
                            Result.failure(MessageVerifyingError(results))
                        } else {
                            Result.success(Success(expected, actual))
                        }
                    }
            }
        } catch (e: java.lang.AssertionError) {
            return Result.failure(e)
        }
    }

    @Suppress("SpreadOperator", "NestedBlockDepth")
    private fun checkHeaders(actual: Map<String, String>, expected: Map<String, String>, eval: Evaluator) =
        if (expected.isEmpty()) Result.success(emptyMap())
        else try {
            assertEquals("Different headers size", expected.size, actual.size)
            expected.entries.partition { actual[it.key] != null }.let { (matched, absentInActual) ->
                (
                    matched.map { (it.key to it.value) to (it.key to actual[it.key]) } +
                        absentInActual.map { it.toPair() }.zip(absentInExpected(actual, matched))
                    ).map { (expected, actual) -> headerCheckResult(expected, actual, eval) }
                    .let { results ->
                        if (results.any { it.actualValue != null || it.actualKey != null }) {
                            Result.failure(HeadersVerifyingError(results))
                        } else {
                            Result.success(results.associate { it.header })
                        }
                    }
            }
        } catch (e: AssertionError) {
            Result.failure(HeadersSizeVerifyingError(expected, actual, e.message!!, e))
        }

    private fun headerCheckResult(expected: Pair<String, String>, actual: Pair<String, String?>, eval: Evaluator) =
        if (expected.first == actual.first) {
            if (checkAndSet(eval, eval.resolveToObj(actual.second), eval.resolveNoType(expected.second))) HeaderCheckResult(expected)
            else HeaderCheckResult(expected, actualValue = actual.second)
        } else HeaderCheckResult(expected, actualKey = actual.first)

    data class HeaderCheckResult(
        val header: Pair<String, String>,
        val actualKey: String? = null,
        val actualValue: String? = null
    )

    private fun absentInExpected(actual: Map<String, String>, matched: List<Map.Entry<String, String>>) =
        actual.entries.filterNot { matched.hasKey(it) }.map { it.toPair() }

    private fun List<Map.Entry<String, String>>.hasKey(it: Map.Entry<String, String>) = map { it.key }.contains(it.key)

    private fun List<Message>.sorted(exactMatch: Boolean) = if (!exactMatch) sortedBy { it.body } else this
    private fun List<TypedMessage>.sortedTyped(exactMatch: Boolean) =
        if (!exactMatch) sortedBy { it.body } else this

    @Suppress("TooGenericExceptionCaught")
    private fun awaitSize(expected: Expected, receive: () -> Pair<Boolean, Actual>): Actual {
        var prevActual = receive().let { (_, actual) -> actual }
        try {
            expected.await?.let {
                var currentActual: Actual? = null
                it.await("Await message queue ${expected.queue}").untilAsserted {
                    if (currentActual != null) {
                        currentActual = receive().let { (accumulate, actual) ->
                            if (accumulate) Actual(currentActual!!.messages + actual.messages) else actual
                        }
                        prevActual = currentActual!!
                    }
                    currentActual = prevActual
                    assertEquals(expected.messages.size, currentActual!!.messages.size)
                }
            } ?: assertEquals(expected.messages.size, prevActual.messages.size)
            return prevActual
        } catch (ignore: Throwable) {
            throw SizeVerifyingError(
                expected.messages,
                prevActual.messages,
                expected.await?.timeoutMessage(ignore) ?: ignore.rootCauseMessage(),
                ignore
            )
        }
    }

    override fun verify(eval: Evaluator, expected: Expected, actual: Actual) =
        verify(eval, expected) { false to actual }

    data class MessageVerifyResult(val headers: Result<Map<String, String>>, val content: Result<ExpectedContent>)

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

    class HeadersVerifyingError(val result: List<HeaderCheckResult>) : java.lang.AssertionError()
}
