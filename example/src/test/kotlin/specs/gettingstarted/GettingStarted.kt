package specs.gettingstarted

import app.start
import app.stop
import io.restassured.RestAssured
import io.restassured.RestAssured.get
import org.concordion.api.AfterSpecification
import org.concordion.api.BeforeSpecification
import org.concordion.api.FullOGNL
import specs.Specs

@FullOGNL
class GettingStarted : Specs() {
    fun isDone(id: String): Boolean {
        return !get("/jobs/$id").body().jsonPath().getBoolean("running")
    }

    companion object {
        @JvmStatic
        @BeforeSpecification
        fun setUp() {
            RestAssured.port = 8080
            start()
        }

        @JvmStatic
        @AfterSpecification
        fun tearDown() {
            stop()
            RestAssured.port = PORT
        }
    }
}
