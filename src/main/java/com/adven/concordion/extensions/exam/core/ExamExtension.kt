package com.adven.concordion.extensions.exam.core

import com.adven.concordion.extensions.exam.core.html.Html
import com.adven.concordion.extensions.exam.core.html.pre
import com.adven.concordion.extensions.exam.core.utils.DateFormatMatcher
import com.adven.concordion.extensions.exam.core.utils.DateWithin
import com.adven.concordion.extensions.exam.core.utils.HANDLEBARS
import com.adven.concordion.extensions.exam.core.utils.XMLDateWithin
import com.github.jknack.handlebars.Handlebars
import net.javacrumbs.jsonunit.JsonAssert.`when`
import net.javacrumbs.jsonunit.core.Configuration
import net.javacrumbs.jsonunit.core.Option.IGNORING_ARRAY_ORDER
import org.concordion.api.extension.ConcordionExtender
import org.concordion.api.extension.ConcordionExtension
import org.concordion.api.listener.ExampleEvent
import org.hamcrest.Matcher
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat
import org.joda.time.format.ISODateTimeFormat
import org.xmlunit.diff.DefaultNodeMatcher
import org.xmlunit.diff.ElementSelectors.byName
import org.xmlunit.diff.ElementSelectors.byNameAndText
import org.xmlunit.diff.NodeMatcher
import java.util.*
import java.util.Collections.addAll
import kotlin.collections.HashMap

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

fun String.parseDate(format: String?): Date = this.parseDateTime(format).toDate()

fun String.parseDateTime(format: String?): DateTime = DateTime.parse(
    this, if (format == null) ISODateTimeFormat.dateTimeParser().withOffsetParsed() else DateTimeFormat.forPattern(format)
)

fun String.fileExt() = substring(lastIndexOf('.') + 1).toLowerCase()