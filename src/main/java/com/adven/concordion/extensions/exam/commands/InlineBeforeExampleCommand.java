package com.adven.concordion.extensions.exam.commands;

import com.adven.concordion.extensions.exam.ExamExtension;
import nu.xom.Element;
import nu.xom.Nodes;
import nu.xom.XPathContext;
import org.concordion.api.CommandCall;
import org.concordion.api.Evaluator;
import org.concordion.api.ResultRecorder;

public class InlineBeforeExampleCommand extends ExamCommand {
    public InlineBeforeExampleCommand(String tag) {
        super("inline", tag);
    }

    @Override
    public void beforeParse(Element elem) {
        super.beforeParse(elem);
        final XPathContext ns = new XPathContext("e", ExamExtension.NS);
        Nodes examples = elem.getDocument().getRootElement().getFirstChildElement("body").query(".//e:example", ns);
        elem.detach();
        for (int i = 0; i < examples.size(); i++) {
            Element example = (Element) examples.get(i);
            if (example.getChildElements().size() > 0) {
                example.insertChild(elem.copy(), 0);
            }
        }
    }
}