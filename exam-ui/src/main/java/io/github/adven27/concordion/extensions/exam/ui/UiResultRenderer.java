package io.github.adven27.concordion.extensions.exam.ui;

import io.github.adven27.concordion.extensions.exam.core.html.Html;
import com.codeborne.selenide.ex.UIAssertionError;
import org.concordion.api.listener.AssertEqualsListener;
import org.concordion.api.listener.AssertFailureEvent;
import org.concordion.api.listener.AssertFalseListener;
import org.concordion.api.listener.AssertSuccessEvent;
import org.concordion.api.listener.AssertTrueListener;

import java.io.File;
import java.util.concurrent.atomic.AtomicLong;

import static io.github.adven27.concordion.extensions.exam.core.html.HtmlBuilder.imageOverlay;
import static io.github.adven27.concordion.extensions.exam.core.html.HtmlBuilder.noImageOverlay;
import static com.codeborne.selenide.Selenide.screenshot;

public class UiResultRenderer implements AssertEqualsListener, AssertTrueListener, AssertFalseListener {
    private static final AtomicLong screenshotsCounter = new AtomicLong();
    private static final String DEFAULT_DESC = "No description";
    private final boolean screenshots;

    UiResultRenderer(boolean screenshots) {
        this.screenshots = screenshots;
    }

    public void failureReported(AssertFailureEvent event) {
        Html s = new Html(event.getElement());
        Html el = s.parent();
        UIAssertionError err = (UIAssertionError) event.getActual();
        el.remove(s);
        el.childs(
            err.getScreenshot().getImage().isEmpty()
                ? noImageOverlay(event.getExpected(), err.getMessage(), "rest-failure")
                : imageOverlay(getPath(err.getScreenshot().getImage()), 360, event.getExpected(), err.getMessage(), "rest-failure")
        );
    }

    public void successReported(AssertSuccessEvent event) {
        Html s = new Html(event.getElement());
        Html el = s.parent();
        String name = s.attrOrFail("name");
        String desc = s.attr("desc");
        el.remove(s);
        el.childs(
            screenshots
                ? imageOverlay(getPath(screenshot(getFileName(name))), 360, name, checkAndGetDesc(desc), "rest-success")
                : noImageOverlay(name, checkAndGetDesc(desc), "rest-success")
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