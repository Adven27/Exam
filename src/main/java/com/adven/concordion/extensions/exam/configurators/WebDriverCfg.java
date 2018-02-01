package com.adven.concordion.extensions.exam.configurators;

import com.adven.concordion.extensions.exam.ExamExtension;
import com.codeborne.selenide.Configuration;
import com.codeborne.selenide.WebDriverRunner;
import io.github.bonigarcia.wdm.ChromeDriverManager;
import io.github.bonigarcia.wdm.FirefoxDriverManager;
import io.github.bonigarcia.wdm.InternetExplorerDriverManager;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.DesiredCapabilities;

import static java.util.Collections.singletonMap;
import static org.openqa.selenium.chrome.ChromeOptions.CAPABILITY;

public class WebDriverCfg {
    private static boolean webDriverInited = false;
    private final ExamExtension extension;
    private Long timeout;
    private String browser;
    private String baseUrl;
    private String version;
    private boolean headless;

    public WebDriverCfg(ExamExtension extension) {
        this.extension = extension;
    }

    private static void setUp(ExamExtension extension,
                              Long timeout,
                              String browser,
                              String version,
                              String baseUrl,
                              boolean headless) {
        if (!webDriverInited) {
            if (timeout != null) {
                Configuration.timeout = timeout;
            }
            if (baseUrl != null) {
                Configuration.baseUrl = baseUrl;
            }
            if (browser == null) {
                browser = WebDriverRunner.CHROME;
            }
            Configuration.browser = browser;
            switch (browser) {
                case WebDriverRunner.FIREFOX:
                    FirefoxDriverManager.getInstance().version(version).setup();
                    break;
                case WebDriverRunner.INTERNET_EXPLORER:
                    InternetExplorerDriverManager.getInstance().version(version).setup();
                    break;
                default:
                    ChromeDriverManager.getInstance().version(version).setup();
                    if (headless) {
                        setHeadlessChromeOptions(extension);
                    }
            }
            webDriverInited = true;
        }
    }

    private static void setHeadlessChromeOptions(ExamExtension extension) {
        final ChromeOptions opt = new ChromeOptions();
        opt.addArguments(
                "no-sandbox", "headless", "disable-gpu", "disable-extensions", "window-size=1366,768");
        extension.webDriverCapabilities(new DesiredCapabilities(singletonMap(CAPABILITY, opt)));
    }

    public WebDriverCfg timeout(long timeout) {
        this.timeout = timeout;
        return this;
    }

    public WebDriverCfg baseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
        return this;
    }

    public WebDriverCfg version(String version) {
        this.version = version;
        return this;
    }

    public WebDriverCfg browser(String browser) {
        this.browser = browser;
        return this;
    }

    //FIXME Only chrome is supported
    public WebDriverCfg headless() {
        this.headless = true;
        return this;
    }

    public ExamExtension end() {
        setUp(extension, timeout, browser, version, baseUrl, headless);
        return extension;
    }
}