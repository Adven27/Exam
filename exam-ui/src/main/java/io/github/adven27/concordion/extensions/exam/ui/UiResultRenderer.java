package io.github.adven27.concordion.extensions.exam.ui;

import com.codeborne.selenide.ex.UIAssertionError;
import io.github.adven27.concordion.extensions.exam.core.html.Html;
import org.concordion.api.listener.AssertEqualsListener;
import org.concordion.api.listener.AssertFailureEvent;
import org.concordion.api.listener.AssertFalseListener;
import org.concordion.api.listener.AssertSuccessEvent;
import org.concordion.api.listener.AssertTrueListener;

import java.io.File;
import java.util.concurrent.atomic.AtomicLong;

import static com.codeborne.selenide.Selenide.screenshot;
import static io.github.adven27.concordion.extensions.exam.core.html.HtmlBuilder.codeHighlight;
import static io.github.adven27.concordion.extensions.exam.core.html.HtmlBuilder.imageOverlay;
import static io.github.adven27.concordion.extensions.exam.core.html.HtmlBuilder.noImageOverlay;
import static io.github.adven27.concordion.extensions.exam.core.html.HtmlBuilder.paragraph;

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
        Fail fail = new Fail(event.getActual());
        el.remove(s);
        Html desc = codeHighlight(fail.message, "bash");
        el.childs(
            fail.screenshot.isEmpty()
                ? noImageOverlay(event.getExpected(), desc, true)
                : imageOverlay(getPath(fail.screenshot), 360, event.getExpected(), desc, true)
        );
    }

    public void successReported(AssertSuccessEvent event) {
        Html s = new Html(event.getElement());
        Html el = s.parent();
        String name = s.attrOrFail("name");
        String desc = s.attr("desc");
        el.remove(s);
        Html paragraph = paragraph(checkAndGetDesc(desc)).css("card-text small");
        el.childs(
            screenshots
                ? imageOverlay(getPath(screenshot(getFileName(name))), 360, name, paragraph, false)
                : noImageOverlay(name, paragraph, false)
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

    static class Fail {
        final String screenshot;
        final String message;

        public Fail(Object actual) {
            if (actual instanceof Throwable) {
                Throwable t = (Throwable) actual;
                if (t.getCause() instanceof UIAssertionError) {
                    UIAssertionError uiAssertionError = (UIAssertionError) t.getCause();
                    message = uiAssertionError.getMessage();
                    screenshot = uiAssertionError.getScreenshot().getImage();
                } else {
                    message = t.getMessage();
                    screenshot = "";
                }
            } else {
                message = "Actual is not Throwable";
                screenshot = "";
            }
        }
    }
}