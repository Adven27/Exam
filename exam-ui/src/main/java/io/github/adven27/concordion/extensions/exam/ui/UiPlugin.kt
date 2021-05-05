package io.github.adven27.concordion.extensions.exam.ui

import com.codeborne.selenide.Configuration
import com.codeborne.selenide.WebDriverRunner
import com.codeborne.selenide.WebDriverRunner.FIREFOX
import com.codeborne.selenide.WebDriverRunner.INTERNET_EXPLORER
import io.github.adven27.concordion.extensions.exam.core.ExamPlugin
import io.github.adven27.concordion.extensions.exam.core.commands.ExamCommand
import io.github.bonigarcia.wdm.WebDriverManager
import io.github.bonigarcia.wdm.config.DriverManagerType
import org.openqa.selenium.chrome.ChromeOptions
import org.openqa.selenium.chrome.ChromeOptions.CAPABILITY
import org.openqa.selenium.remote.DesiredCapabilities

@Suppress("LongParameterList")
open class UiPlugin @JvmOverloads constructor(
    timeout: Long = Configuration.timeout,
    browser: String = WebDriverRunner.CHROME,
    version: String? = null,
    baseUrl: String = Configuration.baseUrl,
    headless: Boolean = true,
    private val screenshotsOnSuccess: Boolean = true,
    screenshotsOnFail: Boolean = true,
    private var capabilities: DesiredCapabilities? = null
) : ExamPlugin.NoSetUp() {

    companion object {
        private var webDriverInited: Boolean = false
    }

    init {
        if (!webDriverInited) {
            Configuration.screenshots = screenshotsOnFail
            Configuration.timeout = timeout
            Configuration.baseUrl = baseUrl
            Configuration.browser = browser
            Configuration.holdBrowserOpen = false
            Configuration.browserSize = "1680x1050"
            when (browser) {
                FIREFOX -> DriverManagerType.FIREFOX.setup(version)
                INTERNET_EXPLORER -> DriverManagerType.IEXPLORER.setup(version)
                else -> DriverManagerType.CHROME.setup(version).apply {
                    if (headless) {
                        capabilities = headlessCapabilities()
                    }
                }
            }
            webDriverInited = true
        }
    }

    private fun DriverManagerType.setup(version: String?) =
        WebDriverManager.getInstance(this).driverVersion(version).setup()

    private fun headlessCapabilities() = DesiredCapabilities(
        mapOf(
            CAPABILITY to ChromeOptions().apply {
                addArguments("no-sandbox", "headless", "disable-gpu", "disable-extensions", "window-size=1366,768")
            }
        )
    )

    override fun commands(): List<ExamCommand> = listOf(BrowserCommand("div", screenshotsOnSuccess, capabilities))
}
