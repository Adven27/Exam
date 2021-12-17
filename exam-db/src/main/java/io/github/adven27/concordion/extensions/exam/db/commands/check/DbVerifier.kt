package io.github.adven27.concordion.extensions.exam.db.commands.check

import io.github.adven27.concordion.extensions.exam.core.commands.AwaitVerifier
import io.github.adven27.concordion.extensions.exam.core.commands.Verifier.Success
import io.github.adven27.concordion.extensions.exam.db.DbTester
import io.github.adven27.concordion.extensions.exam.db.DbUnitConfig
import io.github.adven27.concordion.extensions.exam.db.commands.check.CheckCommand.Expected
import io.github.adven27.concordion.extensions.exam.db.commands.columnNamesArray
import io.github.adven27.concordion.extensions.exam.db.commands.sortedTable
import io.github.adven27.concordion.extensions.exam.db.commands.tableName
import io.github.adven27.concordion.extensions.exam.db.commands.withColumnsAsIn
import mu.KLogging
import org.awaitility.core.ConditionTimeoutException
import org.concordion.api.Evaluator
import org.dbunit.Assertion.assertWithValueComparer
import org.dbunit.assertion.DbComparisonFailure
import org.dbunit.assertion.Difference
import org.dbunit.dataset.CompositeTable
import org.dbunit.dataset.ITable
import org.dbunit.dataset.SortedTable

open class DbVerifier(val dbTester: DbTester) : AwaitVerifier<Expected, ITable> {
    companion object : KLogging()

    override fun verify(eval: Evaluator, expected: Expected, actual: ITable) =
        verify(eval, expected) { false to actual }

    override fun verify(
        eval: Evaluator,
        expected: Expected,
        getActual: () -> Pair<Boolean, ITable>
    ): Result<Success<Expected, ITable>> =
        try {
            val sortCols: Array<String> =
                if (expected.orderBy.isEmpty()) expected.table.columnNamesArray() else expected.orderBy.toTypedArray()
            var actualTable = sortedTable(
                getActual().second.withColumnsAsIn(expected.table),
                sortCols,
                dbTester.dbUnitConfig.overrideRowSortingComparer
            )
            val expectedTable = sortedTable(
                CompositeTable(actualTable.tableMetaData, expected.table),
                sortCols,
                dbTester.dbUnitConfig.overrideRowSortingComparer
            )
            expected.await?.let {
                it.await("Await DB table ${expectedTable.tableName()}").untilAsserted {
                    actualTable = sortedTable(
                        getActual().second.withColumnsAsIn(expectedTable),
                        sortCols,
                        dbTester.dbUnitConfig.overrideRowSortingComparer
                    )
                    dbUnitAssert(expectedTable, actualTable)
                }
            } ?: dbUnitAssert(expectedTable, actualTable)
            Result.success(Success(expected, actualTable))
        } catch (expected: Throwable) {
            logger.warn("Check failed", expected)
            Result.failure(if (expected is ConditionTimeoutException) expected.cause!! else expected)
        }

    class TableSizeMismatch(val actual: ITable, failure: DbComparisonFailure) :
        AssertionError("table size mismatch", failure)

    class TableContentMismatch(val diff: List<Difference>) :
        AssertionError("table content mismatch:\n${diff.prettyPrint()}")

    private fun dbUnitAssert(expected: SortedTable, actual: ITable) {
        dbTester.dbUnitConfig.diffFailureHandler.diffList.clear()
        try {
            assert(expected, actual, dbTester.dbUnitConfig)
        } catch (fail: DbComparisonFailure) {
            throw TableSizeMismatch(actual, fail)
        }
        if (dbTester.dbUnitConfig.diffFailureHandler.diffList.isNotEmpty()) {
            throw TableContentMismatch(dbTester.dbUnitConfig.diffFailureHandler.diffList as List<Difference>)
        }
    }

    protected fun assert(expected: SortedTable, actual: ITable, config: DbUnitConfig) {
        assertWithValueComparer(
            expected,
            actual,
            config.diffFailureHandler,
            config.valueComparer,
            config.columnValueComparers
        )
    }
}

private fun List<Difference>.prettyPrint(): String =
    associate { (""""${it.columnName}" in row ${it.rowIndex + 1}""") to it.failMessage }
        .toSortedMap().entries.joinToString("\n") { """${it.key}: ${it.value}""" }
