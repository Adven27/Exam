package io.github.adven27.concordion.extensions.exam.db.commands.check

import io.github.adven27.concordion.extensions.exam.core.commands.SuitableResultRenderer
import io.github.adven27.concordion.extensions.exam.core.commands.VerifyFailureEvent
import io.github.adven27.concordion.extensions.exam.core.commands.VerifySuccessEvent
import io.github.adven27.concordion.extensions.exam.core.errorMessage
import io.github.adven27.concordion.extensions.exam.core.html.Html
import io.github.adven27.concordion.extensions.exam.core.html.div
import io.github.adven27.concordion.extensions.exam.core.html.span
import io.github.adven27.concordion.extensions.exam.core.rootCauseMessage
import io.github.adven27.concordion.extensions.exam.db.DbPlugin.ValuePrinter
import io.github.adven27.concordion.extensions.exam.db.commands.ExamMatchersAwareValueComparer.Companion.isDbMatcher
import io.github.adven27.concordion.extensions.exam.db.commands.check.CheckCommand.Expected
import io.github.adven27.concordion.extensions.exam.db.commands.check.DbVerifier.TableContentMismatch
import io.github.adven27.concordion.extensions.exam.db.commands.check.DbVerifier.TableSizeMismatch
import io.github.adven27.concordion.extensions.exam.db.commands.get
import io.github.adven27.concordion.extensions.exam.db.commands.renderTable
import org.concordion.api.Element
import org.concordion.api.listener.AbstractElementEvent
import org.dbunit.assertion.Difference
import org.dbunit.dataset.ITable

class MdResultRenderer(printer: ValuePrinter) : BaseResultRenderer(printer) {
    override fun isSuitFor(element: Element) = element.localName != "div"
    override fun root(event: AbstractElementEvent): Element = event.element.parentElement.parentElement
}

class HtmlResultRenderer(printer: ValuePrinter) : BaseResultRenderer(printer) {
    override fun isSuitFor(element: Element) = element.localName == "div"
    override fun root(event: AbstractElementEvent): Element = event.element
}

abstract class BaseResultRenderer(private val printer: ValuePrinter) : SuitableResultRenderer<Expected, ITable>() {
    abstract fun root(event: AbstractElementEvent): Element

    override fun successReported(event: VerifySuccessEvent<Expected, ITable>) = with(root(event)) {
        appendSister(
            renderTable(
                event.expected.table,
                { td, row, col -> td.success(event.expected.table[row, col], event.actual[row, col]) },
                caption = event.expected.caption,
                ifEmpty = { css("table-success") }
            ).el
        )
        parentElement.removeChild(this)
    }

    private fun Html.success(expected: Any?, actual: Any?): Html = this(
        Html(printer.wrap(expected)).tooltip(printer.print(actual), expected.isDbMatcher())
    ).css("table-success")

    override fun failureReported(event: VerifyFailureEvent<Expected>) = with(root(event)) {
        appendSister(renderFail(event))
        parentElement.removeChild(this)
    }

    private fun renderFail(event: VerifyFailureEvent<Expected>) = when (event.fail) {
        is TableSizeMismatch -> renderSizeMismatch(event.fail as TableSizeMismatch, event.expected)
        is TableContentMismatch -> renderContentMismatch(event.fail as TableContentMismatch, event.expected)
        else -> renderUnknownError(event.fail, event.expected)
    }

    private fun renderSizeMismatch(fail: TableSizeMismatch, expected: Expected) = errorTemplate(
        expected, fail, butWas = renderTable(fail.actual, printer)
    )

    private fun renderContentMismatch(fail: TableContentMismatch, expected: Expected) = errorTemplate(
        expected,
        fail,
        renderTable(
            expected.table,
            cellFailure(expected.table, fail.diff.first().actualTable, fail.diff),
            expected.caption
        )
    )

    private fun renderUnknownError(fail: Throwable, expected: Expected) = errorTemplate(expected, fail)

    private fun errorTemplate(
        expected: Expected,
        fail: Throwable,
        expectedTable: Html = renderTable(expected.table, printer, expected.caption),
        butWas: Html? = null
    ) = errorMessage(
        message = expected.await?.timeoutMessage(fail) ?: fail.rootCauseMessage(),
        type = "json",
        html = div()(
            span("Expected:"),
            expectedTable,
            if (butWas != null) span("but was:") else null,
            butWas,
        )
    ).second.el

    private fun cellFailure(expected: ITable, actual: ITable, diff: List<Difference>): (Html, Int, String) -> Html =
        { td, row, col -> diff[row, col]?.let { td.diff(it) } ?: td.success(expected[row, col], actual[row, col]) }

    private fun Html.diff(it: Difference) = this(
        Html("del", printer.print(it.expectedValue), "class" to "me-1"),
        Html("ins", printer.print(it.actualValue))
    ).css("table-danger").tooltip(it.failMessage)

    private operator fun List<Difference>.get(row: Int, col: String) =
        singleOrNull { it.rowIndex == row && it.columnName.equals(col, ignoreCase = true) }
}
