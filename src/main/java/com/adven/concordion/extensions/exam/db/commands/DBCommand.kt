package com.adven.concordion.extensions.exam.db.commands

import com.adven.concordion.extensions.exam.core.commands.ExamCommand
import com.adven.concordion.extensions.exam.core.html.*
import com.adven.concordion.extensions.exam.db.DbPlugin
import com.adven.concordion.extensions.exam.db.DbTester
import com.adven.concordion.extensions.exam.db.MarkedHasNoDefaultValue
import com.adven.concordion.extensions.exam.db.TableData
import org.concordion.api.CommandCall
import org.concordion.api.Evaluator
import org.concordion.api.ResultRecorder
import org.dbunit.dataset.Column
import org.dbunit.dataset.ITable
import org.dbunit.dataset.filter.DefaultColumnFilter

open class DBCommand(name: String, tag: String, protected val dbTester: DbTester, var valuePrinter: DbPlugin.ValuePrinter) : ExamCommand(name, tag) {
    protected val remarks = HashMap<String, Int>()
    private val colParser = ColParser()
    protected lateinit var expectedTable: ITable

    protected var where: String? = null
    protected var orderBy: Array<String> = emptyArray()
    protected var ds: String? = null

    override fun setUp(cmd: CommandCall?, eval: Evaluator?, resultRecorder: ResultRecorder?) {
        val root = tableSlim(cmd.html())(
            div(""),
            span("")
        )
        remarks.clear()
        where = root.takeAwayAttr("where", eval!!)
        orderBy = root.takeAwayAttr("orderBy", eval)?.split(",")?.map { it.trim() }?.toTypedArray() ?: emptyArray()
        ds = root.takeAwayAttr("ds", DbTester.DEFAULT_DATASOURCE)
        val ignoreRowsBefore = root.takeAwayAttr("ignoreRowsBefore", eval)
        val ignoreRowsAfter = root.takeAwayAttr("ignoreRowsAfter", eval)
        val table = root.takeAwayAttr("table", eval)!!

        expectedTable = TableData.filled(
            table,
            DbRowParser(root, "row", ignoreRowsBefore, ignoreRowsAfter).parse(),
            parseCols(root),
            eval
        )
    }

    protected fun parseCols(el: Html): Map<String, Any?> {
        val attr = el.takeAwayAttr("cols")
        return if (attr == null) emptyMap()
        else {
            val remarkAndVal = colParser.parse(attr)
            remarks += remarkAndVal.map { it.key to it.value.first }.filter { it.second > 0 }
            remarkAndVal.mapValues { if (it.value.second == null) MarkedHasNoDefaultValue() else it.value.second }
        }
    }
}

class ColParser {
    fun parse(attr: String): Map<String, Pair<Int, String?>> {
        return attr.split(",")
            .map {
                val (r, n, v) = ("""(\**)([^=]+)=?(.*)""".toRegex()).matchEntire(it.trim())!!.destructured
                mapOf(n to (r.length to (if (v.isBlank()) null else v)))
            }
            .reduce { acc, next -> acc + next }
    }
}

fun ITable.tableName(): String = this.tableMetaData.tableName
fun ITable.columns() = this.tableMetaData.columns
fun ITable.columnNames() = this.tableMetaData.columns.map { it.columnName }
fun ITable.columnNamesArray() = this.columnNames().toTypedArray()
fun ITable.columnsSortedBy(sort: (o1: String, o2: String) -> Int) = columnNames().sortedWith(Comparator(sort))

fun ITable.withColumnsAsIn(expected: ITable) = DefaultColumnFilter.includedColumnsTable(this, expected.columns())

operator fun ITable.get(row: Int, col: String): Any? = this.getValue(row, col)
operator fun ITable.get(row: Int, col: Column): Any? = this[row, col.columnName]

fun <R> ITable.mapRows(transform: (Int) -> R): List<R> = (0 until this.rowCount).map(transform)

fun renderTable(root: Html, t: ITable, remarks: HashMap<String, Int>, valuePrinter: DbPlugin.ValuePrinter) {
    renderTable(
        root,
        t,
        { col: String -> if (remarks.containsKey(col)) "table-info" else "" },
        { col1, col2 -> -compareValues(remarks[col1], remarks[col2]) },
        { td, row, col -> td()(Html(valuePrinter.wrap(t[row, col]))) },
        { }
    )
}

fun renderTable(root: Html, t: ITable, styleCol: (String) -> String, sortCols: (col1: String, col2: String) -> Int, cell: (Html, Int, String) -> Html, ifEmpty: Html.() -> Unit) {
    val cols = t.columnsSortedBy(sortCols)
    root(
        tableCaption(root.takeAwayAttr("caption"), t.tableName()),
        thead()(tr()(cols.map { th(it, CLASS to styleCol(it)) })),
        tbody()(
            if (t.rowCount == 0) {
                listOf(tr()(td("<EMPTY>").attrs("colspan" to "${cols.size}").apply(ifEmpty)))
            } else {
                t.mapRows { row -> cols.map { cell(td(CLASS to styleCol(it)), row, it) } }.map { tr()(*it.toTypedArray()) }
            }
        )
    )
}

fun tableCaption(title: String?, def: String?): Html = caption()(
    italic(" ${if (title != null && !title.isBlank()) title else def}", CLASS to "fa fa-database fa-pull-left fa-border")
)