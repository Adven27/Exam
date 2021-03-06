package specs.xj

import specs.Specs
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class Xj : Specs() {
    val actualJson: String
        get() = """{"date" : "$NOW"}"""

    val actualXml: String
        get() = "<date>$NOW</date>"

    val actualBigXml: String
        get() = "<root>${(1..50).map { "<num>$it</num>" }.reduce { it, acc -> acc + it }}</root>"

    val actualJsonWithFieldsToIgnore: String
        get() = """{ 
            |"param1":"1", 
            |"extra":"ignore", 
            |"arr": [{"param2":"2", "extra":"ignore"}, {"extra":"ignore", "param3":"3"}]
            |}""".trimMargin()

    companion object {
        private val NOW = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
    }
}
