package com.adven.concordion.extensions.exam.core.html

import org.concordion.internal.OgnlEvaluator
import org.joda.time.LocalDate.now
import org.joda.time.Period
import org.junit.Test
import java.text.SimpleDateFormat
import kotlin.test.assertEquals

class RowParserTest {
    private val eval = OgnlEvaluator(null)

    @Test
    fun valuesAreCommaSeparatedAndTrimmed() {
        val html = table()(
            Html("row", " 1 1 , 2"),
            Html("row", " 3 , 4")
        )

        assertEquals(listOf(listOf("1 1", "2"), listOf("3", "4")), RowParser(html, "row", eval).parse())
    }

    @Test
    fun spacesCommasAndExpressionsInsideCells() {
        val html = Html("table")(
            Html("row", " 1',' 2 "),
            Html("row", " {{now 'dd.MM' plus='1 day'}},' 4, 5 '")
        )

        val expectedPeriod = Period().plusDays(1)
        val expectedDate = SimpleDateFormat("dd.MM").format(now().plus(expectedPeriod).toDate())
        assertEquals(listOf(
            listOf("1'", " 2"),
            listOf(expectedDate, " 4, 5 ")
        ), RowParser(html, "row", eval).parse())
    }

    @Test
    fun resolveOneOrMorePlaceholdersToString() {
        val html = Html("table")(
            Html("row", "{{resolve \"{{now 'dd'}} {{now 'MM'}}\"}}, {{resolve '{{today}}'}}")
        )
        val dd = SimpleDateFormat("dd").format(now().toDate())
        val mm = SimpleDateFormat("MM").format(now().toDate())

        assertEquals(listOf(listOf("$dd $mm", now().toDate().toString())), RowParser(html, "row", eval).parse())
    }
}