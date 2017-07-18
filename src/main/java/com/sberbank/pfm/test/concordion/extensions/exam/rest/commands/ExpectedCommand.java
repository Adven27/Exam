package com.sberbank.pfm.test.concordion.extensions.exam.rest.commands;

import org.apache.commons.lang3.StringUtils;
import org.concordion.api.CommandCall;
import org.concordion.api.Element;
import org.concordion.api.Evaluator;
import org.concordion.api.ResultRecorder;
import org.concordion.internal.util.Check;

import static net.javacrumbs.jsonunit.JsonAssert.assertJsonEquals;
import static net.javacrumbs.jsonunit.JsonAssert.when;
import static net.javacrumbs.jsonunit.core.Option.IGNORING_ARRAY_ORDER;

public class ExpectedCommand extends RestVerifyCommand {

    public void verify(CommandCall commandCall, Evaluator evaluator, ResultRecorder resultRecorder) {
        Check.isFalse(commandCall.hasChildCommands(), "Nesting commands inside an 'assertEquals' is not supported");

        Element element = commandCall.getElement();
        element.addStyleClass("json");

        String expected = printer.prettyPrint(element.getText());
        element.moveChildrenTo(new Element("tmp"));
        element.appendText(expected);

        String actual = RequestExecutor.fromEvaluator(evaluator).responseBody();
        if (StringUtils.isEmpty(actual)) {
            failure(resultRecorder, element, "(not set)", expected);
            return;
        }

        String prettyActual = printer.prettyPrint(actual);
        try {
            if (areEqual(prettyActual, expected)) {
                success(resultRecorder, element);
            } else {
                failure(resultRecorder, element, prettyActual, expected);
            }
        } catch (Exception e) {
            e.printStackTrace();
            failure(resultRecorder, element, prettyActual, expected);
        }
    }

    private boolean areEqual(String prettyActual, String expected) {
        try {
            assertJsonEquals(expected, prettyActual, when(IGNORING_ARRAY_ORDER));
        } catch (AssertionError e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }
}