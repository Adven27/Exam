package com.sberbank.pfm.test.concordion.extensions.exam.rest.commands;

import com.sberbank.pfm.test.concordion.extensions.exam.PlaceholdersResolver;
import com.sberbank.pfm.test.concordion.extensions.exam.rest.JsonPrettyPrinter;
import org.concordion.api.*;

import java.util.HashMap;
import java.util.Map;

public class BodyCommand extends AbstractCommand {

    @Override
    public void setUp(CommandCall commandCall, Evaluator evaluator, ResultRecorder resultRecorder) {
        Element e = commandCall.getElement();
        String headers = e.getAttributeValue("headers");
        e.addStyleClass("json");
        String body = e.getText();
        body = PlaceholdersResolver.resolve(body, evaluator);
        e.moveChildrenTo(new Element("tmp"));
        e.appendText(new JsonPrettyPrinter().prettyPrint(body));


        Map<String, String> headersMap = new HashMap<>();
        if (headers != null) {
            String[] headersArray = headers.split(",");
            for (int i = 0; i < headersArray.length; i++) {
                if (i - 1 % 2 == 0) {
                    headersMap.put(headersArray[i - 1], headersArray[i]);
                }
            }
            e.removeAttribute("headers");
        }

        RequestExecutor.fromEvaluator(evaluator).body(body).header(headersMap);
    }
}