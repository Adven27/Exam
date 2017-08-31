package com.adven.concordion.extensions.exam;

import com.adven.concordion.extensions.exam.html.Html;
import nu.xom.Attribute;
import nu.xom.Document;
import nu.xom.Element;
import nu.xom.Elements;
import org.concordion.api.listener.DocumentParsingListener;

import static com.adven.concordion.extensions.exam.html.Html.codemirror;
import static com.google.common.base.Strings.isNullOrEmpty;

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
            Attribute attr = new Attribute(elem.getLocalName(), "");
            attr.setNamespace("e", ExamExtension.NS);
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

    private String translateTag(String command) {
        String name = registry.tagFor(command);
        return name == null ? command : name;
    }
}