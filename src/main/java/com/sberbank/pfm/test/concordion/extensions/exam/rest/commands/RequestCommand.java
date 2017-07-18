package com.sberbank.pfm.test.concordion.extensions.exam.rest.commands;

import com.sberbank.pfm.test.concordion.extensions.exam.PlaceholdersResolver;
import com.sberbank.pfm.test.concordion.extensions.exam.commands.ExamCommand;
import com.sberbank.pfm.test.concordion.extensions.exam.html.Html;
import org.concordion.api.CommandCall;
import org.concordion.api.Evaluator;
import org.concordion.api.ResultRecorder;

import java.util.HashMap;
import java.util.Map;

import static com.sberbank.pfm.test.concordion.extensions.exam.html.Html.*;
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
        Html root = new Html(commandCall.getElement()).success();

        String url = attr(root, URL, "/", evaluator);
        String type = attr(root, TYPE, "application/json", evaluator);
        String cookies = cookies(evaluator, root);
        Map<String, String> headersMap = headers(root);

        addRequestDescTo(root, url, type, cookies);
        startTable(root, executor.hasRequestBody());

        executor.type(type).url(url).header(headersMap).cookies(cookies);
    }

    private void startTable(Html html, boolean hasRequestBody) {
        Html table = table();
        Html header = tr();
        if (hasRequestBody) {
            header.childs(
                    th("Request")
            );
        }
        header.childs(
                th("Expected response"),
                th("Status code")
        );
        table.childs(header);
        html.dropAllTo(table);
    }

    private Map<String, String> headers(Html html) {
        String headers = html.takeAwayAttr(HEADERS);
        Map<String, String> headersMap = new HashMap<>();
        if (headers != null) {
            String[] headersArray = headers.split(",");
            for (int i = 0; i < headersArray.length; i++) {
                if (i - 1 % 2 == 0) {
                    headersMap.put(headersArray[i - 1], headersArray[i]);
                }
            }
        }
        return headersMap;
    }

    private String cookies(Evaluator evaluator, Html html) {
        String cookies = html.takeAwayAttr(COOKIES);
        if (cookies != null) {
            cookies = PlaceholdersResolver.resolve(cookies, evaluator);
        }
        evaluator.setVariable("#cookies", cookies);
        return cookies;
    }

    private void addRequestDescTo(Html root, String url, String type, String cookies) {
        final Html div = Html.div().childs(
                span(method() + " "),
                code(url),
                span("  Content-Type  "),
                code(type)
        );
        if (cookies != null) {
            div.childs(
                    span("  Cookies  "),
                    code(cookies)
            );
        }
        root.childs(div);
    }

    private String attr(Html html, String attrName, String defaultValue, Evaluator evaluator) {
        String attr = html.takeAwayAttr(attrName, defaultValue);
        evaluator.setVariable("#" + attrName, attr);
        return attr;
    }
}