package com.adven.concordion.extensions.exam.commands;

import nu.xom.Attribute;
import nu.xom.Element;

import static org.concordion.internal.ConcordionBuilder.NAMESPACE_CONCORDION_2007;

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
        Attribute attr = new Attribute("example", "before");
        attr.setNamespace("c", NAMESPACE_CONCORDION_2007);
        elem.addAttribute(attr);
    }
}