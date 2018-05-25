package com.adven.concordion.extensions.exam.rest.commands;

import com.adven.concordion.extensions.exam.PlaceholdersResolver;
import com.adven.concordion.extensions.exam.html.Html;
import com.adven.concordion.extensions.exam.html.RowParser;
import com.adven.concordion.extensions.exam.rest.JsonPrettyPrinter;
import com.jayway.restassured.response.Response;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.jsonunit.core.Configuration;
import org.apache.commons.collections.map.HashedMap;
import org.concordion.api.CommandCall;
import org.concordion.api.CommandCallList;
import org.concordion.api.Evaluator;
import org.concordion.api.ResultRecorder;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.adven.concordion.extensions.exam.html.Html.*;
import static com.adven.concordion.extensions.exam.rest.commands.RequestExecutor.fromEvaluator;
import static com.google.common.base.Strings.isNullOrEmpty;
import static net.javacrumbs.jsonunit.JsonAssert.assertJsonEquals;
import static org.apache.commons.lang3.StringUtils.isEmpty;

@Slf4j
public class CaseCommand extends RestVerifyCommand {
    private static final String DESC = "desc";
    private static final String URL_PARAMS = "urlParams";
    private static final String COOKIES = "cookies";
    private static final String VARIABLES = "vars";
    private static final String VALUES = "vals";
    private static final String BODY = "body";
    private static final String EXPECTED = "expected";
    private static final String WHERE = "where";
    private static final String CASE = "case";

    private final JsonPrettyPrinter jsonPrinter = new JsonPrettyPrinter();
    private List<Map<String, Object>> cases = new ArrayList<>();
    private int number = 0;
    private Configuration cfg;

    public CaseCommand(String tag, Configuration cfg) {
        super(CASE, tag);
        this.cfg = cfg;
    }

    @Override
    public void setUp(CommandCall commandCall, Evaluator eval, ResultRecorder resultRecorder) {
        Html caseRoot = new Html(commandCall.getElement());
        cases.clear();
        final Html where = caseRoot.first(WHERE);
        if (where != null) {
            String[] names = where.takeAwayAttr(VARIABLES, eval).split(",");
            for (List<Object> val : new RowParser(where, VALUES, eval).parse()) {
                Map<String, Object> caseParams = new HashedMap();
                for (int j = 0; j < names.length; j++) {
                    caseParams.put("#" + names[j].trim(), val.get(j));
                }
                cases.add(caseParams);
            }
        } else {
            cases.add(new HashedMap());
        }

        Html body = caseRoot.first(BODY);
        Html expected = caseRoot.first(EXPECTED);
        caseRoot.remove(body, expected);
        for (Map<String, Object> ignored : cases) {
            caseRoot.childs(
                    Html.tag(CASE).childs(
                            body == null ? null : Html.tag(BODY).text(body.text()),
                            Html.tag(EXPECTED).text(expected.text())
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
                executor.cookies(PlaceholdersResolver.INSTANCE.resolveJson(cookies, eval));
            }

            executor.urlParams(urlParams == null ? null : PlaceholdersResolver.INSTANCE.resolveJson(urlParams, eval));

            Html caseTR = tr().insteadOf(root.first(CASE));
            Html body = caseTR.first(BODY);
            if (body != null) {
                Html td = td().insteadOf(body).css("json");
                String bodyStr = PlaceholdersResolver.INSTANCE.resolveJson(td.text(), eval);
                td.removeAllChild().text(jsonPrinter.prettyPrint(bodyStr));
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

            vf(td().insteadOf(caseTR.first(EXPECTED)), eval, resultRecorder);

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
        Html rt = new Html(cmd.getElement());
        String caseDesc = caseDesc(rt.attr(DESC), evaluator);
        rt.attr("data-type", CASE).attr("id", caseDesc).above(
                tr().childs(
                        td(caseDesc).attr("colspan", colspan).muted()
                )
        );
    }

    private String caseDesc(String desc, Evaluator eval) {
        return ++number + ") " + (desc == null ? "" : PlaceholdersResolver.INSTANCE.resolveJson(desc, eval));
    }

    private void vf(Html root, Evaluator eval, ResultRecorder resultRecorder) {
        final String expected = printer.prettyPrint(PlaceholdersResolver.INSTANCE.resolveJson(root.text(), eval));
        root.removeAllChild().text(expected).css("json");

        RequestExecutor executor = fromEvaluator(eval);

        fillCaseContext(root, executor);

        String actual = executor.responseBody();
        if (isEmpty(actual)) {
            failure(resultRecorder, root, "(not set)", expected);
            return;
        }

        String prettyActual = printer.prettyPrint(actual);
        try {
            assertJsonEquals(expected, prettyActual, cfg);
            success(resultRecorder, root);
        } catch (AssertionError | Exception e) {
            log.warn("Failed to assert expected={} with actual={}", expected, prettyActual, e);
            failure(resultRecorder, root, prettyActual, expected);
        }
    }

    private void fillCaseContext(Html root, RequestExecutor executor) {
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

        root.parent().above(tr().childs(td().attr("colspan", executor.hasRequestBody() ? "3" : "2").childs(div)));
    }
}