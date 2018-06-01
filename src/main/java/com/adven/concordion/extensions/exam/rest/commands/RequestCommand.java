package com.adven.concordion.extensions.exam.rest.commands;

import com.adven.concordion.extensions.exam.commands.ExamCommand;
import com.adven.concordion.extensions.exam.html.Html;
import com.jayway.restassured.http.Method;
import org.concordion.api.CommandCall;
import org.concordion.api.Evaluator;
import org.concordion.api.ResultRecorder;

import java.util.HashMap;
import java.util.Map;

import static com.adven.concordion.extensions.exam.html.Html.*;

public abstract class RequestCommand extends ExamCommand {
    private static final String HEADERS = "headers";
    private static final String COOKIES = "cookies";
    private static final String TYPE = "type";
    private static final String URL = "url";

    public RequestCommand(String name, String tag) {
        super(name, tag);
    }

    protected abstract Method method();

    @Override
    public void setUp(CommandCall commandCall, Evaluator evaluator, ResultRecorder resultRecorder) {
        RequestExecutor executor = RequestExecutor.newExecutor(evaluator).method(method());
        Html root = new Html(commandCall.getElement()).success();

        String url = attr(root, URL, "/", evaluator);
        String type = attr(root, TYPE, "application/json", evaluator);
        String cookies = cookies(evaluator, root);
        Map<String, String> headersMap = headers(root, evaluator);

        addRequestDescTo(root, url, type, cookies);
        startTable(root, executor.hasRequestBody());

        executor.type(type).url(url).header(headersMap).cookies(cookies);
    }

    private void startTable(Html html, boolean hasRequestBody) {
        Html table = table();
        Html header = thead();
        Html tr = thead();
        if (hasRequestBody) {
            tr.childs(
                    th("Request")
            );
        }
        tr.childs(
                th("Expected response"),
                th("Status code")
        );
        table.childs(header.childs(tr));
        html.dropAllTo(table);
    }

    private Map<String, String> headers(Html html, Evaluator eval) {
        String headers = html.takeAwayAttr(HEADERS, eval);
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

    private String cookies(Evaluator eval, Html html) {
        String cookies = html.takeAwayAttr(COOKIES, eval);
        eval.setVariable("#cookies", cookies);
        return cookies;
    }

    private void addRequestDescTo(Html root, String url, String type, String cookies) {
        final Html div = div().childs(
                h(4, "").childs(
                        badge(method().name(), "success"),
                        badge(type, "info"),
                        code(url)
                )
        );
        if (cookies != null) {
            div.childs(
                    h(6, "").childs(
                            badge("Cookies", "info"),
                            code(cookies)
                    )
            );
        }
        root.childs(div);
    }

    private String attr(Html html, String attrName, String defaultValue, Evaluator evaluator) {
        String attr = html.takeAwayAttr(attrName, defaultValue, evaluator);
        evaluator.setVariable("#" + attrName, attr);
        return attr;
    }
}