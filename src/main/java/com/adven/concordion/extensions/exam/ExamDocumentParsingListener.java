package com.adven.concordion.extensions.exam;

import com.adven.concordion.extensions.exam.commands.ExamCommand;
import com.adven.concordion.extensions.exam.html.Html;
import nu.xom.Document;
import nu.xom.Element;
import nu.xom.Elements;
import org.concordion.api.listener.DocumentParsingListener;

import static com.adven.concordion.extensions.exam.html.Html.*;

class ExamDocumentParsingListener implements DocumentParsingListener {
    private final CommandRegistry registry;

    public ExamDocumentParsingListener(CommandRegistry registry) {
        this.registry = registry;
    }

    @Override
    public void beforeParsing(Document document) {
        visit(document.getRootElement());

        addToTopButton(document);
    }

    private void addToTopButton(Document document) {
        Html el = new Html(new org.concordion.api.Element(document.getRootElement()));
        el.childs(
                Companion.button("").attr("id", "btnToTop").attr("onclick", "topFunction()").childs(
                        Companion.italic("").css("fa fa-arrow-up fa-3x")
                )
        );
    }

    private void visit(Element elem) {
        log(new org.concordion.api.Element(elem));
        Elements children = elem.getChildElements();
        for (int i = 0; i < children.size(); i++) {
            visit(children.get(i));
        }

        if (ExamExtension.NS.equals(elem.getNamespaceURI())) {
            ExamCommand command = registry.getBy(elem.getLocalName());
            if (command != null) {
                command.beforeParse(elem);
            }
        }
    }

    private void log(org.concordion.api.Element elem) {
        if (Boolean.valueOf(elem.getAttributeValue("print"))) {
            StringBuilder sb = new StringBuilder();
            for (org.concordion.api.Element e : elem.getChildElements()) {
                sb.append("\n" + e.toXML());
            }
            elem.prependChild(Companion.codeXml(sb.toString()).el());
        }
    }
}