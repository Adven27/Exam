package com.sberbank.pfm.test.concordion.extensions.exam;

import com.codeborne.selenide.Configuration;
import com.sberbank.pfm.test.concordion.extensions.exam.bootstrap.BootstrapExtension;
import com.sberbank.pfm.test.concordion.extensions.exam.commands.*;
import com.sberbank.pfm.test.concordion.extensions.exam.db.DummyTester;
import com.sberbank.pfm.test.concordion.extensions.exam.db.commands.DBCheckCommand;
import com.sberbank.pfm.test.concordion.extensions.exam.db.commands.DBSetCommand;
import com.sberbank.pfm.test.concordion.extensions.exam.db.commands.DBShowCommand;
import com.sberbank.pfm.test.concordion.extensions.exam.files.commands.FilesCheckCommand;
import com.sberbank.pfm.test.concordion.extensions.exam.files.commands.FilesSetCommand;
import com.sberbank.pfm.test.concordion.extensions.exam.files.commands.FilesShowCommand;
import com.sberbank.pfm.test.concordion.extensions.exam.html.Html;
import com.sberbank.pfm.test.concordion.extensions.exam.rest.DateFormatMatcher;
import com.sberbank.pfm.test.concordion.extensions.exam.rest.DateWithinMatcher;
import com.sberbank.pfm.test.concordion.extensions.exam.rest.commands.*;
import com.sberbank.pfm.test.concordion.extensions.exam.ui.BrowserCommand;
import io.github.bonigarcia.wdm.ChromeDriverManager;
import nu.xom.Attribute;
import nu.xom.Document;
import nu.xom.Element;
import nu.xom.Elements;
import org.concordion.api.extension.ConcordionExtender;
import org.concordion.api.extension.ConcordionExtension;
import org.concordion.api.listener.DocumentParsingListener;
import org.dbunit.JdbcDatabaseTester;
import org.hamcrest.Matcher;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import static com.google.common.base.Strings.isNullOrEmpty;
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

    private static void setUpChromeDriver(String version) {
        Configuration.browser = "chrome";
        ChromeDriverManager.getInstance().version(version).setup();
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

                tags.put("browser", "table");
            }

            @Override
            public void beforeParsing(Document document) {
                visit(document.getRootElement());
            }

            private void visit(Element elem) {
                log(elem);
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

            private void log(Element elem) {
                if (Boolean.valueOf(elem.getAttributeValue("log"))) {
                    Element pre = new Element("pre");
                    pre.appendChild(elem.toXML());
                    elem.insertChild(pre, 0);
                }
            }

            private String toXml(Element elem) {
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < elem.getChildCount(); i++) {
                    sb.append(elem.getChild(i).toXML());
                }
                return sb.toString();
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
    }
}