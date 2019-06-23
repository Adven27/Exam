package com.adven.concordion.extensions.exam.ui;

import com.adven.concordion.extensions.exam.core.html.Html;
import com.codeborne.selenide.ex.UIAssertionError;
import org.concordion.api.listener.*;

import java.io.File;
import java.util.concurrent.atomic.AtomicLong;

import static com.adven.concordion.extensions.exam.core.html.HtmlBuilder.imageOverlay;
import static com.codeborne.selenide.Selenide.screenshot;

public class UiResultRenderer implements AssertEqualsListener, AssertTrueListener, AssertFalseListener {

    private static final AtomicLong screenshotsCounter = new AtomicLong();
    private static final String DEFAULT_DESC = "No description";

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
        String desc = s.attr("desc");
        el.remove(s);
        el.childs(
            imageOverlay(getPath(screenshot(getFileName(name))), 360, name, checkAndGetDesc(desc), "rest-success")
        );
    }

    private String checkAndGetDesc(String desc) {
        return desc == null ? DEFAULT_DESC : desc;
    }

    private String getFileName(String name) {
        long increment = screenshotsCounter.incrementAndGet();
        return increment + "-" + name;
    }

    private String getPath(String screenshot) {
        return new File(screenshot).getName();
    }
}