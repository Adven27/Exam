package specs.xj

import org.joda.time.LocalDate
import specs.Specs

class Xj : Specs() {
    val actualJson: String
        get() = """{"date" : "$NOW"}"""

    val actualXml: String
        get() = "<date>$NOW</date>"

    val actualJsonWithFieldsToIgnore: String
        get() = """{ 
            |"param1":"1", 
            |"extra":"ignore", 
            |"arr": [{"param2":"2", "extra":"ignore"}, {"extra":"ignore", "param3":"3"}]
            |}""".trimMargin()

    companion object {
        private val NOW = LocalDate.now().toString("yyyy-MM-dd")
    }
}