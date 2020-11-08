package specs.ui;

import org.openqa.selenium.By;
import specs.Specs;

import static com.codeborne.selenide.Condition.exist;
import static com.codeborne.selenide.Condition.text;
import static com.codeborne.selenide.Selenide.$;

public class Ui extends Specs {

    public void hasText(String text) {
        $(By.tagName("span")).shouldHave(text(text));
    }

    public String noParamsCheck() {
        $(By.tagName("span")).should(exist);
        return "valueFromMethodCall";
    }

    public boolean areEqual(String s1, String s2) {
        return s1.equals(s2);
    }
}