package com.adven.concordion.extensions.exam.configurators;

import com.adven.concordion.extensions.exam.ExamExtension;
import com.codeborne.selenide.Configuration;
import com.codeborne.selenide.WebDriverRunner;
import io.github.bonigarcia.wdm.ChromeDriverManager;
import io.github.bonigarcia.wdm.FirefoxDriverManager;
import io.github.bonigarcia.wdm.InternetExplorerDriverManager;

public class WebDriverCfg {
    private static boolean webDriverInited = false;
    private final ExamExtension extension;
    private Long timeout;
    private String browser;
    private String baseUrl;
    private String version;

    public WebDriverCfg(ExamExtension extension) {
        this.extension = extension;
    }

    private static void setUp(Long timeout, String browser, String version, String baseUrl) {
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
            }
            webDriverInited = true;
        }
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

    public ExamExtension end() {
        setUp(timeout, browser, version, baseUrl);
        return extension;
    }
}
