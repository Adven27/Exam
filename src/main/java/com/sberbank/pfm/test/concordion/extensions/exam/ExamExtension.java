package com.sberbank.pfm.test.concordion.extensions.exam;

import com.sberbank.pfm.test.concordion.extensions.exam.bootstrap.BootstrapExtension;
import com.sberbank.pfm.test.concordion.extensions.exam.commands.*;
import com.sberbank.pfm.test.concordion.extensions.exam.db.commands.DBCheckCommand;
import com.sberbank.pfm.test.concordion.extensions.exam.db.commands.DBSetCommand;
import com.sberbank.pfm.test.concordion.extensions.exam.db.commands.DBShowCommand;
import com.sberbank.pfm.test.concordion.extensions.exam.files.commands.FilesCheckCommand;
import com.sberbank.pfm.test.concordion.extensions.exam.files.commands.FilesSetCommand;
import com.sberbank.pfm.test.concordion.extensions.exam.files.commands.FilesShowCommand;
import com.sberbank.pfm.test.concordion.extensions.exam.rest.commands.*;
import nu.xom.Attribute;
import nu.xom.Document;
import nu.xom.Element;
import nu.xom.Elements;
import org.concordion.api.extension.ConcordionExtender;
import org.concordion.api.extension.ConcordionExtension;
import org.concordion.api.listener.DocumentParsingListener;
import org.dbunit.JdbcDatabaseTester;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class ExamExtension implements ConcordionExtension {
    public static final String NS = "http://exam.extension.io";
    public Map<String, Matcher> jsonUnitMatchers = new HashMap<>();

    private JdbcDatabaseTester dbTester;

    public ExamExtension dbTester(String driver, String url, String user, String password) {
        try {
            dbTester = new JdbcDatabaseTester(driver, url, user, password);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
        return this;
    }

    public ExamExtension dbTester(Properties properties) {
        return dbTester(
                properties.getProperty("hibernate.connection.driver_class"),
                properties.getProperty("connection.url"),
                properties.getProperty("hibernate.connection.username"),
                properties.getProperty("hibernate.connection.password")
        );
    }

    public ExamExtension dbTester(JdbcDatabaseTester dbTester) {
        this.dbTester = dbTester;
        return this;
    }

    @Override
    public void addTo(ConcordionExtender ex) {
        new CodeMirrorExtension().addTo(ex);
        new BootstrapExtension().addTo(ex);

        ex.withCommand(NS, "given", new GivenCommand());
        ex.withCommand(NS, "when", new WhenCommand());
        ex.withCommand(NS, "then", new ThenCommand());
        ex.withCommand(NS, "check", new CaseCheckCommand());
        ex.withCommand(NS, "example", new ExampleCommand());
        ex.withCommand(NS, "summary", new ExamplesSummaryCommand());

        ex.withCommand(NS, "db-show", new DBShowCommand(dbTester));
        ex.withCommand(NS, "db-check", new DBCheckCommand(dbTester));
        ex.withCommand(NS, "db-set", new DBSetCommand(dbTester));

        ex.withCommand(NS, "fl-show", new FilesShowCommand());
        ex.withCommand(NS, "fl-set", new FilesSetCommand());
        ex.withCommand(NS, "fl-check", new FilesCheckCommand());

        ex.withCommand(NS, "rs-post", new PostCommand());
        ex.withCommand(NS, "rs-get", new GetCommand());
        ex.withCommand(NS, "rs-case", new CaseCommand(jsonUnitMatchers));
        ex.withCommand(NS, "rs-echoJson", new EchoJsonCommand());
        ex.withCommand(NS, "rs-status", new ExpectedStatusCommand());

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
            }

            @Override
            public void beforeParsing(Document document) {
                visit(document.getRootElement());
            }

            private void visit(Element elem) {
                Elements children = elem.getChildElements();
                for (int i = 0; i < children.size(); i++) {
                    visit(children.get(i));
                }

                if (NS.equals(elem.getNamespaceURI())) {
                    Attribute attr = new Attribute(elem.getLocalName(), "");
                    attr.setNamespace("e", NS);
                    elem.addAttribute(attr);
                    elem.setNamespacePrefix("");
                    elem.setNamespaceURI(null);
                    elem.setLocalName(translateTag(elem.getLocalName()));
                }
            }

            private String translateTag(String localName) {
                String name = tags.get(localName);
                return name == null ? localName : name;
            }
        });

        ex.withSpecificationProcessingListener(new SpecSummaryListener());
    }

    public ExamExtension withJsonUnitMatcher(String matcherName, Matcher<?> matcher) {
        jsonUnitMatchers.put(matcherName, matcher);
        return this;
    }

    public static class DateMatcher extends BaseMatcher<Date> {

        @Override
        public boolean matches(Object item) {
            return false;
        }

        @Override
        public void describeTo(Description description) {

        }
    }
}