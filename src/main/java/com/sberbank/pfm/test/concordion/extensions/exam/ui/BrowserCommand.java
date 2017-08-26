package com.sberbank.pfm.test.concordion.extensions.exam.ui;

import com.sberbank.pfm.test.concordion.extensions.exam.commands.ExamCommand;
import com.sberbank.pfm.test.concordion.extensions.exam.html.Html;
import org.concordion.api.CommandCall;
import org.concordion.api.Evaluator;
import org.concordion.api.ResultRecorder;
import org.openqa.selenium.By;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.open;
import static com.sberbank.pfm.test.concordion.extensions.exam.html.Html.*;

public class BrowserCommand extends ExamCommand {
    private static final String URL = "url";
    private String url;

    @Override
    public void setUp(CommandCall commandCall, Evaluator evaluator, ResultRecorder resultRecorder) {
        Html root = new Html(commandCall.getElement()).style("table table-condensed");
        url = attr(root, URL, "/", evaluator);
        root.childs(tr().childs(th("Steps"), th("[" + url + "]"), th("")));
    }

    @Override
    public void execute(CommandCall commandCall, Evaluator evaluator, ResultRecorder resultRecorder) {
        open(url);
        evalSteps(new Html(commandCall.getElement()), evaluator);
    }

    protected List<List<Object>> evalSteps(Html el, Evaluator evaluator) {
        List<List<Object>> result = new ArrayList<>();
        int i = 0;
        for (Html s : el.childs()) {
            i++;
            if ("step".equals(s.localName())) {
                String name = s.attr("name");
                String text = s.text();
                File file = eval(evaluator, name, text);
                el.remove(s);
                el.childs(
                        tr().childs(
                                td("Step " + i),
                                td(name + " [" + text + "]"),
                                td().childs(
                                        thumbnail(file.getAbsolutePath())
                                )
                        )
                );
            }
        }
        return result;
    }

    private File eval(Evaluator ev, String name, String text) {
        String exp = name + "()";
        if (!"".equals(text)) {
            exp = name + "(#TEXT)";
            ev.setVariable("#TEXT", text);
        }
        ev.evaluate(exp);
        return $(By.tagName("html")).screenshot();
    }

    private String attr(Html html, String attrName, String defaultValue, Evaluator evaluator) {
        String attr = html.takeAwayAttr(attrName, defaultValue, evaluator);
        if (attr.startsWith(":")) {
            attr = "http://localhost" + attr;
        }
        evaluator.setVariable("#" + attrName, attr);
        return attr;
    }
}