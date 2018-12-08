package com.adven.concordion.extensions.exam.db.commands

import com.adven.concordion.extensions.exam.db.DbResultRenderer
import com.adven.concordion.extensions.exam.html.*
import org.concordion.api.CommandCall
import org.concordion.api.Element
import org.concordion.api.Evaluator
import org.concordion.api.Result.FAILURE
import org.concordion.api.Result.SUCCESS
import org.concordion.api.ResultRecorder
import org.concordion.api.listener.AssertEqualsListener
import org.concordion.api.listener.AssertFailureEvent
import org.concordion.api.listener.AssertSuccessEvent
import org.concordion.internal.util.Announcer
import org.dbunit.Assertion.assertEquals
import org.dbunit.IDatabaseTester
import org.dbunit.assertion.DbComparisonFailure
import org.dbunit.assertion.DiffCollectingFailureHandler
import org.dbunit.assertion.Difference
import org.dbunit.dataset.Column
import org.dbunit.dataset.ITable
import org.dbunit.dataset.SortedTable
import org.dbunit.dataset.filter.DefaultColumnFilter.includedColumnsTable
import org.dbunit.util.QualifiedTableName

class DBCheckCommand(name: String, tag: String, dbTester: IDatabaseTester) : DBCommand(name, tag, dbTester) {
    private val listeners = Announcer.to(AssertEqualsListener::class.java)

    private val actualTable: ITable
        get() {
            val conn = dbTester.connection
            val qualifiedName = QualifiedTableName(expectedTable.tableName(), conn.schema).qualifiedName
            val where = if (where.isNullOrEmpty()) "" else "WHERE " + where!!
            return conn.createQueryTable(qualifiedName, "select * from $qualifiedName $where")
        }

    init {
        listeners.addListener(DbResultRenderer())
    }

    private fun failure(resultRecorder: ResultRecorder, element: Element, actual: Any?, expected: String) {
        resultRecorder.record(FAILURE)
        listeners.announce().failureReported(AssertFailureEvent(element, expected, actual))
    }

    private fun success(resultRecorder: ResultRecorder, element: Element) {
        resultRecorder.record(SUCCESS)
        listeners.announce().successReported(AssertSuccessEvent(element))
    }

    override fun verify(cmd: CommandCall?, evaluator: Evaluator?, resultRecorder: ResultRecorder?) {
        val actual = actualTable
        val filteredActual = includedColumnsTable(
                actual, expectedTable.tableMetaData.columns)

        assertEq(cmd.html(), resultRecorder, expectedTable, filteredActual)
    }

    private fun assertEq(rootEl: Html, resultRecorder: ResultRecorder?, expected: ITable, actual: ITable) {
        var root = rootEl
        val diffHandler = DiffCollectingFailureHandler()
        val columns = expected.tableMetaData.columns
        val expectedTable = SortedTable(expected, columns)
        try {
            assertEquals(expectedTable, SortedTable(actual, columns), diffHandler)
        } catch (f: DbComparisonFailure) {
            //TODO move to ResultRenderer
            resultRecorder!!.record(FAILURE)
            val div = div().css("rest-failure bd-callout bd-callout-danger")(div(f.message))
            root.below(div)

            val exp = tableSlim()
            div(span("Expected: "), exp)
            root = exp

            val act = tableSlim()
            renderTable(act, actual)
            div(span("but was: "), act)
        }
        checkResult(root, expectedTable, diffHandler.diffList as List<Difference>, resultRecorder!!)
    }

    private fun checkResult(el: Html, expected: ITable, diffs: List<Difference>, resultRecorder: ResultRecorder) {
        val title = el.attr("caption")
        el(tableCaption(title, expected.tableMetaData.tableName))

        val cols = expected.tableMetaData.columns
        val header = thead()
        val thr = tr()
        for (col in cols) {
            thr(th(col.columnName))
        }
        el(header(thr))

        if (expected.rowCount == 0) {
            val td = td("<EMPTY>").attrs("colspan" to "${cols.size}")
            val tr = tr()(td)
            el(tr)
            success(resultRecorder, td.el())
        } else {
            for (row in 0 until expected.rowCount) {
                val tr = tr()
                for (col in cols) {
                    val displayedExpected = expected.getValue(row, col.columnName)?.toString() ?: "(null)"
                    val td = td(displayedExpected)
                    tr(td)
                    val fail = findFail(diffs, row, col)
                    if (fail != null) {
                        failure(resultRecorder, td.el(), fail.actualValue, displayedExpected)
                    } else {
                        success(resultRecorder, td.el())
                    }
                }
                el(tr)
            }
        }
    }

    private fun findFail(diffs: List<Difference>, row: Int, col: Column): Difference? {
        var fail: Difference? = null
        for (diff in diffs) {
            if (diff.rowIndex == row && diff.columnName == col.columnName) {
                fail = diff
                break
            }
        }
        return fail
    }
}