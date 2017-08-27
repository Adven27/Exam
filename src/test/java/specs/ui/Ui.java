package specs.ui;

import org.concordion.api.Unimplemented;
import org.junit.Test;
import org.openqa.selenium.By;
import specs.Specs;

import static com.codeborne.selenide.Condition.exist;
import static com.codeborne.selenide.Condition.text;
import static com.codeborne.selenide.Selenide.$;

public class Ui extends Specs {

    public void hasText(String text) throws Exception {
        $(By.tagName("span")).shouldHave(text(text));
    }

    @Test
    public void noParamsCheck() throws Exception {
        $(By.tagName("span")).should(exist);
    }
}