@file:Suppress("TooManyFunctions")

package com.adven.concordion.extensions.exam.core

import com.adven.concordion.extensions.exam.core.html.Html
import com.adven.concordion.extensions.exam.core.html.pre
import com.adven.concordion.extensions.exam.core.utils.DateFormatMatcher
import com.adven.concordion.extensions.exam.core.utils.DateWithin
import com.adven.concordion.extensions.exam.core.utils.HANDLEBARS
import com.adven.concordion.extensions.exam.core.utils.XMLDateWithin
import com.adven.concordion.extensions.exam.core.utils.equalToXml
import com.github.jknack.handlebars.Handlebars
import mu.KotlinLogging
import net.javacrumbs.jsonunit.JsonAssert
import net.javacrumbs.jsonunit.JsonAssert.`when`
import net.javacrumbs.jsonunit.core.Configuration
import net.javacrumbs.jsonunit.core.Option.IGNORING_ARRAY_ORDER
import nu.xom.Builder
import org.concordion.api.Element
import org.concordion.api.Evaluator
import org.concordion.api.extension.ConcordionExtender
import org.concordion.api.extension.ConcordionExtension
import org.concordion.api.listener.ExampleEvent
import org.hamcrest.Matcher
import org.junit.Assert
import org.xmlunit.diff.DefaultNodeMatcher
import org.xmlunit.diff.ElementSelectors.byName
import org.xmlunit.diff.ElementSelectors.byNameAndText
import org.xmlunit.diff.NodeMatcher
import java.io.StringReader
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.Period
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.time.format.ResolverStyle.SMART
import java.util.ArrayList
import java.util.Collections.addAll
import java.util.Date

class ExamExtension : ConcordionExtension {
    private var focusOnError: Boolean = true
    private var jsonUnitCfg: Configuration = DEFAULT_JSON_UNIT_CFG
    private var nodeMatcher: NodeMatcher = DEFAULT_NODE_MATCHER
    private var skipDecider: SkipDecider = SkipDecider.NoSkip()
    private val plugins = ArrayList<ExamPlugin>()

    /**
     * Attach xmlunit/jsonunit matchers.
     *
     * @param matcherName name to reference in placeholder.
     * @param matcher     implementation.
     * <br></br>usage:<br></br>
     * !{matcherName param1 param2}
     */
    @Suppress("unused")
    fun addPlaceholderMatcher(matcherName: String, matcher: Matcher<*>): ExamExtension {
        jsonUnitCfg = jsonUnitCfg.withMatcher(matcherName, matcher)
        return this
    }

    @Suppress("unused")
    fun withXmlUnitNodeMatcher(nodeMatcher: NodeMatcher): ExamExtension {
        this.nodeMatcher = nodeMatcher
        return this
    }

    @Suppress("unused")
    fun withHandlebar(fn: (handlebars: Handlebars) -> Unit): ExamExtension {
        fn(HANDLEBARS)
        return this
    }

    @Suppress("unused")
    fun addPlugin(plugin: ExamPlugin): ExamExtension {
        plugins.add(plugin)
        return this
    }

    @Suppress("unused")
    fun withPlugins(vararg plugins: ExamPlugin): ExamExtension {
        addAll(this.plugins, *plugins)
        return this
    }

    /**
     * All examples but failed will be collapsed
     */
    @Suppress("unused")
    fun withFocusOnFailed(enabled: Boolean): ExamExtension {
        focusOnError = enabled
        return this
    }

    @Suppress("unused")
    fun runOnlyExamplesWithPathsContains(vararg substrings: String): ExamExtension {
        skipDecider = object : SkipDecider {
            var reason = ""
            override fun test(event: ExampleEvent): Boolean {
                val skips = mutableListOf<String>()
                val name = event.resultSummary.specificationDescription
                val noSkip = substrings.none { substring ->
                    if (name.contains(substring)) {
                        true
                    } else {
                        skips += substring
                        false
                    }
                }
                if (noSkip) {
                    reason = "specification name: \"$name\" not contains: $skips \n"
                }
                return noSkip
            }

            override fun reason(): String = reason
        }
        return this
    }

    @Suppress("unused")
    fun withSkipExampleDecider(decider: SkipDecider): ExamExtension {
        skipDecider = decider
        return this
    }

    override fun addTo(ex: ConcordionExtender) {
        val registry = CommandRegistry(jsonUnitCfg, nodeMatcher)
        plugins.forEach { registry.register(it.commands()) }

        registry.commands()
            .filter { "example" != it.name() }
            .forEach { ex.withCommand(NS, it.name(), it) }

        CodeMirrorExtension().addTo(ex)
        BootstrapExtension().addTo(ex)
        ex.withDocumentParsingListener(ExamDocumentParsingListener(registry))
        ex.withSpecificationProcessingListener(SpecSummaryListener())
        ex.withThrowableListener {
            Html(it.element).below(
                pre(
                    "Error while executing command:\n\n" +
                        "${PARSED_COMMANDS[it.element.getAttributeValue("cmdId")]}\n\n" +
                        "${it.throwable.cause?.message ?: it.throwable.message}"
                ).css("alert alert-warning small")
            )
        }
        if (focusOnError) {
            ex.withSpecificationProcessingListener(FocusOnErrorsListener())
        }
        ex.withExampleListener(ExamExampleListener(skipDecider))
    }

    companion object {
        val PARSED_COMMANDS: MutableMap<String, String> = HashMap()
        const val NS = "http://exam.extension.io"
        val DEFAULT_NODE_MATCHER = DefaultNodeMatcher(byNameAndText, byName)
        val DEFAULT_JSON_UNIT_CFG: Configuration = `when`(IGNORING_ARRAY_ORDER)
            .withMatcher("formattedAs", DateFormatMatcher())
            .withMatcher("formattedAndWithin", DateWithin.param())
            .withMatcher("formattedAndWithinNow", DateWithin.now())
            .withMatcher("xmlDateWithinNow", XMLDateWithin())
    }
}

fun String.toHtml() = parseTemplate(this)
fun parseTemplate(tmpl: String) = Html(Element(Builder().build(StringReader(tmpl)).rootElement).deepClone())

private val DEFAULT_ZONED_DATETIME_FORMAT = DateTimeFormatter.ISO_OFFSET_DATE_TIME.withResolverStyle(SMART)
private val DEFAULT_LOCAL_DATETIME_FORMAT = DateTimeFormatter.ISO_DATE_TIME.withResolverStyle(SMART)
private val DEFAULT_LOCAL_DATE_FORMAT = DateTimeFormatter.ISO_DATE.withResolverStyle(SMART)

fun ZonedDateTime.toString(pattern: String): String = this.format(DateTimeFormatter.ofPattern(pattern))
fun Date.toString(pattern: String): String =
    pattern.toDatePattern().withZone(ZoneId.systemDefault()).format(this.toInstant())

fun ZonedDateTime.toDate(): Date = Date.from(this.toInstant())
fun LocalDateTime.toDate(zoneId: ZoneId = ZoneId.systemDefault()): Date = Date.from(this.atZone(zoneId).toInstant())
fun LocalDate.toDate(zoneId: ZoneId = ZoneId.systemDefault()): Date = Date.from(this.atStartOfDay(zoneId).toInstant())
fun Date.toZonedDateTime(): ZonedDateTime = ZonedDateTime.from(this.toInstant().atZone(ZoneId.systemDefault()))
fun Date.toLocalDateTime(zoneId: ZoneId = ZoneId.systemDefault()): LocalDateTime =
    this.toInstant().atZone(zoneId).toLocalDateTime()

fun Date.toLocalDate(zoneId: ZoneId = ZoneId.systemDefault()): LocalDate =
    this.toInstant().atZone(zoneId).toLocalDate()

fun Date.plus(period: Pair<Period, Duration>): LocalDateTime =
    this.toLocalDateTime().plus(period.first).plus(period.second)

fun Date.minus(period: Pair<Period, Duration>): LocalDateTime =
    this.toLocalDateTime().minus(period.first).minus(period.second)

fun LocalDateTime.plus(period: Pair<Period, Duration>): LocalDateTime = this.plus(period.first).plus(period.second)
fun LocalDateTime.minus(period: Pair<Period, Duration>): LocalDateTime = this.minus(period.first).minus(period.second)

fun String.parseDate(format: String? = null) = try {
    this.parseDateTime(format).toDate()
} catch (e: DateTimeParseException) {
    logger.debug("Failed to parse ZonedDateTime from $this with pattern '${format ?: DEFAULT_ZONED_DATETIME_FORMAT}'. Try to parse as LocalDateTime.")
    try {
        this.parseLocalDateTime(format).toDate()
    } catch (e: DateTimeParseException) {
        logger.debug("Failed to parse LocalDateTime from $this with pattern '${format ?: DEFAULT_LOCAL_DATETIME_FORMAT}'. Try to parse as LocalDate.")
        this.parseLocalDate(format).toDate()
    }
}

fun String.parseDateTime(format: String? = null): ZonedDateTime = ZonedDateTime.parse(
    this,
    format?.toDatePattern() ?: DEFAULT_ZONED_DATETIME_FORMAT
)

fun String.parseLocalDateTime(format: String? = null): LocalDateTime = LocalDateTime.parse(
    this,
    format?.toDatePattern() ?: DEFAULT_LOCAL_DATETIME_FORMAT
)

fun String.parseLocalDate(format: String? = null): LocalDate = LocalDate.parse(
    this,
    format?.toDatePattern() ?: DEFAULT_LOCAL_DATE_FORMAT
)

fun String.toDatePattern(): DateTimeFormatter = DateTimeFormatter.ofPattern(this)

fun String.fileExt() = substring(lastIndexOf('.') + 1).toLowerCase()

fun String.toMap(): Map<String, String> = unboxIfNeeded(this)
    .split(",")
    .map {
        val (n, v) = it.split("=")
        Pair(n.trim(), v.trim())
    }.toMap()

fun Map<String, String>.resolveValues(eval: Evaluator) = this.mapValues { eval.resolveNoType(it.value) }

private fun unboxIfNeeded(it: String) = if (it.trim().startsWith("{")) it.substring(1, it.lastIndex) else it

interface ContentVerifier {
    fun verify(expected: String, actual: String, config: Array<Any?>)

    class Json : ContentVerifier {
        override fun verify(expected: String, actual: String, config: Array<Any?>) {
            JsonAssert.assertJsonEquals(expected, actual, config[0] as Configuration)
        }
    }

    class Xml : ContentVerifier {
        override fun verify(expected: String, actual: String, config: Array<Any?>) {
            actual.equalToXml(expected, config[0] as Configuration, config[1] as NodeMatcher)
        }
    }

    class Default : ContentVerifier {
        override fun verify(expected: String, actual: String, config: Array<Any?>) {
            Assert.assertEquals("Default content verifier error.", expected, actual)
        }
    }
}

private val logger = KotlinLogging.logger {}
