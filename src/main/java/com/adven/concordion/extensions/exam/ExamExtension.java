package com.adven.concordion.extensions.exam;

import com.adven.concordion.extensions.exam.bootstrap.BootstrapExtension;
import com.adven.concordion.extensions.exam.commands.ExamCommand;
import com.adven.concordion.extensions.exam.rest.DateFormatMatcher;
import com.adven.concordion.extensions.exam.rest.DateWithinMatcher;
import com.codeborne.selenide.Configuration;
import io.github.bonigarcia.wdm.ChromeDriverManager;
import org.concordion.api.extension.ConcordionExtender;
import org.concordion.api.extension.ConcordionExtension;
import org.dbunit.JdbcDatabaseTester;
import org.hamcrest.Matcher;

import static net.javacrumbs.jsonunit.JsonAssert.when;
import static net.javacrumbs.jsonunit.core.Option.IGNORING_ARRAY_ORDER;

public class ExamExtension implements ConcordionExtension {
    public static final String NS = "http://exam.extension.io";
    private net.javacrumbs.jsonunit.core.Configuration jsonUnitCfg;

    private JdbcDatabaseTester dbTester;

    public ExamExtension() {
        jsonUnitCfg = when(IGNORING_ARRAY_ORDER).
                withMatcher("formattedAs", new DateFormatMatcher()).
                withMatcher("formattedAndWithin", new DateWithinMatcher());
    }

    @SuppressWarnings("unused")
    public ExamExtension withJsonUnitMatcher(String matcherName, Matcher<?> matcher) {
        jsonUnitCfg = jsonUnitCfg.withMatcher(matcherName, matcher);
        return this;
    }

    private static void setUpChromeDriver(String version) {
        Configuration.browser = "chrome";
        ChromeDriverManager.getInstance().version(version).setup();
    }

    @SuppressWarnings("unused")
    public ExamExtension webDriver(String version) {
        setUpChromeDriver(version);
        return this;
    }

    @SuppressWarnings("unused")
    public ExamExtension webDriver() {
        return webDriver(null);
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

        final CommandRegistry registry = new CommandRegistry(dbTester, jsonUnitCfg);

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