package com.sberbank.pfm.test.concordion.extensions.exam.commands;

import com.sberbank.pfm.test.concordion.extensions.exam.html.Html;
import org.concordion.api.CommandCall;
import org.concordion.api.Evaluator;
import org.concordion.api.ResultRecorder;

public class ExampleCommand extends ExamCommand {
    private String header;
    private int number = 0;

    @Override
    public void setUp(CommandCall commandCall, Evaluator evaluator, ResultRecorder resultRecorder) {
        final Html root = new Html(commandCall.getElement());
        header = ++number + ". " + root.takeAwayAttr("name");
        root.panel(header).attr("data-example", String.valueOf(number));
    }

    @Override
    public void execute(CommandCall commandCall, Evaluator evaluator, ResultRecorder resultRecorder) {
        System.err.println("\n*********** START OF " + header + " ***********");
        super.execute(commandCall, evaluator, resultRecorder);
        System.err.println("*********** END OF " + header + " ***********");
    }
}