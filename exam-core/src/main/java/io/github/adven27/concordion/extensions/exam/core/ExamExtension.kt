@file:Suppress("TooManyFunctions")

package io.github.adven27.concordion.extensions.exam.core

import com.github.jknack.handlebars.Handlebars
import io.github.adven27.concordion.extensions.exam.core.html.Html
import io.github.adven27.concordion.extensions.exam.core.html.codeXmlBlack
import io.github.adven27.concordion.extensions.exam.core.html.span
import io.github.adven27.concordion.extensions.exam.core.utils.DateFormatMatcher
import io.github.adven27.concordion.extensions.exam.core.utils.DateWithin
import io.github.adven27.concordion.extensions.exam.core.utils.HANDLEBARS
import io.github.adven27.concordion.extensions.exam.core.utils.XMLDateWithin
import io.github.adven27.concordion.extensions.exam.core.utils.equalToXml
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
import java.util.Date
import java.util.Random
import java.util.function.Consumer

class ExamExtension constructor(private vararg var plugins: ExamPlugin) : ConcordionExtension {
    private var focusOnError: Boolean = true
    private var jsonUnitCfg: Configuration = DEFAULT_JSON_UNIT_CFG
    private var nodeMatcher: NodeMatcher = DEFAULT_NODE_MATCHER
    private var skipDecider: SkipDecider = SkipDecider.NoSkip()

    /**
     * Attach xmlunit/jsonunit matchers.
     *
     * @param matcherName name to reference in placeholder.
     * @param matcher     implementation.
     * usage: {{matcherName param1 param2}}
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
    fun withHandlebar(fn: Consumer<Handlebars>): ExamExtension {
        fn.accept(HANDLEBARS)
        return this
    }

    @Deprecated(message = "Use the constructor")
    @Suppress("unused")
    fun withPlugins(vararg plugins: ExamPlugin): ExamExtension {
        this.plugins = plugins
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
            Html(it.element).above(
                errorMessage(
                    "Error while executing command",
                    "${it.throwable.cause?.message ?: it.throwable.message}",
                    codeXmlBlack(PARSED_COMMANDS[it.element.getAttributeValue("cmdId")]?.fixIndent())
                )
            )
        }
        if (focusOnError) {
            ex.withSpecificationProcessingListener(FocusOnErrorsListener())
        }
        ex.withExampleListener(ExamExampleListener(skipDecider))
    }

    fun setUp() {
        plugins.forEach { it.setUp() }
    }

    fun tearDown() {
        plugins.forEach { it.tearDown() }
    }

    companion object {
        val PARSED_COMMANDS: MutableMap<String, String> = HashMap()
        const val NS = "http://exam.extension.io"

        @JvmField
        val DEFAULT_NODE_MATCHER = DefaultNodeMatcher(byNameAndText, byName)

        @JvmField
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

private fun failTemplate(header: String = "", cntId: String) = //language=xml
    """
    <div class="card border-danger alert-warning">
      ${if (header.isNotEmpty()) "<div class='card-header bg-danger text-white'>$header</div>" else ""}
      <div class="card-body mb-1 mt-1">
        <pre id='$cntId' class="card-text" style='white-space: pre-wrap;'/>
      </div>
    </div>
    """

fun errorMessage(header: String = "", message: String, html: Html = span()): Html =
    "error-${Random().nextInt()}".let { id ->
        failTemplate(header, id).toHtml().apply { findBy(id)!!.text(message).below(html) }
    }

fun String.fixIndent() = this.replace("\n            ", "\n")
