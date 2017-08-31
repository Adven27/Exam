package com.adven.concordion.extensions.exam;

import com.adven.concordion.extensions.exam.commands.ExamCommand;
import nu.xom.Document;
import nu.xom.Element;
import nu.xom.Elements;
import org.concordion.api.listener.DocumentParsingListener;

import static com.adven.concordion.extensions.exam.html.Html.codemirror;

class ExamDocumentParsingListener implements DocumentParsingListener {
    private final CommandRegistry registry;

    public ExamDocumentParsingListener(CommandRegistry registry) {
        this.registry = registry;
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
            elem.prependChild(codemirror("xml").text(sb.toString()).el());
        }
    }
}