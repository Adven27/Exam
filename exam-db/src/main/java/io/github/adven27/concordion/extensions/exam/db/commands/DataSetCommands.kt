package io.github.adven27.concordion.extensions.exam.db.commands

import io.github.adven27.concordion.extensions.exam.core.commands.ExamCommand
import io.github.adven27.concordion.extensions.exam.core.commands.awaitConfig
import io.github.adven27.concordion.extensions.exam.core.html.Html
import io.github.adven27.concordion.extensions.exam.core.html.attr
import io.github.adven27.concordion.extensions.exam.core.html.div
import io.github.adven27.concordion.extensions.exam.core.html.html
import io.github.adven27.concordion.extensions.exam.core.html.span
import io.github.adven27.concordion.extensions.exam.core.html.takeAttr
import io.github.adven27.concordion.extensions.exam.db.DbPlugin
import io.github.adven27.concordion.extensions.exam.db.DbResultRenderer
import io.github.adven27.concordion.extensions.exam.db.DbTester
import io.github.adven27.concordion.extensions.exam.db.builder.DataSetConfig
import io.github.adven27.concordion.extensions.exam.db.builder.DataSetExecutor
import io.github.adven27.concordion.extensions.exam.db.builder.SeedStrategy
import io.github.adven27.concordion.extensions.exam.db.builder.SeedStrategy.CLEAN_INSERT
import io.github.adven27.concordion.extensions.exam.db.commands.DBCheckCommand.Companion.isDbMatcher
import org.concordion.api.CommandCall
import org.concordion.api.Evaluator
import org.concordion.api.Fixture
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
import org.dbunit.operation.DatabaseOperation

class DataSetExecuteCommand(
    name: String,
    tag: String,
    val dbTester: DbTester,
    var valuePrinter: DbPlugin.ValuePrinter,
    private val allowedSeedStrategies: List<SeedStrategy>
) : ExamCommand(name, tag) {
    override fun setUp(
        cmd: CommandCall,
        evaluator: Evaluator,
        resultRecorder: ResultRecorder,
        fixture: Fixture
    ) {
        DataSetExecutor(cmd.ds(dbTester)).insertDataSet(
            DataSetConfig(cmd.dataSets(), cmd.allowedSeedStrategy(allowedSeedStrategies), debug = cmd.debug()),
            evaluator
        ).iterator().apply {
            while (next()) {
                cmd.html()(
                    table.let {
                        renderTable(
                            null,
                            it,
                            { td, row, col -> td()(Html(valuePrinter.wrap(it[row, col]))) }
                        )
                    }
                )
            }
        }
    }
}

@Suppress("TooManyFunctions")
class DataSetVerifyCommand(name: String, tag: String, val dbTester: DbTester, var valuePrinter: DbPlugin.ValuePrinter) :
    ExamCommand(name, tag) {
    private val listeners = Announcer.to(AssertEqualsListener::class.java)

    init {
        listeners.addListener(DbResultRenderer())
    }

    override fun verify(
        commandCall: CommandCall,
        evaluator: Evaluator,
        resultRecorder: ResultRecorder,
        fixture: Fixture
    ) {
        val root = commandCall.html()
        dbTester.dbUnitConfig.valueComparer.setEvaluator(evaluator)
        dbTester.dbUnitConfig.columnValueComparers.forEach { it.value.setEvaluator(evaluator) }
        DataSetExecutor(commandCall.ds(dbTester)).awaitCompareCurrentDataSetWith(
            commandCall.awaitConfig(), DataSetConfig(commandCall.dataSets()), evaluator, orderBy = commandCall.orderBy()
        ).apply {
            toHtml(root, resultRecorder)
        }
    }

    private fun DataSetExecutor.DataSetsCompareResult.toHtml(root: Html, resultRecorder: ResultRecorder) {
        expected.iterator().apply {
            val mismatchedTables = rowsMismatch.sortedAsTables(expected.tableNames).map { mismatch ->
                render(mismatch, root)
                resultRecorder.record(FAILURE)
                mismatch.first.tableName()
            }.toList()
            val diffTables = diffTables(resultRecorder, root)
            val passed: (table: ITable) -> Boolean =
                { table -> !(diffTables + mismatchedTables).contains(table.tableName()) }
            while (next()) {
                if (passed(table)) {
                    root(
                        table.let { expected ->
                            val actual = actual.getTable(expected.tableName())
                            renderTable(
                                null,
                                expected,
                                markAsSuccess(expected, actual, resultRecorder),
                                ifEmpty = { markAsSuccess(resultRecorder) }
                            )
                        }
                    )
                }
            }
        }
    }

    private fun DataSetExecutor.DataSetsCompareResult.diffTables(recorder: ResultRecorder, root: Html): List<String> {
        return diff.groupBy { it.expectedTable }.map { (expected, diffs) ->
            val markAsSuccessOrFailure: (Html, Int, String) -> Html = { td, row, col ->
                val value = expected[row, col]
                val expectedValue = valuePrinter.wrap(value)
                diffs.firstOrNull { it.rowIndex == row && it.columnName == col }?.markAsFailure(recorder, td)
                    ?: td.markAsSuccess(recorder)(
                        Html(expectedValue).text(
                            appendIf(
                                value.isDbMatcher() && actual.getTable(expected.tableName()).rowCount == expected.rowCount,
                                actual.getTable(expected.tableName()),
                                row,
                                col
                            )
                        )
                    )
            }
            root(
                renderTable(
                    null,
                    expected,
                    markAsSuccessOrFailure,
                    ifEmpty = { markAsSuccess(recorder) }
                )
            )
            expected.tableName()
        }.toList()
    }

    private fun markAsSuccess(expected: ITable, actual: ITable, recorder: ResultRecorder): (Html, Int, String) -> Html =
        { td, row, col ->
            val value = expected[row, col]
            val expectedValue = valuePrinter.wrap(value)
            td.markAsSuccess(recorder)(
                Html(expectedValue).text(
                    appendIf(
                        value.isDbMatcher() && actual.rowCount == expected.rowCount,
                        actual,
                        row,
                        col
                    )
                )
            )
        }

    private fun List<Triple<ITable, ITable, DbComparisonFailure>>.sortedAsTables(tables: Array<String>) =
        tables.mapNotNull { t -> this.find { it.first.tableName() == t } }

    private fun render(mismatch: Triple<ITable, ITable, DbComparisonFailure>, root: Html) {
        root(
            div().css("rest-failure bd-callout bd-callout-danger")(div(mismatch.third.message))(
                span("Expected: "),
                render(mismatch.first),
                span("but was: "),
                render(mismatch.second)
            )
        )
    }

    private fun render(tbl: ITable): Html =
        renderTable(null, tbl, { td, row, col -> td()(Html(valuePrinter.wrap(tbl[row, col]))) })

    private fun appendIf(append: Boolean, actual: ITable, row: Int, col: String): String =
        if (append) " (${actual[row, col]})" else ""

    private fun Html.markAsSuccess(resultRecorder: ResultRecorder) = success(resultRecorder, this)
    private fun Difference.markAsFailure(resultRecorder: ResultRecorder, td: Html): Html {
        return failure(resultRecorder, td, valuePrinter.print(this.actualValue), valuePrinter.print(this.expectedValue))
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

private fun CommandCall?.dataSets() = attr("datasets", "").split(",").map { attr("dir", "").trim() + it.trim() }

fun CommandCall?.operation() = SeedStrategy.valueOf(this.takeAttr("operation", CLEAN_INSERT.name).toUpperCase())

fun CommandCall?.allowedOperation(allowedSeedStrategies: List<SeedStrategy>): DatabaseOperation =
    allowedSeedStrategy(allowedSeedStrategies).operation

fun CommandCall?.allowedSeedStrategy(allowed: List<SeedStrategy>): SeedStrategy = operation().let { strategy ->
    allowed.find { it == strategy }
        ?: throw java.lang.IllegalArgumentException("Forbidden seed strategy $strategy. Allowed strategies: $allowed")
}

private fun CommandCall?.debug() = this.takeAttr("debug", "false").toBoolean()
private fun CommandCall?.ds(dbTester: DbTester): DbTester = this.takeAttr("ds", DbTester.DEFAULT_DATASOURCE).let {
    dbTester.executors[it]
        ?: throw IllegalArgumentException("DbTester for datasource [$it] not registered in DbPlugin.")
}

private fun CommandCall?.orderBy() =
    this.takeAttr("orderBy", "").split(",").map { it.trim() }.filter { it.isNotEmpty() }.toTypedArray()
