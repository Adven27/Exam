package com.adven.concordion.extensions.exam.core;

import com.adven.concordion.extensions.exam.core.commands.ExamCommand;
import com.adven.concordion.extensions.exam.core.utils.DateFormatMatcher;
import com.adven.concordion.extensions.exam.core.utils.DateWithin;
import com.adven.concordion.extensions.exam.core.utils.XMLDateWithin;
import net.javacrumbs.jsonunit.core.Configuration;
import org.concordion.api.extension.ConcordionExtender;
import org.concordion.api.extension.ConcordionExtension;
import org.hamcrest.Matcher;
import org.xmlunit.diff.DefaultNodeMatcher;
import org.xmlunit.diff.NodeMatcher;

import java.util.ArrayList;
import java.util.List;

import static java.util.Collections.addAll;
import static net.javacrumbs.jsonunit.JsonAssert.when;
import static net.javacrumbs.jsonunit.core.Option.IGNORING_ARRAY_ORDER;
import static org.xmlunit.diff.ElementSelectors.byName;
import static org.xmlunit.diff.ElementSelectors.byNameAndText;

public class ExamExtension implements ConcordionExtension {
    public static final String NS = "http://exam.extension.io";
    public static final DefaultNodeMatcher DEFAULT_NODE_MATCHER = new DefaultNodeMatcher(byNameAndText, byName);
    public static final Configuration DEFAULT_JSON_UNIT_CFG = when(IGNORING_ARRAY_ORDER)
        .withMatcher("formattedAs", new DateFormatMatcher())
        .withMatcher("formattedAndWithin", DateWithin.Companion.param())
        .withMatcher("formattedAndWithinNow", DateWithin.Companion.now())
        .withMatcher("xmlDateWithinNow", new XMLDateWithin());

    private Configuration jsonUnitCfg;
    private NodeMatcher nodeMatcher;
    private final List<ExamPlugin> plugins = new ArrayList<>();

    public ExamExtension() {
        jsonUnitCfg = DEFAULT_JSON_UNIT_CFG;
        nodeMatcher = DEFAULT_NODE_MATCHER;
    }

    /**
     * Attach xmlunit/jsonunit matchers.
     *
     * @param matcherName name to reference in placeholder.
     * @param matcher     implementation.
     *                    <br/>usage:<br/>
     *                    !{matcherName param1 param2}
     */
    @SuppressWarnings("unused")
    public ExamExtension addPlaceholderMatcher(String matcherName, Matcher<?> matcher) {
        jsonUnitCfg = jsonUnitCfg.withMatcher(matcherName, matcher);
        return this;
    }

    @SuppressWarnings("unused")
    public ExamExtension withXmlUnitNodeMatcher(NodeMatcher nodeMatcher) {
        this.nodeMatcher = nodeMatcher;
        return this;
    }

    @SuppressWarnings("unused")
    public ExamExtension addPlugin(ExamPlugin plugin) {
        plugins.add(plugin);
        return this;
    }

    @SuppressWarnings("unused")
    public ExamExtension withPlugins(ExamPlugin... plugins) {
        addAll(this.plugins, plugins);
        return this;
    }

    @Override
    public void addTo(ConcordionExtender ex) {
        CommandRegistry registry = new CommandRegistry(jsonUnitCfg, nodeMatcher);
        for (ExamPlugin plugin : plugins) {
            registry.register(plugin.commands());
        }
        new CodeMirrorExtension().addTo(ex);
        new BootstrapExtension().addTo(ex);

        for (ExamCommand cmd : registry.commands()) {
            if (!"example".equals(cmd.name())) {
                ex.withCommand(NS, cmd.name(), cmd);
            }
        }

        ex.withDocumentParsingListener(new ExamDocumentParsingListener(registry));
        ex.withSpecificationProcessingListener(new SpecSummaryListener());
        ex.withExampleListener(new ExamExampleListener());
    }
}