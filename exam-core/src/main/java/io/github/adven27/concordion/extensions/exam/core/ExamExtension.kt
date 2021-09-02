@file:Suppress("TooManyFunctions")

package io.github.adven27.concordion.extensions.exam.core

import com.github.jknack.handlebars.Handlebars
import io.github.adven27.concordion.extensions.exam.core.json.DefaultObjectMapperProvider
import io.github.adven27.concordion.extensions.exam.core.utils.DateFormatMatcher
import io.github.adven27.concordion.extensions.exam.core.utils.DateWithin
import io.github.adven27.concordion.extensions.exam.core.utils.HANDLEBARS
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

    @Suppress("unused")
    fun withContentVerifiers(addContentVerifiers: Map<String, ContentVerifier>): ExamExtension {
        CONTENT_VERIFIERS += addContentVerifiers
        return this
    }

    @Suppress("unused")
    fun withContentResolvers(addContentResolvers: Map<String, ContentResolver>): ExamExtension {
        CONTENT_RESOLVERS += addContentResolvers
        return this
    }

    @Suppress("unused")
    fun withContentPrinters(addContentPrinters: Map<String, ContentPrinter>): ExamExtension {
        CONTENT_PRINTERS += addContentPrinters
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
        val registry = CommandRegistry(JsonVerifier(), XmlVerifier())
        plugins.forEach { registry.register(it.commands()) }

        registry.commands()
            .filter { "example" != it.name() }
            .forEach { ex.withCommand(NS, it.name(), it) }

        CodeMirrorExtension().addTo(ex)
        HighlightExtension().addTo(ex)
        FontAwesomeExtension().addTo(ex)
        BootstrapExtension().addTo(ex)
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

        @JvmField
        var JACKSON_2_OBJECT_MAPPER_PROVIDER: Jackson2ObjectMapperProvider = DefaultObjectMapperProvider()

        @JvmField
        val DEFAULT_NODE_MATCHER = DefaultNodeMatcher(byNameAndText, byName)
        val DEFAULT_JSON_UNIT_CFG: Configuration
            get() = `when`(IGNORING_ARRAY_ORDER).apply {
                MATCHERS.forEach { withMatcher(it.key, it.value) }
            }
        val MATCHERS: MutableMap<String, Matcher<*>> = mutableMapOf(
            "formattedAs" to DateFormatMatcher(),
            "formattedAndWithin" to DateWithin.param(),
            "formattedAndWithinNow" to DateWithin.now(),
            "xmlDateWithinNow" to XMLDateWithin(),
        )

        val CONTENT_TYPE_CONFIGS: MutableMap<String, ContentTypeConfig> = mutableMapOf(
            "json" to JsonContentTypeConfig(),
            "xml" to XmlContentTypeConfig(),
            "text" to TextContentTypeConfig(),
        )

        @JvmStatic
        fun contentTypeConfig(type: String): ContentTypeConfig = CONTENT_TYPE_CONFIGS[type]
            ?: throw IllegalStateException("Content type config for type '$type' not found. Provide it via ExamExtension(...).withContentTypeConfigs(...) method.")

        private val CONTENT_VERIFIERS: MutableMap<String, ContentVerifier> = mutableMapOf(
            "json" to JsonVerifier(),
            "xml" to XmlVerifier(),
            "text" to ContentVerifier.Default(),
        )

        @JvmStatic
        fun contentVerifier(type: String): ContentVerifier = CONTENT_VERIFIERS[type]
            ?: throw IllegalStateException("Content verifier for type '$type' not found. Provide it via ExamExtension(...).withContentVerifiers(...) method.")

        private val CONTENT_RESOLVERS: MutableMap<String, ContentResolver> = mutableMapOf(
            "json" to JsonResolver(),
            "xml" to XmlResolver(),
            "text" to JsonResolver(),
        )

        private val CONTENT_PRINTERS: MutableMap<String, ContentPrinter> = mutableMapOf(
            "json" to JsonPrinter(),
            "xml" to XmlPrinter(),
            "text" to ContentPrinter.AsIs(),
        )

        @JvmStatic
        fun prettyXml(text: String) = text.prettyXml()

        @JvmStatic
        fun prettyJson(text: String) = text.prettyJson()
    }
}
