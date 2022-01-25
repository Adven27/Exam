import com.codeborne.selenide.Browsers;
import io.github.adven27.concordion.extensions.exam.ui.UiPlugin;

import java.time.Duration;

public class Specs extends AbstractSpecs {
    @Override
    protected ExamExtension init() {
        return new ExamExtension(
            new UiPlugin(
                " http://localhost:8080",      // SUT base url
                new UiPlugin.Browser(Browsers.CHROME), // Browser specification
                UiPlugin.Screenshots.ALWAYS,           // Screenchots specifications
                Duration.ofSeconds(4)                  // Timeout to fail the test, if conditions still not me
            )
        );
    }
}