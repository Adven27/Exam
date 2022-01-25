package io.github.adven27.concordion.extensions.exam.ui

import com.codeborne.selenide.Browsers.CHROME
import com.codeborne.selenide.Browsers.FIREFOX
import com.codeborne.selenide.Browsers.INTERNET_EXPLORER
import com.codeborne.selenide.Configuration
import io.github.adven27.concordion.extensions.exam.core.ExamPlugin
import io.github.adven27.concordion.extensions.exam.core.commands.ExamCommand
import io.github.bonigarcia.wdm.WebDriverManager
import io.github.bonigarcia.wdm.config.DriverManagerType
import org.openqa.selenium.chrome.ChromeOptions
import org.openqa.selenium.chrome.ChromeOptions.CAPABILITY
import org.openqa.selenium.remote.DesiredCapabilities
import java.time.Duration

open class UiPlugin @JvmOverloads constructor(
    baseUrl: String = Configuration.baseUrl,
    private val browser: Browser = Browser(),
    private val screenshots: Screenshots = Screenshots.ALWAYS,
    timeout: Duration = Duration.ofMillis(Configuration.timeout)
) : ExamPlugin.NoSetUp(), ExamPlugin {

    data class Browser @JvmOverloads constructor(
        val name: String = CHROME,
        val version: String? = null,
        var capabilities: DesiredCapabilities? = null,
        val headless: Boolean = true
    )

    data class Screenshots @JvmOverloads constructor(val onSuccess: Boolean = true, val onFail: Boolean = true) {
        companion object {
            @JvmField
            val ON_SUCCESS_ONLY: Screenshots = Screenshots(onSuccess = true, onFail = false)

            @JvmField
            val ON_FAIL_ONLY: Screenshots = Screenshots(onSuccess = false, onFail = true)

            @JvmField
            val ALWAYS: Screenshots = Screenshots(onSuccess = true, onFail = true)
        }
    }

    companion object {
        private var webDriverInited: Boolean = false
    }

    init {
        if (!webDriverInited) {
            Configuration.screenshots = screenshots.onFail
            Configuration.timeout = timeout.toMillis()
            Configuration.baseUrl = baseUrl
            Configuration.browser = browser.name
            Configuration.holdBrowserOpen = false
            Configuration.browserSize = "1680x1050"
            when (browser.name) {
                FIREFOX -> DriverManagerType.FIREFOX.setup(browser.version)
                INTERNET_EXPLORER -> DriverManagerType.IEXPLORER.setup(browser.version)
                else -> DriverManagerType.CHROME.setup(browser.version).apply {
                    if (browser.headless) {
                        browser.capabilities = headlessCapabilities()
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

    override fun commands(): List<ExamCommand> =
        listOf(BrowserCommand("div", screenshots.onSuccess, browser.capabilities))
}
