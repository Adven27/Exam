package com.sberbank.pfm.test.concordion.extensions.exam.rest.commands;

import com.sberbank.pfm.test.concordion.extensions.exam.PlaceholdersResolver;
import org.concordion.api.*;

import static com.google.common.base.Strings.isNullOrEmpty;

public class CaseCommand extends RestVerifyCommand {
    private static final String DESC = "desc";
    private static final String URL_PARAMS = "urlParams";
    private static final String COOKIES = "cookies";
    private int number = 0;

    @Override
    public void execute(CommandCall commandCall, Evaluator evaluator, ResultRecorder resultRecorder) {
        CommandCallList childCommands = commandCall.getChildren();
        childCommands.setUp(evaluator, resultRecorder);

        Element element = commandCall.getElement();
        String urlParams = element.getAttributeValue(URL_PARAMS);
        if (urlParams != null) {
            element.removeAttribute(URL_PARAMS);
        }

        RequestExecutor executor = RequestExecutor.fromEvaluator(evaluator).urlParams(urlParams);

        String cookies = element.getAttributeValue(COOKIES);
        if (cookies != null) {
            cookies = PlaceholdersResolver.resolve(cookies, evaluator);
            element.removeAttribute(COOKIES);
            executor.cookies(cookies);
        }

        executor.execute();
        childCommands.execute(evaluator, resultRecorder);
        childCommands.verify(evaluator, resultRecorder);
    }

    @Override
    public void verify(CommandCall commandCall, Evaluator evaluator, ResultRecorder resultRecorder) {
        RequestExecutor executor = RequestExecutor.fromEvaluator(evaluator);
        final String colspan = executor.hasRequestBody() ? "3" : "2";

        Element element = commandCall.getElement();

        String expectedStatus = "HTTP/1.1 200 OK";

        Element td = new Element("td");
        td.appendText(expectedStatus);
        element.appendChild(td);

        Element descTD = new Element("td");
        descTD.addAttribute("colspan", colspan);
        descTD.addStyleClass("text-muted");
        descTD.appendText(caseDesc(element));

        Element tr = new Element("tr");
        element.moveChildrenTo(tr);
        element.appendSister(tr);

        Element getDescTD = new Element("td");
        getDescTD.addAttribute("colspan", colspan);

        Element getDescBlock = new Element("div");
        getDescBlock.appendChild(new Element("i").appendText(executor.requestMethod() + " "));
        getDescBlock.appendChild(new Element("code").appendText(executor.requestUrlWithParams()));
        String cookies = executor.cookies();
        if (!isNullOrEmpty(cookies)) {
            getDescBlock.appendChild(new Element("i").appendText(" Cookies "));
            getDescBlock.appendChild(new Element("code").appendText(cookies));
        }

        getDescTD.appendChild(getDescBlock);

        Element tr2 = new Element("tr");
        tr2.appendChild(getDescTD);
        element.appendSister(tr2);

        element.appendChild(descTD);

        String actualStatus = executor.statusLine();
        if (expectedStatus.equals(actualStatus)) {
            success(resultRecorder, td);
        } else {
            failure(resultRecorder, td, actualStatus, expectedStatus);
        }
    }

    private String caseDesc(Element element) {
        String desc = element.getAttributeValue(DESC);
        return ++number + ") " + (desc == null ? "" : desc);
    }
}