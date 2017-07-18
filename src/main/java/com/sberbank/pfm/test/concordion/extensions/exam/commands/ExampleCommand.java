package com.sberbank.pfm.test.concordion.extensions.exam.commands;

import org.concordion.api.CommandCall;
import org.concordion.api.Element;
import org.concordion.api.Evaluator;
import org.concordion.api.ResultRecorder;

public class ExampleCommand extends ExamCommand {
    private String header;
    private int number = 0;

    @Override
    public void setUp(CommandCall commandCall, Evaluator evaluator, ResultRecorder resultRecorder) {
        Element element = commandCall.getElement();
        element.addStyleClass("panel panel-info table-responsive");
        header = ++number + ". " + element.getAttributeValue("name");
        element.removeAttribute("name");
        element.addAttribute("data-example", String.valueOf(number));

        Element panelBody = new Element("div");
        panelBody.addStyleClass("panel-body");
        element.moveChildrenTo(panelBody);

        Element panelHeader = new Element("div");
        panelHeader.addStyleClass("panel-heading");
        panelHeader.appendChild(new Element("a").
                addAttribute("data-type", "example").
                addAttribute("name", header).
                appendText(header));

        element.appendChild(panelHeader);
        element.appendChild(panelBody);
    }

    @Override
    public void execute(CommandCall commandCall, Evaluator evaluator, ResultRecorder resultRecorder) {
        System.err.println("\n*********** START OF " + header + " ***********");
        super.execute(commandCall, evaluator, resultRecorder);
        System.err.println("*********** END OF " + header + " ***********");
    }
}