package com.sberbank.pfm.test.concordion.extensions.exam.rest.commands;

import com.sberbank.pfm.test.concordion.extensions.exam.PlaceholdersResolver;
import com.sberbank.pfm.test.concordion.extensions.exam.html.Html;
import org.concordion.api.CommandCall;
import org.concordion.api.CommandCallList;
import org.concordion.api.Evaluator;
import org.concordion.api.ResultRecorder;

import static com.google.common.base.Strings.isNullOrEmpty;
import static com.sberbank.pfm.test.concordion.extensions.exam.html.Html.*;
import static com.sberbank.pfm.test.concordion.extensions.exam.rest.commands.RequestExecutor.fromEvaluator;

public class CaseCommand extends RestVerifyCommand {
    private static final String DESC = "desc";
    private static final String URL_PARAMS = "urlParams";
    private static final String COOKIES = "cookies";
    private int number = 0;

    @Override
    public void execute(CommandCall cmd, Evaluator eval, ResultRecorder resultRecorder) {
        CommandCallList childCommands = cmd.getChildren();
        childCommands.setUp(eval, resultRecorder);

        final String expectedStatus = "HTTP/1.1 200 OK";
        Html statusTd = td(expectedStatus);
        Html root = new Html(cmd.getElement()).childs(statusTd);

        RequestExecutor executor = fromEvaluator(eval).urlParams(root.takeAwayAttr(URL_PARAMS));

        String cookies = root.takeAwayAttr(COOKIES);
        if (cookies != null) {
            cookies = PlaceholdersResolver.resolve(cookies, eval);
            executor.cookies(cookies);
        }

        executor.execute();
        childCommands.execute(eval, resultRecorder);
        childCommands.verify(eval, resultRecorder);

        String actualStatus = executor.statusLine();
        if (expectedStatus.equals(actualStatus)) {
            success(resultRecorder, statusTd.el());
        } else {
            failure(resultRecorder, statusTd.el(), actualStatus, expectedStatus);
        }
    }

    @Override
    public void verify(CommandCall cmd, Evaluator evaluator, ResultRecorder resultRecorder) {
        RequestExecutor executor = fromEvaluator(evaluator);
        final String colspan = executor.hasRequestBody() ? "3" : "2";

        Html root = new Html(cmd.getElement()).dropAllTo(tr());
        Html div = div().childs(
                i(executor.requestMethod() + " "),
                code(executor.requestUrlWithParams())
        );
        String cookies = executor.cookies();
        if (!isNullOrEmpty(cookies)) {
            div.childs(i(" Cookies "), code(cookies));
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
}