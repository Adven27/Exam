package com.adven.concordion.extensions.exam.core

import com.adven.concordion.extensions.exam.core.utils.DateFormatMatcher
import com.adven.concordion.extensions.exam.core.utils.DateWithin
import com.adven.concordion.extensions.exam.core.utils.XMLDateWithin
import net.javacrumbs.jsonunit.JsonAssert.`when`
import net.javacrumbs.jsonunit.core.Configuration
import net.javacrumbs.jsonunit.core.Option.IGNORING_ARRAY_ORDER
import org.concordion.api.extension.ConcordionExtender
import org.concordion.api.extension.ConcordionExtension
import org.hamcrest.Matcher
import org.xmlunit.diff.DefaultNodeMatcher
import org.xmlunit.diff.ElementSelectors.byName
import org.xmlunit.diff.ElementSelectors.byNameAndText
import org.xmlunit.diff.NodeMatcher
import java.util.*
import java.util.Collections.addAll

class ExamExtension : ConcordionExtension {
    private var jsonUnitCfg: Configuration = DEFAULT_JSON_UNIT_CFG
    private var nodeMatcher: NodeMatcher = DEFAULT_NODE_MATCHER
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
    fun addPlugin(plugin: ExamPlugin): ExamExtension {
        plugins.add(plugin)
        return this
    }

    @Suppress("unused")
    fun withPlugins(vararg plugins: ExamPlugin): ExamExtension {
        addAll(this.plugins, *plugins)
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
        ex.withExampleListener(ExamExampleListener())
    }

    companion object {
        const val NS = "http://exam.extension.io"
        val DEFAULT_NODE_MATCHER = DefaultNodeMatcher(byNameAndText, byName)
        val DEFAULT_JSON_UNIT_CFG: Configuration = `when`(IGNORING_ARRAY_ORDER)
            .withMatcher("formattedAs", DateFormatMatcher())
            .withMatcher("formattedAndWithin", DateWithin.param())
            .withMatcher("formattedAndWithinNow", DateWithin.now())
            .withMatcher("xmlDateWithinNow", XMLDateWithin())
    }
}