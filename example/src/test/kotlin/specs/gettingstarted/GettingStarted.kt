package specs.gettingstarted

import io.restassured.RestAssured.get
import org.concordion.api.FullOGNL
import specs.Specs

@FullOGNL
class GettingStarted : Specs() {
    fun isDone(id: String) = !get("/jobs/$id").body().jsonPath().getBoolean("running")
}
