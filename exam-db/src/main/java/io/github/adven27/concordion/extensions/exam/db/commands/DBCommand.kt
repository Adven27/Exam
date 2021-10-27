@file:Suppress("TooManyFunctions")

package io.github.adven27.concordion.extensions.exam.db.commands

import io.github.adven27.concordion.extensions.exam.core.html.CLASS
import io.github.adven27.concordion.extensions.exam.core.html.Html
import io.github.adven27.concordion.extensions.exam.core.html.attr
import io.github.adven27.concordion.extensions.exam.core.html.caption
import io.github.adven27.concordion.extensions.exam.core.html.div
import io.github.adven27.concordion.extensions.exam.core.html.italic
import io.github.adven27.concordion.extensions.exam.core.html.table
import io.github.adven27.concordion.extensions.exam.core.html.tbody
import io.github.adven27.concordion.extensions.exam.core.html.td
import io.github.adven27.concordion.extensions.exam.core.html.th
import io.github.adven27.concordion.extensions.exam.core.html.thead
import io.github.adven27.concordion.extensions.exam.core.html.tr
import io.github.adven27.concordion.extensions.exam.db.DbPlugin
import io.github.adven27.concordion.extensions.exam.db.builder.SeedStrategy
import org.concordion.api.CommandCall
import org.dbunit.dataset.Column
import org.dbunit.dataset.ITable
import org.dbunit.dataset.filter.DefaultColumnFilter

fun ITable.tableName(): String = this.tableMetaData.tableName
fun ITable.columns(): Array<Column> = this.tableMetaData.columns
fun ITable.columnNames() = this.tableMetaData.columns.map { it.columnName }
fun ITable.columnNamesArray() = this.columnNames().toTypedArray()
fun ITable.columnsSortedBy(sort: (o1: String, o2: String) -> Int) = columnNames().sortedWith(Comparator(sort))

fun ITable.withColumnsAsIn(expected: ITable): ITable =
    DefaultColumnFilter.includedColumnsTable(this, expected.columns())

operator fun ITable.get(row: Int, col: String): Any? = this.getValue(row, col)
operator fun ITable.get(row: Int, col: Column): Any? = this[row, col.columnName]

fun <R> ITable.mapRows(transform: (Int) -> R): List<R> = (0 until this.rowCount).map(transform)

fun renderTable(
    t: ITable,
    valuePrinter: DbPlugin.ValuePrinter,
    caption: String? = null,
    remarks: Map<String, Int> = emptyMap()
): Html =
    renderTable(
        t,
        { td, row, col -> td()(Html(valuePrinter.wrap(t[row, col]))) },
        caption,
        { col: String -> if (remarks.containsKey(col)) "table-info" else "" },
        { col1, col2 -> -compareValues(remarks[col1], remarks[col2]) }
    )

@Suppress("LongParameterList", "SpreadOperator")
fun renderTable(
    t: ITable,
    cell: (Html, Int, String) -> Html,
    caption: String? = null,
    styleCol: (String) -> String = { "" },
    sortCols: (col1: String, col2: String) -> Int = { _, _ -> 0 },
    ifEmpty: Html.() -> Unit = { }
): Html {
    val cols = t.columnsSortedBy(sortCols)
    return div("class" to "table-responsive mb-1 p-1")(
        table()(
            tableCaption(caption, t.tableName()),
            if (t.empty()) thead()(tr()(th())) else thead()(tr()(cols.map { th(it, CLASS to styleCol(it)) })),
            tbody()(
                if (t.empty()) {
                    listOf(tr()(td("EMPTY").attrs("colspan" to "${cols.size}").apply(ifEmpty)))
                } else {
                    t.mapRows { row -> cols.map { cell(td(CLASS to styleCol(it)), row, it) } }
                        .map { tr()(*it.toTypedArray()) }
                }
            )
        )
    )
}

private fun ITable.empty() = this.rowCount == 0

fun tableCaption(title: String?, def: String?): Html = caption()
    .style("width:max-content")(italic(" ", CLASS to "fa fa-database me-1"))
    .text("  ${if (!title.isNullOrBlank()) title else def}")

data class SetAttrs(val seedStrategy: SeedStrategy) {
    companion object {
        private const val OPERATION = "operation"

        fun from(root: CommandCall, allowedSeedStrategies: List<SeedStrategy>) = SetAttrs(
            SeedStrategy.valueOf(root.attr(OPERATION, SeedStrategy.CLEAN_INSERT.name).uppercase())
                .isAllowed(allowedSeedStrategies),
        )

        private fun SeedStrategy.isAllowed(allowed: List<SeedStrategy>): SeedStrategy =
            allowed.find { it == this }
                ?: throw IllegalArgumentException("Forbidden seed strategy $this. Allowed strategies: $allowed")
    }
}
