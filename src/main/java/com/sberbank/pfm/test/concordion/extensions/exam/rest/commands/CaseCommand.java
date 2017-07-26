package com.sberbank.pfm.test.concordion.extensions.exam.rest.commands;

import com.jayway.restassured.response.Response;
import com.sberbank.pfm.test.concordion.extensions.exam.html.Html;
import com.sberbank.pfm.test.concordion.extensions.exam.rest.JsonPrettyPrinter;
import org.apache.commons.collections.map.HashedMap;
import org.concordion.api.CommandCall;
import org.concordion.api.CommandCallList;
import org.concordion.api.Evaluator;
import org.concordion.api.ResultRecorder;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.google.common.base.Strings.isNullOrEmpty;
import static com.sberbank.pfm.test.concordion.extensions.exam.PlaceholdersResolver.resolve;
import static com.sberbank.pfm.test.concordion.extensions.exam.html.Html.*;
import static com.sberbank.pfm.test.concordion.extensions.exam.rest.commands.RequestExecutor.fromEvaluator;
import static net.javacrumbs.jsonunit.JsonAssert.assertJsonEquals;
import static net.javacrumbs.jsonunit.JsonAssert.when;
import static net.javacrumbs.jsonunit.core.Option.IGNORING_ARRAY_ORDER;
import static org.apache.commons.lang3.StringUtils.isEmpty;

public class CaseCommand extends RestVerifyCommand {
    private static final String DESC = "desc";
    private static final String URL_PARAMS = "urlParams";
    private static final String COOKIES = "cookies";
    private final JsonPrettyPrinter jsonPrinter = new JsonPrettyPrinter();
    List<Map<String, Object>> cases = new ArrayList<>();
    private int number = 0;

    @Override
    public void setUp(CommandCall commandCall, Evaluator evaluator, ResultRecorder resultRecorder) {
        cases.clear();
        Html caseRoot = new Html(commandCall.getElement());
        String params = caseRoot.takeAwayAttr("params");
        String values = caseRoot.takeAwayAttr("values");
        if (!isNullOrEmpty(params)) {
            String[] names = params.split(":");
            String[] vals = values.split(",");
            for (String val : vals) {
                String[] vals4Name = val.split(":");
                Map<String, Object> caseParams = new HashedMap();
                for (int j = 0; j < names.length; j++) {
                    caseParams.put("#" + names[j], vals4Name[j]);
                }
                cases.add(caseParams);
            }
        } else {
            cases.add(new HashedMap());
        }

        Html body = caseRoot.first("body");
        Html expected = caseRoot.first("expected");
        caseRoot.removeChilds(body, expected);
        for (Map<String, Object> ignored : cases) {
            caseRoot.childs(
                    tag("case").childs(
                            body == null ? null : tag("body").text(body.text()),
                            tag("expected").text(expected.text())
                    )
            );
        }

    }

    @Override
    public void execute(CommandCall cmd, Evaluator eval, ResultRecorder resultRecorder) {
        CommandCallList childCommands = cmd.getChildren();
        Html root = new Html(cmd.getElement());

        final RequestExecutor executor = fromEvaluator(eval).urlParams(root.takeAwayAttr(URL_PARAMS));

        String cookies = root.takeAwayAttr(COOKIES);
        if (cookies != null) {
            cookies = resolve(cookies, eval);
            executor.cookies(cookies);
        }

        for (Map<String, Object> aCase : cases) {
            for (Map.Entry<String, Object> entry : aCase.entrySet()) {
                eval.setVariable(entry.getKey(), entry.getValue());
            }

            Html caseTR = tr().insteadOf(root.first("case"));
            Html body = caseTR.first("body");
            if (body != null) {
                Html td = td().insteadOf(body).style("json");
                String bodyStr = resolve(td.text(), eval);
                td.removeChildren().text(jsonPrinter.prettyPrint(bodyStr));
                executor.body(bodyStr);
            }

            final String expectedStatus = "HTTP/1.1 200 OK";
            Html statusTd = td(expectedStatus);
            caseTR.childs(statusTd);

            childCommands.setUp(eval, resultRecorder);
            Response response = executor.execute();
            eval.setVariable("#exam_response", response);
            childCommands.execute(eval, resultRecorder);
            childCommands.verify(eval, resultRecorder);

            vf(td().insteadOf(caseTR.first("expected")), eval, resultRecorder);

            String actualStatus = executor.statusLine();
            if (expectedStatus.equals(actualStatus)) {
                success(resultRecorder, statusTd.el());
            } else {
                failure(resultRecorder, statusTd.el(), actualStatus, expectedStatus);
            }
        }
    }

    @Override
    public void verify(CommandCall cmd, Evaluator evaluator, ResultRecorder resultRecorder) {
        RequestExecutor executor = fromEvaluator(evaluator);
        final String colspan = executor.hasRequestBody() ? "3" : "2";

        Html root = new Html(cmd.getElement()).dropAllTo(tr());
        Html div = div().childs(
                italic(executor.requestMethod() + " "),
                code(executor.requestUrlWithParams())
        );
        String cookies = executor.cookies();
        if (!isNullOrEmpty(cookies)) {
            div.childs(
                    italic(" Cookies "),
                    code(cookies)
            );
        }

        for (int i = 0; i < cases.size(); i++) {
            Map<String, Object> aCase = cases.get(i);
            if (!aCase.isEmpty()) {
                div.childs(
                        italic(i == 0 ? " Варианты " : "/"),
                        code(aCase.values().toString())
                );
            }
        }

        root.childs(
                td(caseDesc(root)).attr("colspan", colspan).muted()
        ).below(
                tr().childs(
                        td().attr("colspan", colspan).childs(
                                div
                        )
                )
        );
    }

    private String caseDesc(Html element) {
        String desc = element.attr(DESC);
        return ++number + ") " + (desc == null ? "" : desc);
    }

    private void vf(Html root, Evaluator eval, ResultRecorder resultRecorder) {
        final String expected = printer.prettyPrint(resolve(root.text(), eval));
        root.removeChildren().text(expected).style("json");

        String actual = fromEvaluator(eval).responseBody();
        if (isEmpty(actual)) {
            failure(resultRecorder, root, "(not set)", expected);
            return;
        }

        String prettyActual = printer.prettyPrint(actual);
        try {
            if (areEqual(prettyActual, expected)) {
                success(resultRecorder, root);
            } else {
                failure(resultRecorder, root, prettyActual, expected);
            }
        } catch (Exception e) {
            e.printStackTrace();
            failure(resultRecorder, root, prettyActual, expected);
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