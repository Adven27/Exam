package io.github.adven27.concordion.extensions.exam.db.commands

import io.github.adven27.concordion.extensions.exam.core.commands.ExamCommand
import io.github.adven27.concordion.extensions.exam.core.commands.VarsAttrs
import io.github.adven27.concordion.extensions.exam.core.commands.awaitConfig
import io.github.adven27.concordion.extensions.exam.core.html.Html
import io.github.adven27.concordion.extensions.exam.core.html.div
import io.github.adven27.concordion.extensions.exam.core.html.html
import io.github.adven27.concordion.extensions.exam.core.html.span
import io.github.adven27.concordion.extensions.exam.db.DbPlugin
import io.github.adven27.concordion.extensions.exam.db.DbResultRenderer
import io.github.adven27.concordion.extensions.exam.db.DbTester
import io.github.adven27.concordion.extensions.exam.db.builder.DataSetConfig
import io.github.adven27.concordion.extensions.exam.db.builder.DataSetExecutor
import io.github.adven27.concordion.extensions.exam.db.builder.SeedStrategy
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
import org.dbunit.dataset.ITableIterator

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
        cmd.html().also { root ->
            Attrs.from(root, evaluator, allowedSeedStrategies).also { attrs ->
                insertDataSet(attrs, evaluator).iterator().apply {
                    while (next()) {
                        render(cmd.html())
                    }
                }
            }
        }
    }

    private fun insertDataSet(attrs: Attrs, evaluator: Evaluator) =
        DataSetExecutor(attrs.datasetCommandAttrs.ds(dbTester)).insertDataSet(
            DataSetConfig(
                attrs.datasetCommandAttrs.datasets.dataSets(),
                attrs.setAttrs.seedStrategy,
                debug = attrs.datasetCommandAttrs.debug
            ),
            evaluator
        )

    private fun ITableIterator.render(root: Html) {
        root(
            table.let {
                renderTable(
                    null,
                    it,
                    { td, row, col -> td()(Html(valuePrinter.wrap(it[row, col]))) }
                )
            }
        )
    }

    data class Attrs(
        val datasetCommandAttrs: DatasetCommandAttrs,
        val setAttrs: SetAttrs,
    ) {
        companion object {
            fun from(root: Html, evaluator: Evaluator, allowedSeedStrategies: List<SeedStrategy>) = Attrs(
                DatasetCommandAttrs.from(root, evaluator),
                SetAttrs.from(root, allowedSeedStrategies),
            )
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
        commandCall.html().also { root ->
            Attrs.from(root, evaluator).also { attrs ->
                dbTester.dbUnitConfig.valueComparer.setEvaluator(evaluator)
                dbTester.dbUnitConfig.columnValueComparers.forEach { it.value.setEvaluator(evaluator) }
                DataSetExecutor(attrs.datasetCommandAttrs.ds(dbTester)).awaitCompareCurrentDataSetWith(
                    commandCall.awaitConfig(),
                    DataSetConfig(attrs.datasetCommandAttrs.datasets.dataSets()),
                    evaluator,
                    orderBy = attrs.orderBy
                ).apply {
                    toHtml(root, resultRecorder)
                }
            }
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

    data class Attrs(
        val datasetCommandAttrs: DatasetCommandAttrs,
        val orderBy: Array<String>,
    ) {
        companion object {
            private const val ORDER_BY = "orderBy"

            fun from(root: Html, evaluator: Evaluator) = Attrs(
                DatasetCommandAttrs.from(root, evaluator),
                root.takeAwayAttr(ORDER_BY, "").split(",").map { it.trim() }.filter { it.isNotEmpty() }
                    .toTypedArray(),
            )
        }
    }
}

data class DatasetCommandAttrs(
    val datasets: DatasetsAttrs,
    val vars: VarsAttrs,
    val ds: String,
    val debug: Boolean
) {
    fun ds(dbTester: DbTester): DbTester = dbTester.executors[ds]
        ?: throw IllegalArgumentException("DbTester for datasource [$ds] not registered in DbPlugin.")

    companion object {
        private const val DS = "ds"
        private const val DEBUG = "debug"

        fun from(root: Html, evaluator: Evaluator) = DatasetCommandAttrs(
            DatasetsAttrs.from(root),
            VarsAttrs(root, evaluator),
            root.takeAwayAttr(DS, DbTester.DEFAULT_DATASOURCE),
            root.takeAwayAttr(DEBUG, "false").toBoolean(),
        )
    }
}

data class DatasetsAttrs(
    val datasets: String,
    val dir: String,
) {
    fun dataSets() = datasets.split(",").map { dir.trim() + it.trim() }

    companion object {
        private const val DIR = "dir"
        private const val DATASETS = "datasets"

        fun from(root: Html) = DatasetsAttrs(
            root.attrOrFail(DATASETS),
            root.takeAwayAttr(DIR, ""),
        )
    }
}
