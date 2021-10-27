package io.github.adven27.concordion.extensions.exam.mq

import io.github.adven27.concordion.extensions.exam.core.ContentVerifier.ExpectedContent
import io.github.adven27.concordion.extensions.exam.core.ExamExtension
import io.github.adven27.concordion.extensions.exam.core.commands.AwaitVerifier
import io.github.adven27.concordion.extensions.exam.mq.MqCheckCommand.TypedMessage
import io.github.adven27.concordion.extensions.exam.mq.MqTester.Message
import io.github.adven27.concordion.extensions.exam.mq.commands.CheckCommand.Actual
import io.github.adven27.concordion.extensions.exam.mq.commands.CheckCommand.Expected
import mu.KLogging
import org.awaitility.core.ConditionFactory
import org.junit.Assert

class MqVerifier : AwaitVerifier<Expected, Actual, MqVerifier.VerifyingResult> {
    companion object : KLogging()

    override fun verify(
        expected: Expected,
        getActual: () -> Pair<Boolean, Actual>,
        await: ConditionFactory
    ): Result<VerifyingResult> {
        try {
            return awaitSize(expected, getActual, await).let { actual ->
                expected.messages.sortedTyped(expected.exact)
                    .zip(actual.messages.sorted(expected.exact)) { e, a -> MqCheckCommand.VerifyPair(a, e) }
                    .map {
                        logger.info("Verifying {}", it)
                        val typeConfig = ExamExtension.contentTypeConfig(it.expected.type)
                        MessageVerifyResult(
                            checkHeaders(it.actual.headers, it.expected.headers),
                            typeConfig.let { (_, verifier, _) -> verifier.verify(it.expected.body, it.actual.body) }
                        )
                    }.let { results ->
                        if (results.any { it.headers.isFailure || it.content.isFailure })
                            Result.failure(MessageVerifyingError(results))
                        else
                            Result.success(VerifyingResult(expected, actual))
                    }
            }
        } catch (e: java.lang.AssertionError) {
            return Result.failure(e)
        }
    }

    @Suppress("SpreadOperator")
    private fun checkHeaders(actual: Map<String, String>, expected: Map<String, String>): Result<Map<String, String>> =
        if (expected.isEmpty()) Result.success(emptyMap())
        else try {
            Assert.assertEquals("Different headers size", expected.size, actual.size)
            expected.entries.partition { actual[it.key] != null }.let { (matched, absentInActual) ->
                (
                    matched.map { (it.key to it.value) to (it.key to actual[it.key]) } +
                        absentInActual.map { it.toPair() }.zip(absentInExpected(actual, matched))
                    ).map { (expected, actual) ->
                    headerCheckResult(expected, actual)
                }.let { results ->
                    if (results.any { it.actualValue != null || it.actualKey != null })
                        Result.failure(HeadersVerifyingError(results))
                    else
                        Result.success(results.associate { it.header })
                }
            }
        } catch (e: AssertionError) {
            Result.failure(HeadersSizeVerifyingError(expected, actual, e.message!!, e))
        }

    private fun headerCheckResult(expected: Pair<String, String>, actual: Pair<String, String?>) =
        if (expected.first == actual.first)
            if (expected.second == actual.second) HeaderCheckResult(expected)
            else HeaderCheckResult(expected, actualValue = actual.second)
        else HeaderCheckResult(expected, actualKey = actual.first)

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
    private fun awaitSize(expected: Expected, receive: () -> Pair<Boolean, Actual>, await: ConditionFactory): Actual {
        var currentActual = Actual()
        var prevActual = Actual()
        try {
            await.untilAsserted {
                prevActual = currentActual
                currentActual = receive().let { (accumulate, actual) ->
                    if (accumulate) Actual(currentActual.messages + actual.messages) else actual
                }
                Assert.assertEquals(expected.messages.size, currentActual.messages.size)
            }
            return currentActual
        } catch (e: Exception) {
            throw SizeVerifyingError(expected.messages, prevActual.messages, e.cause?.message ?: e.message ?: "$e", e)
        }
    }

    override fun verify(expected: Expected, actual: Actual) = verify(expected, { false to actual }, expected.await)

    data class MessageVerifyResult(val headers: Result<Map<String, String>>, val content: Result<ExpectedContent>)

    data class VerifyingResult(val expected: Expected, val actual: Actual)

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
