package specs.core.handlebar

import io.github.adven27.concordion.extensions.exam.core.handlebars.date.DateHelpers
import io.github.adven27.concordion.extensions.exam.core.handlebars.matchers.ISO_LOCAL_DATETIME_FORMAT
import io.github.adven27.concordion.extensions.exam.core.handlebars.matchers.ISO_LOCAL_DATE_FORMAT
import io.github.adven27.concordion.extensions.exam.core.handlebars.matchers.MatcherHelpers
import io.github.adven27.concordion.extensions.exam.core.handlebars.misc.MiscHelpers
import io.github.adven27.concordion.extensions.exam.core.toString
import specs.Specs
import java.util.Date

class Handlebar : Specs() {
    companion object {
        val JSON_DATE = Date().let {
            Triple(
                it.toString("yyyy/MM/dd'T'HH:mm.ss"),
                it.toString(ISO_LOCAL_DATE_FORMAT),
                it.toString(ISO_LOCAL_DATETIME_FORMAT)
            )
        }.let { (custom, date, datetime) -> // language=json
            """
            { 
              "formattedAs": "$custom",
              "isoLocalDate": "$date",
              "isoLocalDateTime": "$datetime",
              
              "formattedAndWithinNow": "$custom",
              "isoLocalDateAndWithinNow": "$date",
              "isoLocalDateTimeAndWithinNow": "$datetime",
              
              "formattedAndWithin": "$custom",
              "isoLocalDateAndWithin": "$date",
              "isoLocalDateTimeAndWithin": "$datetime",
              
              "after": "$datetime",
              "before": "$datetime"
            }
        """.trimIndent()
        }
    }

    val givenDateJson: String = JSON_DATE
    val givenDataJson: String = // language=json
        """
          {
            "string": "some string",
            "number": 123,
            "bool": true,
            "ignore": "anything 123",
            "regex": "123"
          }
        """.trimIndent()
    val dateHelpers: String = DateHelpers.values().joinToString("\n") { it.describe() }
    val matcherHelpers: String = MatcherHelpers.values().joinToString("\n") { it.describe() }
    val miscHelpers: String = MiscHelpers.values().joinToString("\n") { it.describe() }
}
