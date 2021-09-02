package specs.core

import org.concordion.api.FullOGNL
import specs.Specs

class CoreDecor : Specs()
class CoreSet : Specs()

@FullOGNL
class CoreVerify : Specs() {
    fun lowercase(name: String): Result = name.lowercase().let {
        Result(it, """{ "result": "$it" }""", """<result>$it</result>""")
    }

    data class Result(val text: String, val json: String, val xml: String)
}
