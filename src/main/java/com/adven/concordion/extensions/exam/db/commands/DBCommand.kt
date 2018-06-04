package com.adven.concordion.extensions.exam.db.commands

import com.adven.concordion.extensions.exam.commands.ExamCommand
import com.adven.concordion.extensions.exam.db.TableData
import com.adven.concordion.extensions.exam.html.*
import com.adven.concordion.extensions.exam.resolveToObj
import org.concordion.api.CommandCall
import org.concordion.api.Evaluator
import org.concordion.api.ResultRecorder
import org.dbunit.IDatabaseTester
import org.dbunit.dataset.Column
import org.dbunit.dataset.ITable
import java.util.*

open class DBCommand(name: String, tag: String, protected val dbTester: IDatabaseTester) : ExamCommand(name, tag) {
    private val remarks = HashMap<String, Int>()
    protected lateinit var expectedTable: ITable
    protected var where: String? = null

    override fun setUp(commandCall: CommandCall?, eval: Evaluator?, resultRecorder: ResultRecorder?) {
        val root = tableSlim(Html(commandCall!!.element))(
            div(""),
            span("")
        )
        remarks.clear()
        where = root.takeAwayAttr("where", eval)
        expectedTable = TableData.filled(
            root.takeAwayAttr("table", eval),
            RowParser(root, "row", eval!!).parse(),
            parseCols(root, eval)
        )
    }

    protected fun parseCols(el: Html, eval: Evaluator): TableData.Cols {
        val attr = el.takeAwayAttr("cols")
        return if (attr == null)
            TableData.Cols(emptyMap())
        else {
            val remarkAndVal = parse(attr)
            remarks.plus(remarkAndVal.map { it.key to it.value.first })
            TableData.Cols(
                remarkAndVal
                    .filterValues { !it.second.isBlank() }
                    .mapValues { resolveToObj(it.value.second, eval) },
                *remarkAndVal.keys.toTypedArray()
            )
        }
    }

    protected fun renderTable(root: Html, t: ITable) {
        val cols = t.tableMetaData.columns.copyOf().sortedWith(Comparator { o1, o2 ->
            compareValues(remarks[o1.columnName] ?: 0, remarks[o2.columnName] ?: 0)
        })

        val rows = (0 until t.rowCount).map { i ->
            cols.map { t.getValue(i, it.columnName)?.toString() ?: "(null)" }
        }

        root(
            tableCaption(root.takeAwayAttr("caption"), t.tableMetaData.tableName),
            thead()(
                tr()(
                    cols.map {
                        th(it.columnName, CLASS to styleFor(it))
                    }
                )
            ),
            tbody()(
                rows.map {
                    tr()(
                        it.withIndex().map { (i, text) ->
                            td(text, CLASS to styleFor(cols[i]))
                        }
                    )
                }
            )
        )
    }

    protected fun tableCaption(title: String?, def: String?): Html {
        return caption(if (title != null && !title.isBlank()) title else def)(
            italic("", CLASS to "fa fa-database fa-pull-left fa-border")
        )
    }

    private fun styleFor(col: Column): String {
        return if (remarks.containsKey(col.columnName)) "table-info" else ""
    }
}

fun parse(attr: String): Map<String, Pair<Int, String>> {
    return attr.split(",")
        .map {
            val (r, n, v) = ("""(\**)([^=]+)=?(.*)""".toRegex()).matchEntire(it.trim())!!.destructured
            mapOf(n to (r.length to v))
        }
        .reduce { acc, next -> acc.plus(next) }
}
