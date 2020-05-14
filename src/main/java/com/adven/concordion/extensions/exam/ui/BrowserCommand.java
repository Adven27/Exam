package com.adven.concordion.extensions.exam.ui;

import com.adven.concordion.extensions.exam.core.commands.ExamVerifyCommand;
import com.adven.concordion.extensions.exam.core.html.Html;
import com.codeborne.selenide.Configuration;
import com.codeborne.selenide.WebDriverRunner;
import com.codeborne.selenide.ex.UIAssertionError;
import org.concordion.api.CommandCall;
import org.concordion.api.Evaluator;
import org.concordion.api.ResultRecorder;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.remote.DesiredCapabilities;

import java.io.File;

import static com.codeborne.selenide.Selenide.open;

public class BrowserCommand extends ExamVerifyCommand {
    private static final String FAIL_FAST = "failFast";
    private static final String URL = "url";
    private static boolean alreadyRunning = false;
    private static ExamWebDriverContainer driverContainer = new ExamWebDriverContainer();
    private final DesiredCapabilities capabilities;
    private String url;
    private boolean failFast;
    private String originalSelenideReportsFolder;

    public BrowserCommand(String tag, boolean screenshots, DesiredCapabilities capabilities) {
        super("browser", tag, new UiResultRenderer(screenshots));
        this.capabilities = capabilities;
    }

    private static void saveScreenshotsTo(String path) {
        Configuration.reportsFolder = path;
    }

    @Override
    public void setUp(CommandCall commandCall, Evaluator evaluator, ResultRecorder resultRecorder) {
        Html root = new Html(commandCall.getElement());
        url = attr(root, URL, "/", evaluator);
        failFast = Boolean.valueOf(root.takeAwayAttr(FAIL_FAST, "true"));
    }

    @Override
    public void execute(CommandCall commandCall, Evaluator evaluator, ResultRecorder resultRecorder) {
        originalSelenideReportsFolder = Configuration.reportsFolder;
        saveScreenshotsTo(currentFolder(commandCall));

        runCustomDriverIfSet(capabilities);

        open(url);
        Html root = new Html(commandCall.getElement()).css("card-group");
        evalSteps(root, evaluator, resultRecorder);
        saveScreenshotsTo(originalSelenideReportsFolder);
    }

    private static void runCustomDriverIfSet(DesiredCapabilities capabilities) {
        if (!(capabilities == null || alreadyRunning)) {
            ChromeDriver webDriver = new ChromeDriver(capabilities);
            WebDriverRunner.setWebDriver(webDriver);
            driverContainer.registerDriverForAutoClose(webDriver);
            alreadyRunning = true;
        }
    }

    private String currentFolder(CommandCall commandCall) {
        String outputPath = System.getProperty("concordion.output.dir");
        if (outputPath == null) {
            outputPath =  new File(System.getProperty("java.io.tmpdir"), "concordion").getPath();
        }
        return outputPath + commandCall.getResource().getParent().getPath();
    }

    private void evalSteps(Html el, Evaluator evaluator, ResultRecorder resultRecorder) {
        boolean failed = false;
        for (Html s : el.childs()) {
            if ("step".equals(s.localName())) {
                if (failed) {
                    el.remove(s);
                } else if (!eval(evaluator, resultRecorder, s)) {
                    failed = true;
                }
            }
        }
    }

    private boolean eval(Evaluator ev, ResultRecorder resultRecorder, Html el) {
        final String name = el.attr("name");
        final String var = el.attr("set");
        String exp = name + "()";
        String text = el.text();
        if (!"".equals(text)) {
            exp = name + "(#TEXT)";
            ev.setVariable("#TEXT", text);
        }
        try {
            Object res = ev.evaluate(exp);
            if (var != null) {
                ev.setVariable("#" + var, res);
            }
            success(resultRecorder, el);
        } catch (Throwable e) {
            Throwable cause = e.getCause();
            if (cause instanceof UIAssertionError) {
                UIAssertionError err = (UIAssertionError) cause;
                failure(resultRecorder, el, err, name);
                if (failFast) {
                    return false;
                }
            }
        }
        return true;
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