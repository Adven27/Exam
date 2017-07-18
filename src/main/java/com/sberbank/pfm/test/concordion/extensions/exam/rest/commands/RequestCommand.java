package com.sberbank.pfm.test.concordion.extensions.exam.rest.commands;

import com.sberbank.pfm.test.concordion.extensions.exam.PlaceholdersResolver;
import com.sberbank.pfm.test.concordion.extensions.exam.commands.ExamCommand;
import org.concordion.api.CommandCall;
import org.concordion.api.Element;
import org.concordion.api.Evaluator;
import org.concordion.api.ResultRecorder;

import java.util.HashMap;
import java.util.Map;

import static com.sberbank.pfm.test.concordion.extensions.exam.rest.commands.RequestExecutor.newExecutor;

public abstract class RequestCommand extends ExamCommand {
    private static final String HEADERS = "headers";
    private static final String COOKIES = "cookies";
    private static final String TYPE = "type";
    private static final String URL = "url";

    protected abstract String method();

    @Override
    public void setUp(CommandCall commandCall, Evaluator evaluator, ResultRecorder resultRecorder) {
        RequestExecutor executor = newExecutor(evaluator).method(method());
        Element element = commandCall.getElement();
        element.addStyleClass("bs-callout bs-callout-success");

        String url = attr(element, URL, "/", evaluator);
        String type = attr(element, TYPE, "application/json", evaluator);
        String cookies = cookies(evaluator, element);
        Map<String, String> headersMap = headers(element);

        addRequestDescTo(element, url, type, cookies);
        startTable(element, executor.hasRequestBody());

        executor.type(type).url(url).header(headersMap).cookies(cookies);
    }

    private void startTable(Element element, boolean hasRequestBody) {
        Element table = new Element("table");
        table.addStyleClass("table table-condensed");
        Element headerRow = new Element("tr");
        if (hasRequestBody) {
            headerRow.appendChild(new Element("th").appendText("Request"));
        }
        headerRow.appendChild(new Element("th").appendText("Expected response"));
        headerRow.appendChild(new Element("th").appendText("Status code"));
        table.appendChild(headerRow);
        element.moveChildrenTo(table);
        element.appendChild(table);
    }

    private Map<String, String> headers(Element element) {
        String headers = element.getAttributeValue(HEADERS);
        Map<String, String> headersMap = new HashMap<>();
        if (headers != null) {
            String[] headersArray = headers.split(",");
            for (int i = 0; i < headersArray.length; i++) {
                if (i - 1 % 2 == 0) {
                    headersMap.put(headersArray[i - 1], headersArray[i]);
                }
            }
            element.removeAttribute("headers");
        }
        return headersMap;
    }

    private String cookies(Evaluator evaluator, Element element) {
        String cookies = element.getAttributeValue(COOKIES);
        if (cookies != null) {
            cookies = PlaceholdersResolver.resolve(cookies, evaluator);
            element.removeAttribute(COOKIES);
        }
        evaluator.setVariable("#cookies", cookies);
        return cookies;
    }

    private void addRequestDescTo(Element element, String url, String type, String cookies) {
        Element div = new Element("div");
        div.appendChild(new Element("span").appendText(method() + " "));
        div.appendChild(new Element("code").appendText(url));
        div.appendChild(new Element("span").appendText("  Content-Type  "));
        div.appendChild(new Element("code").appendText(type));
        if (cookies != null) {
            div.appendChild(new Element("span").appendText("  Cookies  "));
            div.appendChild(new Element("code").appendText(cookies));
        }
        element.appendChild(div);
    }

    private String attr(Element element, String attrName, String defaultValue, Evaluator evaluator) {
        String attr = element.getAttributeValue(attrName);
        if (attr == null) {
            attr = defaultValue;
        } else {
            element.removeAttribute(attrName);
        }
        evaluator.setVariable("#" + attrName, attr);
        return attr;
    }
}