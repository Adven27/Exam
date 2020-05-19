package com.adven.concordion.extensions.exam.db.commands

import com.adven.concordion.extensions.exam.core.commands.ExamCommand
import com.adven.concordion.extensions.exam.core.html.*
import com.adven.concordion.extensions.exam.db.DbPlugin
import com.adven.concordion.extensions.exam.db.DbResultRenderer
import com.adven.concordion.extensions.exam.db.DbTester
import com.adven.concordion.extensions.exam.db.builder.DataSetConfig
import com.adven.concordion.extensions.exam.db.builder.DataSetExecutor
import org.concordion.api.CommandCall
import org.concordion.api.Evaluator
import org.concordion.api.Result
import org.concordion.api.ResultRecorder
import org.concordion.api.listener.AssertEqualsListener
import org.concordion.api.listener.AssertFailureEvent
import org.concordion.api.listener.AssertSuccessEvent
import org.concordion.internal.util.Announcer
import org.dbunit.assertion.Difference
import org.dbunit.dataset.ITable

class DataSetExecuteCommand(name: String, tag: String, val dbTester: DbTester, var valuePrinter: DbPlugin.ValuePrinter) : ExamCommand(name, tag) {
    override fun setUp(commandCall: CommandCall, evaluator: Evaluator, resultRecorder: ResultRecorder) {
        DataSetExecutor(dbTester).insertDataSet(DataSetConfig(commandCall.dataSets()), evaluator).iterator().apply {
            while (next()) {
                commandCall.html()(table.let {
                    renderTable(table(), it, { td, row, col -> td()(Html(valuePrinter.wrap(it[row, col]))) })
                })
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
        dbTester.dbUnitConfig.valueComparer.setEvaluator(evaluator)
        dbTester.dbUnitConfig.columnValueComparers.forEach { it.value.setEvaluator(evaluator) }
        DataSetExecutor(dbTester).compareCurrentDataSetWith(DataSetConfig(commandCall.dataSets()), evaluator).apply {
            third.sortedBy { it.expectedTable.tableName() }.map { diff ->
                commandCall.html()(
                    pre("${diff.expectedTable.tableName()} ${diff.actualTable.tableName()} ${diff.rowIndex} ${diff.failMessage}"))
            }.ifEmpty {
                first.reverseIterator().apply {
                    while (next()) {
                        commandCall.html()(table.let { expected ->
                            val actual = second.getTable(expected.tableName())
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

    private fun appendIf(append: Boolean, actual: ITable, row: Int, col: String): String = if (append) " (${actual[row, col]})" else ""
    private fun Html.markAsSuccess(resultRecorder: ResultRecorder) = success(resultRecorder, this)
    private fun Difference.markAsFailure(resultRecorder: ResultRecorder, td: Html): Html {
        return failure(resultRecorder, td, this.actualValue, valuePrinter.print(this.expectedValue))
    }

    private fun failure(resultRecorder: ResultRecorder, html: Html, actual: Any?, expected: String): Html {
        resultRecorder.record(Result.FAILURE)
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