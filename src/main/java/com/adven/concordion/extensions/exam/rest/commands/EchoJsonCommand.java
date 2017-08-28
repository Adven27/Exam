package com.adven.concordion.extensions.exam.rest.commands;

import org.concordion.api.CommandCall;
import org.concordion.api.Element;
import org.concordion.api.Evaluator;
import org.concordion.api.ResultRecorder;
import org.concordion.internal.util.Check;

public class EchoJsonCommand extends RestVerifyCommand {

    @Override
    public void verify(CommandCall commandCall, Evaluator evaluator, ResultRecorder resultRecorder) {
        Check.isFalse(commandCall.hasChildCommands(), "Nesting commands inside an 'echo' is not supported");

        Object result = evaluator.evaluate(commandCall.getExpression());

        Element e = commandCall.getElement();
        e.addStyleClass("json");
        e.moveChildrenTo(new Element("tmp"));
        e.appendText(printer.prettyPrint(result.toString()));
    }
}