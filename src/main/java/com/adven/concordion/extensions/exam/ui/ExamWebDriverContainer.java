package com.adven.concordion.extensions.exam.ui;

import com.codeborne.selenide.impl.WebDriverThreadLocalContainer;
import org.openqa.selenium.WebDriver;

/**
 * Extension for access protected methods.
 * Because, if you create webdriver manually, you should close it.
 */
public class ExamWebDriverContainer extends WebDriverThreadLocalContainer {

    /**
     * Register driver for automatically close.
     *
     * @param driver web driver
     */
    public void registerDriverForAutoClose(WebDriver driver) {
        setWebDriver(driver);
        markForAutoClose(driver);
    }
}
