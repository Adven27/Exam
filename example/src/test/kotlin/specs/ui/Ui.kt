package specs.ui

import com.codeborne.selenide.Condition.exist
import com.codeborne.selenide.Condition.text
import com.codeborne.selenide.Selenide.`$`
import org.openqa.selenium.By
import specs.Specs

class UiPlugin : Specs()
class Ui : Specs() {
    fun hasParagraphText(text: String) {
        `$`(By.tagName("p")).shouldHave(text(text))
    }

    fun noParamsCheck(): String {
        `$`(By.tagName("p")).should(exist)
        return "valueFromMethodCall"
    }

    fun areEqual(s1: String, s2: String): Boolean {
        return s1 == s2
    }
}
