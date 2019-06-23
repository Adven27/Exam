package com.adven.concordion.extensions.exam.db.commands

import com.adven.concordion.extensions.exam.db.DbTester
import com.adven.concordion.extensions.exam.db.DbResultRenderer
import com.adven.concordion.extensions.exam.core.html.*
import com.adven.concordion.extensions.exam.core.resolveToObj
import com.adven.concordion.extensions.exam.core.utils.parsePeriod
import org.concordion.api.CommandCall
import org.concordion.api.Evaluator
import org.concordion.api.Result.FAILURE
import org.concordion.api.Result.SUCCESS
import org.concordion.api.ResultRecorder
import org.concordion.api.listener.AssertEqualsListener
import org.concordion.api.listener.AssertFailureEvent
import org.concordion.api.listener.AssertSuccessEvent
import org.concordion.internal.util.Announcer
import org.dbunit.assertion.DbComparisonFailure
import org.dbunit.assertion.DbUnitAssert
import org.dbunit.assertion.DiffCollectingFailureHandler
import org.dbunit.assertion.Difference
import org.dbunit.assertion.comparer.value.IsActualEqualToExpectedValueComparer
import org.dbunit.assertion.comparer.value.IsActualWithinToleranceOfExpectedTimestampValueComparer
import org.dbunit.dataset.ITable
import org.dbunit.dataset.SortedTable
import org.dbunit.dataset.datatype.DataType
import org.dbunit.util.QualifiedTableName
import java.sql.Timestamp
import java.util.*
import java.util.regex.Pattern

class DBCheckCommand(name: String, tag: String, dbTester: DbTester) : DBCommand(name, tag, dbTester) {
    private val listeners = Announcer.to(AssertEqualsListener::class.java)

    private val actualTable: ITable
        get() {
            val conn = dbTester.executors[ds]!!.connection
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
        assertEq(evaluator!!, cmd.html(), resultRecorder, expectedTable, actualTable.withColumnsAsIn(expectedTable))
    }

    private fun assertEq(evaluator: Evaluator, rootEl: Html, resultRecorder: ResultRecorder?, expected: ITable, actual: ITable) {
        var root = rootEl
        val diffHandler = DiffCollectingFailureHandler()
        val columns: Array<String> = if (orderBy.isEmpty()) expected.columnNamesArray() else orderBy
        val expectedTable = SortedTable(expected, columns)
        val actualTable = SortedTable(actual, columns)
        try {
            DbUnitAssert().assertWithValueComparer(
                expectedTable,
                actualTable,
                diffHandler,
                RegexAndWithinAwareValueComparer(evaluator),
                null
            )
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
                                    if (text().isRegex() || text().isWithin()) " (${actual[row, it]})" else ""
                                )
                            }
                        })
                }
            })
    }

    private fun Html.markAsSuccess(resultRecorder: ResultRecorder) = success(resultRecorder, this)
    private fun Difference.markAsFailure(resultRecorder: ResultRecorder, td: Html) =
        failure(resultRecorder, td, this.actualValue, this.expectedValue.convertToString())
}

class WithinValueComparer(tolerance: Long) : IsActualWithinToleranceOfExpectedTimestampValueComparer(0, tolerance) {
    public override fun isExpected(
        expectedTable: ITable?,
        actualTable: ITable?,
        rowNum: Int,
        columnName: String?,
        dataType: DataType,
        expectedValue: Any?,
        actualValue: Any?
    ) = super.isExpected(expectedTable, actualTable, rowNum, columnName, dataType, expectedValue, actualValue)
}

class RegexAndWithinAwareValueComparer(val evaluator: Evaluator) : IsActualEqualToExpectedValueComparer() {
    override fun isExpected(
        expectedTable: ITable?,
        actualTable: ITable?,
        rowNum: Int,
        columnName: String?,
        dataType: DataType,
        expected: Any?,
        actual: Any?
    ): Boolean = when {
        expected.isRegex() -> regexMatches(expected, actual)
        expected.isWithin() -> WithinValueComparer(expected.toString().withinPeriod()).isExpected(
            expectedTable, actualTable, rowNum, columnName, dataType, resolve(expected.toString()), actual
        )
        else -> super.isExpected(expectedTable, actualTable, rowNum, columnName, dataType, expected, actual)
    }

    private fun resolve(expected: String) : Timestamp {
        val expectedDateExpression = expected.substring(expected.indexOf("}") + 1).trim()
        return Timestamp((
            if (expectedDateExpression.isBlank()) Date()
            else (resolveToObj(expectedDateExpression, evaluator) as Date)
            ).time)
    }

    private fun regexMatches(expectedValue: Any?, actualValue: Any?): Boolean {
        if (actualValue == null) return false
        val expected = expectedValue.toString()
        val pattern = Pattern.compile(expected.substring(expected.indexOf("}") + 1).trim())
        return pattern.matcher(actualValue.toString()).matches()
    }
}

private fun Any?.isRegex() =
    this != null && this.toString().startsWith("!{regex}")

private fun Any?.isWithin() =
        this != null && this.toString().startsWith("!{within ")

private fun String.withinPeriod() =
    parsePeriod(
        this.substring(
            "!{within ".length,
            this.indexOf("}")
        ).trim()
    ).toPeriod().toStandardDuration().millis