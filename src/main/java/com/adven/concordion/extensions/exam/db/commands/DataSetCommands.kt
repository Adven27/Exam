package com.adven.concordion.extensions.exam.db.commands

import com.adven.concordion.extensions.exam.core.commands.ExamCommand
import com.adven.concordion.extensions.exam.core.html.*
import com.adven.concordion.extensions.exam.db.DbPlugin
import com.adven.concordion.extensions.exam.db.DbResultRenderer
import com.adven.concordion.extensions.exam.db.DbTester
import com.adven.concordion.extensions.exam.db.builder.DataSetConfig
import com.adven.concordion.extensions.exam.db.builder.DataSetExecutor
import com.adven.concordion.extensions.exam.db.builder.SeedStrategy
import com.adven.concordion.extensions.exam.db.builder.SeedStrategy.CLEAN_INSERT
import org.concordion.api.CommandCall
import org.concordion.api.Evaluator
import org.concordion.api.Result
import org.concordion.api.Result.FAILURE
import org.concordion.api.ResultRecorder
import org.concordion.api.listener.AssertEqualsListener
import org.concordion.api.listener.AssertFailureEvent
import org.concordion.api.listener.AssertSuccessEvent
import org.concordion.internal.util.Announcer
import org.dbunit.assertion.DbComparisonFailure
import org.dbunit.assertion.Difference
import org.dbunit.dataset.ITable

class DataSetExecuteCommand(name: String, tag: String, val dbTester: DbTester, var valuePrinter: DbPlugin.ValuePrinter) : ExamCommand(name, tag) {
    override fun setUp(commandCall: CommandCall, evaluator: Evaluator, resultRecorder: ResultRecorder) {
        DataSetExecutor(commandCall.ds(dbTester)).insertDataSet(
            DataSetConfig(commandCall.dataSets(), commandCall.operation(), debug = commandCall.debug()), evaluator
        ).iterator().apply {
            while (next()) {
                commandCall.html()(
                    table.let { renderTable(table(), it, { td, row, col -> td()(Html(valuePrinter.wrap(it[row, col]))) }) }
                )
            }
        }
    }
}

class DataSetVerifyCommand(name: String, tag: String, val dbTester: DbTester, var valuePrinter: DbPlugin.ValuePrinter) : ExamCommand(name, tag) {
    private val listeners = Announcer.to(AssertEqualsListener::class.java)

    init {
        listeners.addListener(DbResultRenderer())
    }

    override fun verify(commandCall: CommandCall, evaluator: Evaluator, resultRecorder: ResultRecorder) {
        val root = commandCall.html()
        dbTester.dbUnitConfig.valueComparer.setEvaluator(evaluator)
        dbTester.dbUnitConfig.columnValueComparers.forEach { it.value.setEvaluator(evaluator) }
        DataSetExecutor(commandCall.ds(dbTester)).compareCurrentDataSetWith(DataSetConfig(commandCall.dataSets()), evaluator).apply {
            val mismatchedTables = rowsMismatch.sortedBy { it.first.tableName() }.map { mismatch ->
                render(mismatch, root)
                resultRecorder.record(FAILURE)
                mismatch.first.tableName()
            }.toList()

            val diffTables = diff.groupBy { it.expectedTable }.map { (expected, diffs) ->
                val markAsSuccessOrFailure: (Html, Int, String) -> Html = { td, row, col ->
                    val value = expected[row, col]
                    val expectedValue = valuePrinter.wrap(value)
                    diffs.firstOrNull { diff ->
                        diff.rowIndex == row && diff.columnName == col
                    }?.markAsFailure(resultRecorder, td)
                        ?: td.markAsSuccess(resultRecorder)(
                            Html(expectedValue).text(
                                appendIf(DBCheckCommand.isDbMatcher(value) && actual.getTable(expected.tableName()).rowCount == expected.rowCount, actual.getTable(expected.tableName()), row, col)
                            )
                        )
                }
                root(renderTable(tableSlim(), expected, markAsSuccessOrFailure, ifEmpty = { markAsSuccess(resultRecorder) }))
                expected.tableName()
            }.toList()
            val passed: (table: ITable) -> Boolean = { table -> !(diffTables + mismatchedTables).contains(table.tableName()) }
            expected.reverseIterator().apply {
                while (next()) {
                    if (passed(table)) {
                        root(table.let { expected ->
                            val actual = actual.getTable(expected.tableName())
                            renderTable(
                                table(),
                                expected,
                                { td, row, col ->
                                    val value = expected[row, col]
                                    val expectedValue = valuePrinter.wrap(value)
                                    td.markAsSuccess(resultRecorder)(
                                        Html(expectedValue).text(
                                            appendIf(DBCheckCommand.isDbMatcher(value) && actual.rowCount == expected.rowCount, actual, row, col)
                                        )
                                    )
                                },
                                ifEmpty = { markAsSuccess(resultRecorder) }
                            )
                        })
                    }
                }
            }
        }
    }

    private fun render(mismatch: Triple<ITable, ITable, DbComparisonFailure>, root: Html) {
        root.below(
            div().css("rest-failure bd-callout bd-callout-danger")(div(mismatch.third.message))(
                span("Expected: "),
                render(mismatch.first),
                span("but was: "),
                render(mismatch.second)
            )
        )
    }

    private fun render(tbl: ITable): Html =
        renderTable(tableSlim(), tbl, { td, row, col -> td()(Html(valuePrinter.wrap(tbl[row, col]))) })

    private fun appendIf(append: Boolean, actual: ITable, row: Int, col: String): String = if (append) " (${actual[row, col]})" else ""
    private fun Html.markAsSuccess(resultRecorder: ResultRecorder) = success(resultRecorder, this)
    private fun Difference.markAsFailure(resultRecorder: ResultRecorder, td: Html): Html {
        return failure(resultRecorder, td, this.actualValue, valuePrinter.print(this.expectedValue))
    }

    private fun failure(resultRecorder: ResultRecorder, html: Html, actual: Any?, expected: String): Html {
        resultRecorder.record(FAILURE)
        listeners.announce().failureReported(AssertFailureEvent(html.el, expected, actual))
        return html
    }

    private fun success(resultRecorder: ResultRecorder, html: Html): Html {
        resultRecorder.record(Result.SUCCESS)
        listeners.announce().successReported(AssertSuccessEvent(html.el))
        return html
    }
}

private fun CommandCall?.dataSets() =
    attr("datasets", "").split(",").map { attr("dir", "").trim() + it.trim() }

private fun CommandCall?.operation() =
    SeedStrategy.valueOf(this.takeAttr("operation", CLEAN_INSERT.name).toUpperCase())

private fun CommandCall?.debug() = this.takeAttr("debug", "false").toBoolean()
private fun CommandCall?.ds(dbTester: DbTester): DbTester = this.takeAttr("ds", DbTester.DEFAULT_DATASOURCE).let {
    dbTester.executors[it]
        ?: throw IllegalArgumentException("DbTester for datasource [$it] not registered in DbPlugin.")
}
