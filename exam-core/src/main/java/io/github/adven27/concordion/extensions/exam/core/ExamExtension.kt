@file:Suppress("TooManyFunctions")

package io.github.adven27.concordion.extensions.exam.core

import ch.qos.logback.classic.turbo.TurboFilter
import com.github.jknack.handlebars.Handlebars
import io.github.adven27.concordion.extensions.exam.core.handlebars.HANDLEBARS
import io.github.adven27.concordion.extensions.exam.core.json.DefaultObjectMapperProvider
import io.github.adven27.concordion.extensions.exam.core.logger.LoggerLevelFilter
import io.github.adven27.concordion.extensions.exam.core.logger.LoggingFormatterExtension
import io.github.adven27.concordion.extensions.exam.core.utils.After
import io.github.adven27.concordion.extensions.exam.core.utils.Before
import io.github.adven27.concordion.extensions.exam.core.utils.DateFormatMatcher
import io.github.adven27.concordion.extensions.exam.core.utils.DateWithinNow
import io.github.adven27.concordion.extensions.exam.core.utils.DateWithinParam
import io.github.adven27.concordion.extensions.exam.core.utils.XMLDateWithin
import net.javacrumbs.jsonunit.JsonAssert.`when`
import net.javacrumbs.jsonunit.core.Configuration
import net.javacrumbs.jsonunit.core.Option.IGNORING_ARRAY_ORDER
import net.javacrumbs.jsonunit.providers.Jackson2ObjectMapperProvider
import org.concordion.api.extension.ConcordionExtender
import org.concordion.api.extension.ConcordionExtension
import org.concordion.api.listener.ExampleEvent
import org.hamcrest.Matcher
import org.xmlunit.diff.DefaultNodeMatcher
import org.xmlunit.diff.ElementSelectors.byName
import org.xmlunit.diff.ElementSelectors.byNameAndText
import org.xmlunit.diff.NodeMatcher
import java.util.function.Consumer

class ExamExtension constructor(private vararg var plugins: ExamPlugin) : ConcordionExtension {
    private var focusOnError: Boolean = true
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
    fun withPlaceholderMatcher(matcherName: String, matcher: Matcher<*>): ExamExtension {
        MATCHERS[matcherName] = matcher
        return this
    }

    @Suppress("unused")
    fun withXmlUnitNodeMatcher(nodeMatcher: NodeMatcher): ExamExtension {
        this.nodeMatcher = nodeMatcher
        return this
    }

    @Suppress("unused")
    fun withJackson2ObjectMapperProvider(provider: Jackson2ObjectMapperProvider): ExamExtension {
        JACKSON_2_OBJECT_MAPPER_PROVIDER = provider
        return this
    }

    @Suppress("unused")
    fun withHandlebar(fn: Consumer<Handlebars>): ExamExtension {
        fn.accept(HANDLEBARS)
        return this
    }

    @Suppress("unused")
    fun withContentTypeConfigs(addContentTypeConfigs: Map<String, ContentTypeConfig>): ExamExtension {
        CONTENT_TYPE_CONFIGS += addContentTypeConfigs
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

    @Suppress("unused")
    fun withLoggingFilter(loggerLevel: Map<String, String>): ExamExtension {
        LOGGING_FILTER = LoggerLevelFilter(loggerLevel)
        return this
    }

    override fun addTo(ex: ConcordionExtender) {
        val registry = CommandRegistry(JsonVerifier(), XmlVerifier())
        plugins.forEach { registry.register(it.commands()) }

        registry.commands()
            .filter { "example" != it.name }
            .forEach { ex.withCommand(NS, it.name, it) }

        CommandPrinterExtension().addTo(ex)
        IncludesExtension().addTo(ex)
        TopButtonExtension().addTo(ex)
        CodeMirrorExtension().addTo(ex)
        HighlightExtension().addTo(ex)
        TocbotExtension().addTo(ex)
        FontAwesomeExtension().addTo(ex)
        BootstrapExtension().addTo(ex)
        DetailsExtension().addTo(ex)
        ResponsiveTableExtension().addTo(ex)
        NomNomlExtension().addTo(ex)
        LoggingFormatterExtension().addTo(ex)

        ex.withDocumentParsingListener(ExamDocumentParsingListener(registry))
        ex.withThrowableListener(ErrorListener())
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

        val MATCHERS: MutableMap<String, Matcher<*>> = mutableMapOf(
            "formattedAs" to DateFormatMatcher(),
            "formattedAndWithin" to DateWithinParam(),
            "formattedAndWithinNow" to DateWithinNow(),
            "xmlDateWithinNow" to XMLDateWithin(),
            "after" to After(),
            "before" to Before(),
        )

        @JvmField
        var JACKSON_2_OBJECT_MAPPER_PROVIDER: Jackson2ObjectMapperProvider = DefaultObjectMapperProvider()

        @JvmField
        val DEFAULT_NODE_MATCHER = DefaultNodeMatcher(byNameAndText, byName)

        @JvmField
        val DEFAULT_JSON_UNIT_CFG: Configuration = `when`(IGNORING_ARRAY_ORDER).let { cfg ->
            MATCHERS.map { cfg to it }
                .reduce { acc, cur ->
                    acc.first
                        .withMatcher(acc.second.key, acc.second.value)
                        .withMatcher(cur.second.key, cur.second.value) to cur.second
                }.first
        }

        val CONTENT_TYPE_CONFIGS: MutableMap<String, ContentTypeConfig> = mutableMapOf(
            "json" to JsonContentTypeConfig(),
            "xml" to XmlContentTypeConfig(),
            "text" to TextContentTypeConfig(),
        )

        @JvmStatic
        fun contentTypeConfig(type: String): ContentTypeConfig = CONTENT_TYPE_CONFIGS[type]
            ?: throw IllegalStateException(
                "Content type config for type '$type' not found. " +
                    "Provide it via ExamExtension(...).withContentTypeConfigs(...) method."
            )

        @JvmStatic
        fun prettyXml(text: String) = text.prettyXml()

        @JvmStatic
        fun prettyJson(text: String) = text.prettyJson()

        var LOGGING_FILTER: TurboFilter = LoggerLevelFilter()
    }
}
