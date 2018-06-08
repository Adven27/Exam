package com.adven.concordion.extensions.exam.html

import org.concordion.api.Evaluator
import org.joda.time.LocalDate.now
import org.joda.time.Period
import org.junit.Test
import org.mockito.Mockito.mock
import java.text.SimpleDateFormat
import kotlin.test.assertEquals

class RowParserTest {
    private val eval = mock(Evaluator::class.java)

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
            Html("row", " \${exam.now+[1 day, 2 month]:dd.MM},' 4, 5 '")
        )

        val expectedPeriod = Period().plusDays(1).plusMonths(2)
        val expectedDate = SimpleDateFormat("dd.MM").format(now().plus(expectedPeriod).toDate())
        assertEquals(listOf(
            listOf("1'", " 2"),
            listOf(expectedDate, " 4, 5 ")
        ), RowParser(html, "row", eval).parse())
    }
}