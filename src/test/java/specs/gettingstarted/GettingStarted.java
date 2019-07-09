package specs.gettingstarted;

import app.SutApp;
import io.restassured.RestAssured;
import org.concordion.api.AfterSpecification;
import org.concordion.api.BeforeSpecification;
import org.concordion.api.FullOGNL;
import specs.Specs;

@FullOGNL
public class GettingStarted extends Specs {

    @BeforeSpecification
    public static void setUp() {
        RestAssured.port = 8080;
        SutApp.start();
    }

    @AfterSpecification
    public static void tearDown() {
        SutApp.stop();
        RestAssured.port = Specs.PORT;
    }
}
