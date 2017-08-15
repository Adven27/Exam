package com.sberbank.pfm.test.concordion.extensions.exam.rest.commands;

import com.jayway.restassured.response.Response;
import com.sberbank.pfm.test.concordion.extensions.exam.PlaceholdersResolver;
import com.sberbank.pfm.test.concordion.extensions.exam.html.Html;
import com.sberbank.pfm.test.concordion.extensions.exam.rest.JsonPrettyPrinter;
import net.javacrumbs.jsonunit.core.Configuration;
import org.apache.commons.collections.map.HashedMap;
import org.concordion.api.CommandCall;
import org.concordion.api.CommandCallList;
import org.concordion.api.Evaluator;
import org.concordion.api.ResultRecorder;
import org.hamcrest.Matcher;

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
    private static final String VARIABLES = "variables";
    private static final String VALUES = "values";
    private final JsonPrettyPrinter jsonPrinter = new JsonPrettyPrinter();
    private final Map<String, Matcher> jsonUnitMatchers;
    List<Map<String, Object>> cases = new ArrayList<>();
    private int number = 0;

    public CaseCommand(Map<String, Matcher> jsonUnitMatchers) {
        this.jsonUnitMatchers = jsonUnitMatchers;
    }

    @Override
    public void setUp(CommandCall commandCall, Evaluator eval, ResultRecorder resultRecorder) {
        cases.clear();
        Html caseRoot = new Html(commandCall.getElement());
        String params = caseRoot.takeAwayAttr(VARIABLES, eval);
        String values = caseRoot.takeAwayAttr(VALUES, eval);
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

        final RequestExecutor executor = fromEvaluator(eval);
        String urlParams = root.takeAwayAttr(URL_PARAMS);
        String cookies = root.takeAwayAttr(COOKIES);

        for (Map<String, Object> aCase : cases) {
            for (Map.Entry<String, Object> entry : aCase.entrySet()) {
                eval.setVariable(entry.getKey(), entry.getValue());
            }

            if (cookies != null) {
                executor.cookies(PlaceholdersResolver.resolve(cookies, eval));
            }

            executor.urlParams(urlParams == null ? null : PlaceholdersResolver.resolve(urlParams, eval));

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

        final String colspan = executor.hasRequestBody() ? "3" : "2";
        Html rt = new Html(cmd.getElement());
        rt.above(
                tr().childs(
                        td(caseDesc(rt.attr(DESC))).attr("colspan", colspan).muted()
                )
        ).above(
                tr().childs(
                        td().attr("colspan", colspan).childs(
                                div
                        )
                )
        );
    }

    private String caseDesc(String desc) {
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
            Configuration cfg = when(IGNORING_ARRAY_ORDER);
            for (Map.Entry<String, Matcher> e : jsonUnitMatchers.entrySet()) {
                cfg = cfg.withMatcher(e.getKey(), e.getValue());
            }
            assertJsonEquals(expected, prettyActual, cfg);
        } catch (AssertionError e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }
}