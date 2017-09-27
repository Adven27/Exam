package com.adven.concordion.extensions.exam;

import com.adven.concordion.extensions.exam.commands.ExamCommand;
import com.adven.concordion.extensions.exam.html.Html;
import nu.xom.Attribute;
import nu.xom.Element;

public class ExamBeforeExampleCommand extends ExamCommand {
    public ExamBeforeExampleCommand(String tag) {
        super("before", tag);
    }

    @Override
    public void beforeParse(Element elem) {
        transformToConcordionExample(elem);
        super.beforeParse(elem);
    }

    private void transformToConcordionExample(Element elem) {
        String name = "before";
        Attribute exampleAttr = new Attribute("example", name);
        exampleAttr.setNamespace("c", "http://www.concordion.org/2007/concordion");
        elem.addAttribute(exampleAttr);

        new Html(new org.concordion.api.Element(elem)).panel(name);
    }
}