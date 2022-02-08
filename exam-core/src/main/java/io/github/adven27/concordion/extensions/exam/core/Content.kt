package io.github.adven27.concordion.extensions.exam.core

import io.github.adven27.concordion.extensions.exam.core.html.Html
import io.github.adven27.concordion.extensions.exam.core.utils.JsonPrettyPrinter
import io.github.adven27.concordion.extensions.exam.core.utils.PlaceholderSupportDiffEvaluator
import mu.KLogging
import net.javacrumbs.jsonunit.JsonAssert
import net.javacrumbs.jsonunit.core.Configuration
import net.javacrumbs.jsonunit.core.internal.JsonUtils
import nu.xom.Builder
import nu.xom.Document
import nu.xom.Serializer
import org.concordion.api.Evaluator
import org.hamcrest.MatcherAssert
import org.hamcrest.Matchers
import org.xmlunit.builder.DiffBuilder
import org.xmlunit.diff.Diff
import org.xmlunit.diff.DifferenceEvaluators
import org.xmlunit.diff.NodeMatcher
import java.io.ByteArrayOutputStream
import java.io.FileNotFoundException
import java.io.StringReader

open class ContentTypeConfig(
    val resolver: ContentResolver,
    val verifier: ContentVerifier,
    val printer: ContentPrinter
) {
    operator fun component1() = resolver
    operator fun component2() = verifier
    operator fun component3() = printer
}

open class JsonContentTypeConfig @JvmOverloads constructor(
    resolver: ContentResolver = JsonResolver(),
    verifier: ContentVerifier = JsonVerifier(),
    printer: ContentPrinter = JsonPrinter(),
) : ContentTypeConfig(resolver, verifier, printer)

open class XmlContentTypeConfig @JvmOverloads constructor(
    resolver: ContentResolver = XmlResolver(),
    verifier: ContentVerifier = XmlVerifier(),
    printer: ContentPrinter = XmlPrinter(),
) : ContentTypeConfig(resolver, verifier, printer)

open class TextContentTypeConfig @JvmOverloads constructor(
    resolver: ContentResolver = JsonResolver(),
    verifier: ContentVerifier = ContentVerifier.Default("text"),
    printer: ContentPrinter = ContentPrinter.AsIs(),
) : ContentTypeConfig(resolver, verifier, printer)

interface ContentResolver {
    fun resolve(content: String, evaluator: Evaluator): String
}

open class JsonResolver : ContentResolver {
    override fun resolve(content: String, evaluator: Evaluator): String = evaluator.resolveJson(content)
}

open class XmlResolver : ContentResolver {
    override fun resolve(content: String, evaluator: Evaluator): String = evaluator.resolveXml(content)
}

interface ContentPrinter {
    open class AsIs : ContentPrinter {
        override fun print(content: String): String = content
        override fun style(): String = "text"
    }

    fun print(content: String): String
    fun style(): String
}

open class JsonPrinter : ContentPrinter {
    override fun print(content: String): String = content.prettyJson()
    override fun style(): String = "json"
}

open class XmlPrinter : ContentPrinter {
    override fun print(content: String): String = content.prettyXml()
    override fun style(): String = "xml"
}

fun String.prettyXml(): String = Builder().build(StringReader(this.trim())).prettyXml().let { removeXmlTag(it) }
private fun removeXmlTag(it: String) = it.substring(it.indexOf('\n') + 1)

fun String.prettyJson() = JsonPrettyPrinter().prettyPrint(this)
fun String.pretty(type: String) = when (type) {
    "json" -> prettyJson()
    "xml" -> prettyXml()
    else -> this
}

fun Document.prettyXml(): String {
    try {
        val out = ByteArrayOutputStream()
        val serializer = Serializer(out, "UTF-8")
        serializer.indent = 2
        serializer.write(this)
        return out.toString("UTF-8").trimEnd()
    } catch (expected: Exception) {
        throw InvalidXml(expected)
    }
}

class InvalidXml(t: Throwable) : RuntimeException(t)

interface ContentVerifier {
    fun verify(expected: String, actual: String): Result<ExpectedContent>

    open class Default(val type: String) : ContentVerifier {
        override fun verify(expected: String, actual: String) = try {
            when {
                actual.isEmpty() ->
                    if (expected.isEmpty()) Result.success(ExpectedContent(type, expected))
                    else Result.failure(Fail("Actual is empty", expected, actual))
                else -> {
                    assertThat(expected, actual)
                    Result.success(ExpectedContent(type, expected))
                }
            }
        } catch (e: AssertionError) {
            logger.warn("Content verification error", e)
            Result.failure(Fail(e.message ?: "$e", expected, actual))
        }

        protected open fun assertThat(expected: String, actual: String) =
            MatcherAssert.assertThat(actual, Matchers.equalTo(expected))
    }

    data class ExpectedContent(val type: String, val content: String) {
        fun pretty() = content.pretty(type)
    }

    data class Fail(val details: String, val expected: String, val actual: String, val type: String = "text") :
        java.lang.AssertionError()

    class Exception(actual: String, expected: String, throwable: Throwable) :
        RuntimeException("Failed to verify content:\n$actual\nExpected:\n$expected", throwable)

    companion object : KLogging()
}

open class XmlVerifier(private val nodeMatcher: NodeMatcher) : ContentVerifier.Default("xml") {

    @JvmOverloads
    constructor(configureNodeMatcher: (NodeMatcher) -> NodeMatcher = { it }) :
        this(configureNodeMatcher(ExamExtension.DEFAULT_NODE_MATCHER))

    override fun assertThat(expected: String, actual: String) = diff(expected, actual).let {
        if (it.hasDifferences()) throw AssertionError(it.toString())
    }

    protected fun diff(expected: String, actual: String): Diff = DiffBuilder.compare(expected.trim())
        .checkForSimilar().withNodeMatcher(nodeMatcher)
        .withTest(actual.trim())
        .withDifferenceEvaluator(
            DifferenceEvaluators.chain(
                DifferenceEvaluators.Default,
                PlaceholderSupportDiffEvaluator(ExamExtension.MATCHERS)
            )
        )
        .ignoreComments().ignoreWhitespace().build()
}

@Suppress("TooGenericExceptionCaught")
open class JsonVerifier(private val configuration: Configuration) : ContentVerifier.Default("json") {

    @JvmOverloads
    constructor(configure: (Configuration) -> Configuration = { it }) : this(configure(ExamExtension.DEFAULT_JSON_UNIT_CFG))

    override fun assertThat(expected: String, actual: String) {
        validate(actual)
        try {
            // set tolerance because of https://github.com/lukas-krecan/JsonUnit/issues/468
            JsonAssert.assertJsonEquals(expected, actual, configuration.withTolerance(0.0))
        } catch (ae: AssertionError) {
            throw ae
        } catch (e: Exception) {
            throw ContentVerifier.Exception(actual, expected, e)
        }
    }

    protected fun validate(actual: String) {
        try {
            JsonUtils.convertToJson(actual, "actual", false)
        } catch (expected: RuntimeException) {
            throw AssertionError(
                "Can not convert actual to json: ${expected.cause?.message ?: expected.message}",
                expected
            )
        }
    }
}

fun Html.content(eval: Evaluator? = null) = content(this.attr("from"), eval)
fun Html.content(from: String?, eval: Evaluator? = null) =
    (from?.findResource(eval)?.readText() ?: this.text()).trimIndent()

fun String.findResource(eval: Evaluator? = null) =
    ExamExtension::class.java.getResource(eval?.resolveJson(this) ?: this)
        ?: throw FileNotFoundException("File not found: $this")

fun String.readFile(eval: Evaluator? = null) = findResource(eval).readText().trimIndent()
