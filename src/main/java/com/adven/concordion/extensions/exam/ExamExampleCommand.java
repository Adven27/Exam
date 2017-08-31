package com.adven.concordion.extensions.exam;

import com.adven.concordion.extensions.exam.commands.ExamCommand;
import com.adven.concordion.extensions.exam.html.Html;
import nu.xom.Attribute;
import nu.xom.Element;

import static com.google.common.base.Strings.isNullOrEmpty;

public class ExamExampleCommand extends ExamCommand {
    public ExamExampleCommand(String tag) {
        super("example", tag);
    }

    @Override
    public void beforeParse(Element elem) {
        transformToConcordionExample(elem);
        super.beforeParse(elem);
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
}