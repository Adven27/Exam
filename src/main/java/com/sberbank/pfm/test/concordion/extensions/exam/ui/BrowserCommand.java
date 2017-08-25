package com.sberbank.pfm.test.concordion.extensions.exam.ui;

import com.sberbank.pfm.test.concordion.extensions.exam.commands.ExamCommand;
import com.sberbank.pfm.test.concordion.extensions.exam.html.Html;
import org.concordion.api.CommandCall;
import org.concordion.api.Evaluator;
import org.concordion.api.ResultRecorder;

import java.util.ArrayList;
import java.util.List;

import static com.codeborne.selenide.Selenide.open;
import static com.sberbank.pfm.test.concordion.extensions.exam.html.Html.*;

public class BrowserCommand extends ExamCommand {
    private static final String URL = "url";
    private String url;

    @Override
    public void setUp(CommandCall commandCall, Evaluator evaluator, ResultRecorder resultRecorder) {
        Html root = new Html(commandCall.getElement()).style("table table-condensed");
        url = attr(root, URL, "/", evaluator);
        root.childs(tr().childs(th("Steps"), th("[" + url + "]")));
    }

    @Override
    public void execute(CommandCall commandCall, Evaluator evaluator, ResultRecorder resultRecorder) {
        open(url);
        evalSteps(new Html(commandCall.getElement()), evaluator);
    }

    private String attr(Html html, String attrName, String defaultValue, Evaluator evaluator) {
        String attr = html.takeAwayAttr(attrName, defaultValue, evaluator);
        if (attr.startsWith(":")) {
            attr = "http://localhost" + attr;
        }
        evaluator.setVariable("#" + attrName, attr);
        return attr;
    }

    protected List<List<Object>> evalSteps(Html el, Evaluator evaluator) {
        List<List<Object>> result = new ArrayList<>();
        int i = 0;
        for (Html s : el.childs()) {
            i++;
            if ("step".equals(s.localName())) {
                String name = s.attr("name");
                String text = s.text();
                eval(evaluator, name, text);
                el.remove(s);
                el.childs(
                        tr().childs(
                                td("Step " + i),
                                td(name + " [" + text + "]")
                        )
                );
            }
        }
        return result;
    }

    private void eval(Evaluator evaluator, String name, String text) {
        if ("".equals(text)) {
            evaluator.evaluate(name + "()");
        } else {
            evaluator.setVariable("#TEXT", text);
            evaluator.evaluate(name + "(#TEXT)");
        }
    }
}