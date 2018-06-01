package com.adven.concordion.extensions.exam.db.commands

import com.adven.concordion.extensions.exam.PlaceholdersResolver
import com.adven.concordion.extensions.exam.commands.ExamCommand
import com.adven.concordion.extensions.exam.db.TableData
import com.adven.concordion.extensions.exam.html.Html
import com.adven.concordion.extensions.exam.html.Html.Companion.italic
import com.adven.concordion.extensions.exam.html.Html.Companion.tableSlim
import com.adven.concordion.extensions.exam.html.Html.Companion.tbody
import com.adven.concordion.extensions.exam.html.Html.Companion.td
import com.adven.concordion.extensions.exam.html.Html.Companion.th
import com.adven.concordion.extensions.exam.html.Html.Companion.thead
import com.adven.concordion.extensions.exam.html.Html.Companion.tr
import com.adven.concordion.extensions.exam.html.RowParser
import org.concordion.api.CommandCall
import org.concordion.api.Evaluator
import org.concordion.api.ResultRecorder
import org.dbunit.IDatabaseTester
import org.dbunit.database.DatabaseConfig.PROPERTY_DATATYPE_FACTORY
import org.dbunit.dataset.Column
import org.dbunit.dataset.DataSetException
import org.dbunit.dataset.ITable
import org.dbunit.ext.h2.H2DataTypeFactory
import org.dbunit.ext.hsqldb.HsqldbDataTypeFactory
import org.dbunit.ext.oracle.OracleDataTypeFactory
import java.util.*
import kotlin.collections.ArrayList

open class DBCommand(name: String, tag: String, protected val dbTester: IDatabaseTester) : ExamCommand(name, tag) {
    private val remarks = HashMap<String, Int>()
    protected lateinit var expectedTable: ITable
    protected var where: String? = null

    //Fix for warning "Potential problem found:
    // The configured data type factory 'class org.dbunit.dataset.datatype.DefaultDataTypeFactory'"
    private fun getRidOfDbUnitWarning() {
        try {
            val connection = dbTester.connection
            val dbName: String = connection.connection.metaData.databaseProductName
            val dbConfig = connection.config
            when (dbName) {
                "HSQL Database Engine" -> dbConfig.setProperty(PROPERTY_DATATYPE_FACTORY, HsqldbDataTypeFactory())
                "H2" -> dbConfig.setProperty(PROPERTY_DATATYPE_FACTORY, H2DataTypeFactory())
                "Oracle" -> dbConfig.setProperty(PROPERTY_DATATYPE_FACTORY, OracleDataTypeFactory())
                else -> System.err.println("No matching database product found $dbName")
            }
        } catch (e: Exception) {
            throw RuntimeException(e)
        }

    }

    override fun setUp(commandCall: CommandCall?, eval: Evaluator?, resultRecorder: ResultRecorder?) {
        getRidOfDbUnitWarning()
        val root = tableSlim(Html(commandCall!!.element))
        try {
            remarks.clear()
            where = root.takeAwayAttr("where", eval)
            expectedTable = TableData.filled(
                    root.takeAwayAttr("table", eval),
                    RowParser(root, "row", eval!!).parse(),
                    parseCols(root, eval)
            )
        } catch (e: DataSetException) {
            throw RuntimeException(e)
        }

    }

    protected fun parseCols(el: Html, eval: Evaluator): TableData.Cols {
        val cols = ArrayList<String>()
        val defaults = HashMap<String, Any?>()
        val attr = el.takeAwayAttr("cols")
        attr?.split(",")?.forEach {
            var c = it.trim()
            var remark: String? = null
            if (c.startsWith("*")) {
                val endRemark = c.lastIndexOf("*")
                remark = c.substring(0, endRemark + 1)
                c = c.substring(endRemark + 1)
            }
            if (c.contains("=")) {
                val colDefault = c.split("=")
                defaults[colDefault[0]] = PlaceholdersResolver.resolveToObj(colDefault[1], eval)
                if (remark != null) {
                    remarks[colDefault[0]] = remark.length
                }
            } else {
                cols.add(c)
                if (remark != null) {
                    remarks[c] = remark.length
                }
            }
        }
        return TableData.Cols(defaults, *cols.toTypedArray())
    }

    protected fun renderTable(root: Html, t: ITable) {
        try {
            val rows = ArrayList<List<String>>()
            val columns = t.tableMetaData.columns
            val cols = Arrays.copyOf(columns, columns.size)

            Arrays.sort(cols) { o1, o2 ->
                -1 * Integer.compare(
                        remarks.getOrDefault(o1.columnName, 0),
                        remarks.getOrDefault(o2.columnName, 0))
            }

            for (i in 0 until t.rowCount) {
                val row = ArrayList<String>()
                for (col in cols) {
                    row.add(t.getValue(i, col.columnName)?.toString() ?: "(null)")
                }
                rows.add(row)
            }

            root.childs(dbCaption(t, root.takeAwayAttr("caption")))

            val header = thead()
            val trh = tr()

            cols.forEach {
                trh.childs(
                        th(it.columnName).css(markedColumn(it))
                )
            }

            root.childs(header.childs(trh))
            val tbody = tbody()
            for (row in rows) {
                val tr = tr()
                for (i in row.indices) {
                    tr.childs(
                            td(row[i]).css(markedColumn(cols[i]))
                    )
                }
                tbody.childs(tr)
            }
            root.childs(tbody)
        } catch (e: DataSetException) {
            throw RuntimeException(e)
        }

    }

    protected fun dbCaption(t: ITable, title: String?): Html {
        return Html.caption(if (title == null || title.isBlank()) t.tableMetaData.tableName else title).childs(
                italic("").css("fa fa-database fa-pull-left fa-border")
        )
    }

    private fun markedColumn(col: Column): String {
        return if (remarks.containsKey(col.columnName)) "table-info" else ""
    }
}