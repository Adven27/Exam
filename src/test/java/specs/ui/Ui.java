package specs.ui;

import com.codeborne.selenide.Configuration;
import io.github.bonigarcia.wdm.ChromeDriverManager;
import org.concordion.api.Unimplemented;
import org.junit.BeforeClass;
import specs.Specs;

import static com.codeborne.selenide.Selenide.open;

@Unimplemented
public class Ui extends Specs {
    @BeforeClass
    public static void setupClass() {
        Configuration.browser = "chrome";
        ChromeDriverManager.getInstance().version("2.29").setup();
    }

    public void name() throws Exception {
        open("http://localhost:8080/");
    }
}