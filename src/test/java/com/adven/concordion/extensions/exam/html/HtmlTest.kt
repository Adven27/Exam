package com.adven.concordion.extensions.exam.html

import org.junit.Test
import org.xmlunit.builder.DiffBuilder
import org.xmlunit.diff.Diff
import kotlin.test.assertFalse

class HtmlTest {

    @Test
    fun htmlDSLExample() {
        val cols = 1..2
        val diff = diff(
            div(NAME to "div", CLASS to "div-class")(
                table(NAME to "table", "custom-attr" to "some-value")(
                    thead()(
                        tr()(
                            cols.map {
                                th(it.toString())
                            })),
                    tbody(NAME to "tbody")(
                        (1..3).map { row ->
                            tr()(
                                cols.map {
                                    td(STYLE to "color:red")(
                                        span("$row-$it", CLASS to "span-in-cell"))
                                })
                        }))),
            """|<div name="div" class="div-class">
               |    <table name="table" class="table" custom-attr="some-value">
               |        <thead class="thead-default">
               |        <tr>
               |            <th>1</th>
               |            <th>2</th>
               |        </tr>
               |        </thead>
               |        <tbody name="tbody">
               |        <tr>
               |            <td style="color:red"><span class="span-in-cell">1-1</span></td>
               |            <td style="color:red"><span class="span-in-cell">1-2</span></td>
               |        </tr>
               |        <tr>
               |            <td style="color:red"><span class="span-in-cell">2-1</span></td>
               |            <td style="color:red"><span class="span-in-cell">2-2</span></td>
               |        </tr>
               |        <tr>
               |            <td style="color:red"><span class="span-in-cell">3-1</span></td>
               |            <td style="color:red"><span class="span-in-cell">3-2</span></td>
               |        </tr>
               |        </tbody>
               |    </table>
               |</div>
            """.trimMargin())
        assertFalse(diff.hasDifferences(), diff.toString())
    }

    private fun diff(actual: Html, expected: String): Diff {
        return DiffBuilder.compare(actual.el.toXML())
            .checkForIdentical()
            .withTest(expected)
            .ignoreComments()
            .ignoreWhitespace().build()
    }
}