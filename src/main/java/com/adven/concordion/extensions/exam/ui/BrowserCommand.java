package com.adven.concordion.extensions.exam.ui;

import com.adven.concordion.extensions.exam.commands.ExamCommand;
import com.adven.concordion.extensions.exam.html.Html;
import org.concordion.api.CommandCall;
import org.concordion.api.Evaluator;
import org.concordion.api.ResultRecorder;
import org.openqa.selenium.By;

import java.io.File;

import static com.adven.concordion.extensions.exam.html.Html.imageOverlay;
import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.open;

public class BrowserCommand extends ExamCommand {
    private static final String URL = "url";
    private String url;

    @Override
    public void setUp(CommandCall commandCall, Evaluator evaluator, ResultRecorder resultRecorder) {
        Html root = new Html(commandCall.getElement());
        url = attr(root, URL, "/", evaluator);
    }

    @Override
    public void execute(CommandCall commandCall, Evaluator evaluator, ResultRecorder resultRecorder) {
        open(url);
        Html root = new Html(commandCall.getElement()).css("card-group");
        //Html group = div().css("card-group");
        evalSteps(root, evaluator);
        /*root.childs(
                div().css("card-header").text(url),
                div().css("card-body").childs(
                        group
                )
        );*/
    }

    protected void evalSteps(Html el, Evaluator evaluator) {
        for (Html s : el.childs()) {
            if ("step".equals(s.localName())) {
                String name = s.attr("name");
                String text = s.text();
                File file = eval(evaluator, name, text, s.attr("set"));
                el.remove(s);
                el.childs(
                        imageOverlay(file.getAbsolutePath(), 360, name, "Step desc")
                );
            }
        }
    }

    private File eval(Evaluator ev, String name, String text, String var) {
        String exp = name + "()";
        if (!"".equals(text)) {
            exp = name + "(#TEXT)";
            ev.setVariable("#TEXT", text);
        }
        Object res = ev.evaluate(exp);
        if (var != null) {
            ev.setVariable("#" + var, res);
        }
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