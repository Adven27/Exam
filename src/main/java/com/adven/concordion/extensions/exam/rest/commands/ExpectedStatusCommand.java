package com.adven.concordion.extensions.exam.rest.commands;

import org.concordion.api.CommandCall;
import org.concordion.api.Element;
import org.concordion.api.Evaluator;
import org.concordion.api.ResultRecorder;
import org.concordion.internal.util.Check;

public class ExpectedStatusCommand extends RestVerifyCommand {

    public ExpectedStatusCommand(String name, String tag) {
        super(name, tag);
    }

    @Override
    public void verify(CommandCall commandCall, Evaluator evaluator, ResultRecorder resultRecorder) {
        Check.isFalse(commandCall.hasChildCommands(), "Nesting commands inside an 'assertEquals' is not supported");

        Element element = commandCall.getElement();
        String expected = element.getText();
        String actual = evaluator.evaluate(commandCall.getExpression()).toString();

        if (expected.equals(actual)) {
            success(resultRecorder, element);
        } else {
            failure(resultRecorder, element, actual, expected);
        }
    }
}