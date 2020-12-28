package com.adven.concordion.extensions.exam.core.html

import com.adven.concordion.extensions.exam.core.toDate
import org.concordion.internal.FixtureInstance
import org.concordion.internal.OgnlEvaluator
import org.junit.Test
import java.text.SimpleDateFormat
import java.time.LocalDateTime.now
import java.time.Period
import kotlin.test.assertEquals

class RowParserTest {
    private val eval = OgnlEvaluator(FixtureInstance(Object()))

    @Test
    fun valuesAreCommaSeparatedAndTrimmed() {
        val html = table()(
            Html("row", " 1 1 , 2"),
            Html("row", " 3 , 4")
        )

        assertEquals(
            mapOf("1" to listOf("1 1", "2"), "2" to listOf("3", "4")),
            RowParserEval(html, "row", eval).parse()
        )
    }

    @Test
    fun spacesCommasAndExpressionsInsideCells() {
        val html = Html("table")(
            Html("row", " 1',' 2 "),
            Html("row", " {{now 'dd.MM' plus='1 day'}},' 4, 5 '")
        )

        val expectedPeriod = Period.ofDays(1)
        val expectedDate = SimpleDateFormat("dd.MM").format(now().plus(expectedPeriod).toDate())
        assertEquals(
            mapOf(
                "1" to listOf("1'", " 2"),
                "2" to listOf(expectedDate, " 4, 5 ")
            ),
            RowParserEval(html, "row", eval).parse()
        )
    }

    @Test
    fun resolveOneOrMorePlaceholdersToString() {
        val html = Html("table")(
            Html("row", "{{resolve \"{{now 'dd'}} {{now 'MM'}}\"}}, {{resolve '{{today}}'}}")
        )
        val dd = SimpleDateFormat("dd").format(now().toDate())
        val mm = SimpleDateFormat("MM").format(now().toDate())

        assertEquals(
            mapOf("1" to listOf("$dd $mm", now().toLocalDate().toDate().toString())),
            RowParserEval(html, "row", eval).parse()
        )
    }
}
