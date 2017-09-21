package com.adven.concordion.extensions.exam.ui;

import com.adven.concordion.extensions.exam.html.Html;
import com.codeborne.selenide.ex.UIAssertionError;
import org.concordion.api.listener.*;

import java.io.File;

import static com.adven.concordion.extensions.exam.html.Html.imageOverlay;
import static com.codeborne.selenide.Selenide.screenshot;

public class UiResultRenderer implements AssertEqualsListener, AssertTrueListener, AssertFalseListener {

    public void failureReported(AssertFailureEvent event) {
        Html s = new Html(event.getElement());
        Html el = s.parent();
        UIAssertionError err = (UIAssertionError) event.getActual();
        el.remove(s);
        el.childs(
                imageOverlay(getPath(err.getScreenshot()), 360, event.getExpected(), err.getMessage(), "rest-failure")
        );
    }

    public void successReported(AssertSuccessEvent event) {
        Html s = new Html(event.getElement());
        Html el = s.parent();
        String name = s.attr("name");
        el.remove(s);
        el.childs(
                imageOverlay(getPath(screenshot(name)), 360, name, "Step desc", "rest-success")
        );
    }

    private String getPath(String screenshot) {
        return new File(screenshot).getName();
    }
}