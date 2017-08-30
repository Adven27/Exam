package com.adven.concordion.extensions.exam;

import com.adven.concordion.extensions.exam.bootstrap.BootstrapExtension;
import com.adven.concordion.extensions.exam.commands.ExamplesSummaryCommand;
import com.adven.concordion.extensions.exam.commands.GivenCommand;
import com.adven.concordion.extensions.exam.commands.ThenCommand;
import com.adven.concordion.extensions.exam.commands.WhenCommand;
import com.adven.concordion.extensions.exam.db.DummyTester;
import com.adven.concordion.extensions.exam.db.commands.DBCheckCommand;
import com.adven.concordion.extensions.exam.db.commands.DBSetCommand;
import com.adven.concordion.extensions.exam.db.commands.DBShowCommand;
import com.adven.concordion.extensions.exam.files.commands.FilesCheckCommand;
import com.adven.concordion.extensions.exam.files.commands.FilesSetCommand;
import com.adven.concordion.extensions.exam.files.commands.FilesShowCommand;
import com.adven.concordion.extensions.exam.html.Html;
import com.adven.concordion.extensions.exam.rest.DateFormatMatcher;
import com.adven.concordion.extensions.exam.rest.DateWithinMatcher;
import com.adven.concordion.extensions.exam.rest.commands.*;
import com.adven.concordion.extensions.exam.ui.BrowserCommand;
import com.codeborne.selenide.Configuration;
import io.github.bonigarcia.wdm.ChromeDriverManager;
import nu.xom.Attribute;
import nu.xom.Document;
import nu.xom.Element;
import nu.xom.Elements;
import org.concordion.api.ImplementationStatus;
import org.concordion.api.ResultSummary;
import org.concordion.api.extension.ConcordionExtender;
import org.concordion.api.extension.ConcordionExtension;
import org.concordion.api.listener.DocumentParsingListener;
import org.concordion.api.listener.ExampleEvent;
import org.concordion.api.listener.ExampleListener;
import org.dbunit.JdbcDatabaseTester;
import org.hamcrest.Matcher;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import static com.adven.concordion.extensions.exam.html.Html.badge;
import static com.adven.concordion.extensions.exam.html.Html.codemirror;
import static com.google.common.base.Strings.isNullOrEmpty;
import static net.javacrumbs.jsonunit.JsonAssert.when;
import static net.javacrumbs.jsonunit.core.Option.IGNORING_ARRAY_ORDER;
import static org.concordion.api.ImplementationStatus.*;

public class ExamExtension implements ConcordionExtension {
    public static final String NS = "http://exam.extension.io";
    private net.javacrumbs.jsonunit.core.Configuration jsonUnitCfg;

    private JdbcDatabaseTester dbTester;

    public ExamExtension() {
        jsonUnitCfg = when(IGNORING_ARRAY_ORDER).
                withMatcher("formattedAs", new DateFormatMatcher()).
                withMatcher("formattedAndWithin", new DateWithinMatcher());
    }

    private static void setUpChromeDriver(String version) {
        Configuration.browser = "chrome";
        ChromeDriverManager.getInstance().version(version).setup();
    }

    @SuppressWarnings("unused")
    public ExamExtension dbTester(String driver, String url, String user, String password) {
        try {
            dbTester = new JdbcDatabaseTester(driver, url, user, password);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
        return this;
    }

    public ExamExtension dbTester(DummyTester tester) {
        try {
            this.dbTester = tester.instance();
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
        return this;
    }

    @SuppressWarnings("unused")
    public ExamExtension dbTester(Properties props) {
        return dbTester(
                props.getProperty("hibernate.connection.driver_class"),
                props.getProperty("connection.url"),
                props.getProperty("hibernate.connection.username"),
                props.getProperty("hibernate.connection.password")
        );
    }

    @SuppressWarnings("unused")
    public ExamExtension dbTester(JdbcDatabaseTester dbTester) {
        this.dbTester = dbTester;
        return this;
    }

    @SuppressWarnings("unused")
    public ExamExtension withJsonUnitMatcher(String matcherName, Matcher<?> matcher) {
        jsonUnitCfg = jsonUnitCfg.withMatcher(matcherName, matcher);
        return this;
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

    @Override
    public void addTo(ConcordionExtender ex) {
        new CodeMirrorExtension().addTo(ex);
        new BootstrapExtension().addTo(ex);

        ex.withCommand(NS, "given", new GivenCommand());
        ex.withCommand(NS, "when", new WhenCommand());
        ex.withCommand(NS, "then", new ThenCommand());
        ex.withCommand(NS, "check", new CaseCheckCommand());
        ex.withCommand(NS, "summary", new ExamplesSummaryCommand());

        ex.withCommand(NS, "db-show", new DBShowCommand(dbTester));
        ex.withCommand(NS, "db-check", new DBCheckCommand(dbTester));
        ex.withCommand(NS, "db-set", new DBSetCommand(dbTester));

        ex.withCommand(NS, "fl-show", new FilesShowCommand());
        ex.withCommand(NS, "fl-set", new FilesSetCommand());
        ex.withCommand(NS, "fl-check", new FilesCheckCommand());

        ex.withCommand(NS, "rs-post", new PostCommand());
        ex.withCommand(NS, "rs-get", new GetCommand());
        ex.withCommand(NS, "rs-case", new CaseCommand(jsonUnitCfg));
        ex.withCommand(NS, "rs-echoJson", new EchoJsonCommand());
        ex.withCommand(NS, "rs-status", new ExpectedStatusCommand());

        ex.withCommand(NS, "browser", new BrowserCommand());

        ex.withDocumentParsingListener(new DocumentParsingListener() {

            private Map<String, String> tags = new HashMap<>();

            {
                tags.put("given", "div");
                tags.put("when", "div");
                tags.put("then", "div");
                tags.put("example", "div");
                tags.put("summary", "div");

                tags.put("db-show", "table");
                tags.put("db-check", "table");
                tags.put("db-set", "table");

                tags.put("fl-show", "table");
                tags.put("fl-check", "table");
                tags.put("fl-set", "table");

                tags.put("rs-post", "div");
                tags.put("rs-get", "div");
                tags.put("rs-case", "tr");
                tags.put("rs-status", "code");

                tags.put("browser", "div");
            }

            @Override
            public void beforeParsing(Document document) {
                visit(document.getRootElement());
            }

            private void visit(Element elem) {
                log(new org.concordion.api.Element(elem));
                Elements children = elem.getChildElements();
                for (int i = 0; i < children.size(); i++) {
                    visit(children.get(i));
                }

                if (NS.equals(elem.getNamespaceURI())) {
                    Attribute attr = new Attribute(elem.getLocalName(), "");
                    attr.setNamespace("e", NS);
                    elem.addAttribute(attr);
                    if (elem.getLocalName().equals("example")) {
                        transformToConcordionExample(elem);
                    }

                    elem.setNamespacePrefix("");
                    elem.setNamespaceURI(null);
                    elem.setLocalName(translateTag(elem.getLocalName()));
                }
            }

            private void log(org.concordion.api.Element elem) {
                if (Boolean.valueOf(elem.getAttributeValue("print"))) {
                    StringBuilder sb = new StringBuilder();
                    for (org.concordion.api.Element e : elem.getChildElements()) {
                        sb.append("\n" + e.toXML());
                    }
                    elem.prependChild(codemirror("xml").text(sb.toString()).el());
                }
            }

            private void transformToConcordionExample(Element elem) {
                String name = elem.getAttributeValue("name");
                Attribute exampleAttr = new Attribute("example", name);
                exampleAttr.setNamespace("c", "http://www.concordion.org/2007/concordion");
                elem.addAttribute(exampleAttr);

                String val = elem.getAttributeValue("status");
                if (!isNullOrEmpty(val)) {
                    Attribute statusAttr = new Attribute("status", val);
                    statusAttr.setNamespace("c", "http://www.concordion.org/2007/concordion");
                    elem.addAttribute(statusAttr);
                }

                new Html(new org.concordion.api.Element(elem)).panel(name);
            }

            private String translateTag(String localName) {
                String name = tags.get(localName);
                return name == null ? localName : name;
            }
        });

        ex.withSpecificationProcessingListener(new SpecSummaryListener());
        ex.withExampleListener(new ExampleListener() {
            @Override
            public void beforeExample(ExampleEvent event) {

            }

            @Override
            public void afterExample(ExampleEvent event) {
                ResultSummary resultSummary = event.getResultSummary();
                ImplementationStatus status = resultSummary.getImplementationStatus();
                org.concordion.api.Element header = event.getElement().getFirstChildElement("div").getFirstChildElement("a");
                if (header != null) {
                    if (status != null) {
                        header.appendChild(badgeFor(status));
                    } else {
                        if (resultSummary.hasExceptions()) {
                            header.appendChild(badge("Fail", "danger").el());
                        } else {
                            header.appendChild(badge("Success", "success").el());
                        }
                    }
                }
            }

            private org.concordion.api.Element badgeFor(ImplementationStatus status) {
                switch (status) {
                    case EXPECTED_TO_PASS:
                        return badge(EXPECTED_TO_PASS.getTag(), "info").el();
                    case EXPECTED_TO_FAIL:
                        return badge(EXPECTED_TO_FAIL.getTag(), "warning").el();
                    case UNIMPLEMENTED:
                        return badge(UNIMPLEMENTED.getTag(), "default").el();
                }
                throw new UnsupportedOperationException("Unsupported spec implementation status " + status);
            }
        });
    }
}