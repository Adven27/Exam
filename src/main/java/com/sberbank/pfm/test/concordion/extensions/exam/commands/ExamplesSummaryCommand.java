package com.sberbank.pfm.test.concordion.extensions.exam.commands;

import org.concordion.api.*;

import static com.google.common.base.Strings.isNullOrEmpty;

public class ExamplesSummaryCommand extends AbstractCommand {
    @Override
    public void setUp(CommandCall commandCall, Evaluator evaluator, ResultRecorder resultRecorder) {
        Element el = commandCall.getElement();
        el.addAttribute("id", "summary");
        final String title = el.getAttributeValue("title");
        if (!isNullOrEmpty(title)) {
            el.appendChild(new Element("h4").appendText(title));
            el.removeAttribute("title");
        }
    }
}