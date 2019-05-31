package com.adven.concordion.extensions.exam.db.commands

import com.adven.concordion.extensions.exam.db.DbResultRenderer
import com.adven.concordion.extensions.exam.html.*
import com.github.database.rider.core.dataset.DataSetExecutorImpl
import org.concordion.api.CommandCall
import org.concordion.api.Evaluator
import org.concordion.api.Result.FAILURE
import org.concordion.api.Result.SUCCESS
import org.concordion.api.ResultRecorder
import org.concordion.api.listener.AssertEqualsListener
import org.concordion.api.listener.AssertFailureEvent
import org.concordion.api.listener.AssertSuccessEvent
import org.concordion.internal.util.Announcer
import org.dbunit.assertion.*
import org.dbunit.dataset.DataSetException
import org.dbunit.dataset.ITable
import org.dbunit.dataset.SortedTable
import org.dbunit.dataset.datatype.DataType
import org.dbunit.util.QualifiedTableName
import org.slf4j.LoggerFactory
import java.util.regex.Pattern

class DBCheckCommand(name: String, tag: String, dbTester: DataSetExecutorImpl) : DBCommand(name, tag, dbTester) {
    private val listeners = Announcer.to(AssertEqualsListener::class.java)

    private val actualTable: ITable
        get() {
            val conn = DataSetExecutorImpl.getExecutorById(ds).riderDataSource.dbUnitConnection
            val qualifiedName = QualifiedTableName(expectedTable.tableName(), conn.schema).qualifiedName
            val where = if (where.isNullOrEmpty()) "" else "WHERE $where"
            return conn.createQueryTable(qualifiedName, "select * from $qualifiedName $where")
        }

    init {
        listeners.addListener(DbResultRenderer())
    }

    private fun failure(resultRecorder: ResultRecorder, html: Html, actual: Any?, expected: String): Html {
        resultRecorder.record(FAILURE)
        listeners.announce().failureReported(AssertFailureEvent(html.el, expected, actual))
        return html
    }

    private fun success(resultRecorder: ResultRecorder, html: Html): Html {
        resultRecorder.record(SUCCESS)
        listeners.announce().successReported(AssertSuccessEvent(html.el))
        return html
    }

    override fun verify(cmd: CommandCall?, evaluator: Evaluator?, resultRecorder: ResultRecorder?) {
        assertEq(cmd.html(), resultRecorder, expectedTable, actualTable.withColumnsAsIn(expectedTable))
    }

    private fun assertEq(rootEl: Html, resultRecorder: ResultRecorder?, expected: ITable, actual: ITable) {
        var root = rootEl
        val diffHandler = DiffCollectingFailureHandler()
        val columns: Array<String> = if (orderBy.isEmpty()) expected.columnNamesArray() else orderBy
        val expectedTable = SortedTable(expected, columns)
        val actualTable = SortedTable(actual, columns)
        try {
            DBAssert().assertEquals(expectedTable, actualTable, diffHandler)
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
        checkResult(root, expectedTable, actualTable, diffHandler.diffList as List<Difference>, resultRecorder!!)
    }

    private fun checkResult(root: Html, expected: ITable, actual: ITable, diffs: List<Difference>, resultRecorder: ResultRecorder) {
        val cols = expected.columnNames()
        root(
            tableCaption(root.attr("caption"), expected.tableName()),
            thead()(
                tr()(
                    cols.map { th(it) }
                )))
        root(
            if (expected.rowCount == 0) {
                listOf(
                    tr()(
                        td("<EMPTY>").attrs("colspan" to "${cols.size}").markAsSuccess(resultRecorder)
                    )
                )
            } else {
                (0 until expected.rowCount).map { row ->
                    tr()(
                        cols.map {
                            val expectedValue = expected[row, it]
                            td(expectedValue).apply {
                                diffs.firstOrNull { diff ->
                                    diff.rowIndex == row && diff.columnName == it
                                }?.markAsFailure(resultRecorder, this) ?: markAsSuccess(resultRecorder).text(
                                    if (text().isRegex()) """ (${actual[row, it]})""" else ""
                                )
                            }
                        })
                }
            })
    }

    private fun Html.markAsSuccess(resultRecorder: ResultRecorder) = success(resultRecorder, this)
    private fun Difference.markAsFailure(resultRecorder: ResultRecorder, td: Html) =
            failure(resultRecorder, td, this.actualValue, this.expectedValue.toString())
}

class DBAssert : DbUnitAssert() {

    /**
     * Same as DBUnitAssert with support for regex in row values
     * @param expectedTable expected table
     * @param actualTable current table
     * @param comparisonCols columnName
     * @param failureHandler handler
     * @throws DataSetException if datasets does not match
     */
    @Throws(DataSetException::class)
    override fun compareData(
        expectedTable: ITable?,
        actualTable: ITable?,
        comparisonCols: Array<ComparisonColumn>?,
        failureHandler: FailureHandler?
    ) {
        logger.debug(
            "compareData(expectedTable={}, actualTable={}, " + "comparisonCols={}, failureHandler={}) - start",
            expectedTable, actualTable, comparisonCols, failureHandler
        )
        when {
            expectedTable == null -> shouldBeSet("expectedTable")
            actualTable == null -> shouldBeSet("actualTable")
            comparisonCols == null -> shouldBeSet("comparisonCols")
            failureHandler == null -> shouldBeSet("failureHandler")
            else ->
                for (i in 0 until expectedTable.rowCount) {
                    for (j in comparisonCols.indices) {
                        val compareColumn = comparisonCols[j]

                        val column = compareColumn.columnName
                        val dataType = compareColumn.dataType

                        val expected = expectedTable.getValue(i, column)
                        val actual = actualTable.getValue(i, column)

                        if (skipCompare(column, expected, actual)) {
                            if (logger.isTraceEnabled) {
                                logger.trace("""ignoring comparison $expected=$actual on column $column""")
                            }
                            continue
                        }
                        when {
                            expected.isRegex() ->
                                if (!regexMatches(expected, actual)) {
                                    failureHandler.handle(
                                        Difference(
                                            expectedTable, actualTable, i, column, expected, actual ?: "(null)"
                                        )
                                    )
                                }
                            compareAs(dataType, expected, actual) ->
                                failureHandler.handle(
                                    Difference(expectedTable, actualTable, i, column, expected, actual)
                                )
                        }
                    }
                }
        }
    }

    private fun shouldBeSet(param: String): Unit =
        throw NullPointerException("The parameter '$param' must not be null")

    private fun compareAs(type: DataType, expected: Any?, actual: Any?) = type.compare(expected, actual) != 0

    private fun regexMatches(expectedValue: Any, actualValue: Any?): Boolean {
        if (actualValue == null) return false
        val expected = expectedValue.toString()
        val pattern = Pattern.compile(expected.substring(expected.indexOf(endSymbol(expected)) + 1).trim { it <= ' ' })
        return pattern.matcher(actualValue.toString()).matches()
    }

    private fun endSymbol(expected: String) = when {
        expected.startsWith("!{regex}") -> "}"
        else -> ":"
    }

    companion object {
        private val logger = LoggerFactory.getLogger(DbUnitAssert::class.java)
    }
}

private fun Any?.isRegex() =
    this != null && (this.toString().startsWith("regex:") || this.toString().startsWith("!{regex}"))