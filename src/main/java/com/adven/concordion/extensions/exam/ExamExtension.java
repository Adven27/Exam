package com.adven.concordion.extensions.exam;

import com.adven.concordion.extensions.exam.bootstrap.BootstrapExtension;
import com.adven.concordion.extensions.exam.commands.ExamCommand;
import com.adven.concordion.extensions.exam.configurators.DbTester;
import com.adven.concordion.extensions.exam.configurators.RestAssuredCfg;
import com.adven.concordion.extensions.exam.configurators.WebDriverCfg;
import com.adven.concordion.extensions.exam.rest.DateFormatMatcher;
import com.adven.concordion.extensions.exam.rest.DateWithin;
import net.javacrumbs.jsonunit.core.Configuration;
import org.concordion.api.extension.ConcordionExtender;
import org.concordion.api.extension.ConcordionExtension;
import org.dbunit.JdbcDatabaseTester;
import org.hamcrest.Matcher;
import org.xmlunit.diff.DefaultNodeMatcher;
import org.xmlunit.diff.NodeMatcher;

import static net.javacrumbs.jsonunit.JsonAssert.when;
import static net.javacrumbs.jsonunit.core.Option.IGNORING_ARRAY_ORDER;
import static org.xmlunit.diff.ElementSelectors.byName;
import static org.xmlunit.diff.ElementSelectors.byNameAndText;

public class ExamExtension implements ConcordionExtension {
    public static final String NS = "http://exam.extension.io";
    public static final DefaultNodeMatcher DEFAULT_NODE_MATCHER = new DefaultNodeMatcher(byNameAndText, byName);
    public static final Configuration DEFAULT_JSON_UNIT_CFG = when(IGNORING_ARRAY_ORDER).
            withMatcher("formattedAs", new DateFormatMatcher()).
            withMatcher("formattedAndWithin", DateWithin.param()).
            withMatcher("formattedAndWithinNow", DateWithin.now());
    private net.javacrumbs.jsonunit.core.Configuration jsonUnitCfg;

    private JdbcDatabaseTester dbTester;
    private NodeMatcher nodeMatcher;

    public ExamExtension() {
        jsonUnitCfg = DEFAULT_JSON_UNIT_CFG;
        nodeMatcher = DEFAULT_NODE_MATCHER;
    }

    @SuppressWarnings("unused")
    public ExamExtension withJsonUnitMatcher(String matcherName, Matcher<?> matcher) {
        jsonUnitCfg = jsonUnitCfg.withMatcher(matcherName, matcher);
        return this;
    }

    public ExamExtension withXmlUnitNodeMatcher(NodeMatcher nodeMatcher) {
        this.nodeMatcher = nodeMatcher;
        return this;
    }

    @SuppressWarnings("unused")
    public WebDriverCfg webDriver() {
        return new WebDriverCfg(this);
    }

    public RestAssuredCfg rest() {
        return new RestAssuredCfg(this);
    }

    public DbTester db() {
        return new DbTester(this);
    }

    @SuppressWarnings("unused")
    public ExamExtension dbTester(JdbcDatabaseTester dbTester) {
        this.dbTester = dbTester;
        return this;
    }

    @Override
    public void addTo(ConcordionExtender ex) {
        new CodeMirrorExtension().addTo(ex);
        new BootstrapExtension().addTo(ex);

        final CommandRegistry registry = new CommandRegistry(dbTester, jsonUnitCfg, nodeMatcher);

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