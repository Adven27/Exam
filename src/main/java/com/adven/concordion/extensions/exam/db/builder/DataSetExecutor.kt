package com.adven.concordion.extensions.exam.db.builder

import com.adven.concordion.extensions.exam.core.fileExt
import com.adven.concordion.extensions.exam.core.utils.findResource
import com.adven.concordion.extensions.exam.db.DbTester
import com.adven.concordion.extensions.exam.db.builder.CompareOperation.EQUALS
import com.adven.concordion.extensions.exam.db.commands.columnNamesArray
import com.adven.concordion.extensions.exam.db.commands.sortedTable
import org.concordion.api.Evaluator
import org.dbunit.Assertion
import org.dbunit.DatabaseUnitException
import org.dbunit.assertion.DbComparisonFailure
import org.dbunit.assertion.Difference
import org.dbunit.assertion.comparer.value.ValueComparer
import org.dbunit.database.AmbiguousTableNameException
import org.dbunit.dataset.*
import org.dbunit.dataset.csv.CsvDataSet
import org.dbunit.dataset.excel.XlsDataSet
import org.dbunit.dataset.filter.DefaultColumnFilter
import org.dbunit.dataset.filter.SequenceTableFilter
import org.dbunit.dataset.xml.FlatXmlDataSetBuilder
import org.slf4j.LoggerFactory
import java.io.File
import java.io.IOException
import java.io.InputStream
import java.net.URL
import java.util.concurrent.atomic.AtomicBoolean

class DataSetExecutor(private val dbTester: DbTester) {
    private val printDBUnitConfig = AtomicBoolean(true)

    fun insertDataSet(dataSetConfig: DataSetConfig, eval: Evaluator): IDataSet {
        if (dataSetConfig.debug) {
            printDBUnitConfiguration()
        }
        return try {
            performTableOrdering(loadDataSets(eval, dataSetConfig.datasets), dataSetConfig.tableOrdering).apply {
                dataSetConfig.strategy.operation.execute(dbTester.connection, this)
            }
        } catch (e: Exception) {
            throw DataBaseSeedingException("Could not initialize dataset: $dataSetConfig", e)
        }
    }

    private fun printDBUnitConfiguration() {
        if (printDBUnitConfig.compareAndSet(true, false)) {
            val sb = StringBuilder(150)
            sb //.append("caseInsensitiveStrategy: ").append(dbTester.getDbUnitConfig().getCaseInsensitiveStrategy()).append("\n")
                //.append("leakHunter: ").append("" + dbUnitConfig.isLeakHunter()).append("\n")
                .append("columnSensing: ").append("" + dbTester.dbUnitConfig.isColumnSensing).append("\n")
            for ((key, value) in dbTester.dbUnitConfig.databaseConfigProperties) {
                sb.append(key).append(": ").append(value ?: "").append("\n")
            }
            log.info(String.format("DBUnit configuration for dataset executor '%s':\n$sb", dbTester))
        }
    }

    /**
     * @param dataSetNames one or more dataset names to instantiate
     * @return loaded dataset (in case of multiple dataSets they will be merged
     * in one using composite dataset)
     */
    @Throws(DataSetException::class, IOException::class)
    fun loadDataSets(eval: Evaluator, dataSetNames: List<String>): IDataSet {
        val sensitiveTableNames = dbTester.dbUnitConfig.isCaseSensitiveTableNames()
        return CompositeDataSet(
            load(dataSetNames, sensitiveTableNames, eval).toTypedArray(),
            true,
            sensitiveTableNames
        )
    }

    private fun load(dataSetNames: List<String>, sensitiveTableNames: Boolean, eval: Evaluator) =
        dataSetNames.mapNotNull { dataSet ->
            val name = dataSet.trim()
            when (name.fileExt()) {
                "xml" -> {
                    try {
                        FlatXmlDataSetBuilder()
                            .setColumnSensing(dbTester.dbUnitConfig.isColumnSensing)
                            .setCaseSensitiveTableNames(sensitiveTableNames)
                            .build(getDataSetUrl(name))
                    } catch (e: Exception) {
                        FlatXmlDataSetBuilder()
                            .setColumnSensing(dbTester.dbUnitConfig.isColumnSensing)
                            .setCaseSensitiveTableNames(sensitiveTableNames)
                            .build(getDataSetStream(name))
                    }
                }
                "json" -> JSONDataSet(getDataSetStream(name))
                "xls" -> XlsDataSet(getDataSetStream(name))
                "csv" -> CsvDataSet(File(name.findResource().file).parentFile)
                else -> {
                    log.error("Unsupported dataset extension")
                    null
                }
            }
        }.map { ExamDataSet(sensitiveTableNames, it, eval) }.ifEmpty { throw RuntimeException("No dataset loaded for $dataSetNames") }

    private fun getDataSetUrl(ds: String): URL {
        var dataSet = ds
        if (!dataSet.startsWith("/")) {
            dataSet = "/$dataSet"
        }
        var resource = javaClass.getResource(dataSet)
        if (resource == null) { // if not found try to get from datasets folder
            resource = javaClass.getResource("/datasets$dataSet")
        }
        if (resource == null) {
            throw RuntimeException(String.format("Could not find dataset '%s' under 'resources' or 'resources/datasets' directory.",
                dataSet.substring(1)))
        }
        return resource
    }

    @Throws(AmbiguousTableNameException::class)
    private fun performTableOrdering(target: IDataSet, tableOrdering: List<String>): IDataSet {
        var ordered = target
        if (tableOrdering.isNotEmpty()) {
            ordered = FilteredDataSet(
                SequenceTableFilter(tableOrdering.toTypedArray(), dbTester.dbUnitConfig.isCaseSensitiveTableNames()),
                ordered
            )
        }
        return ordered
    }

    private fun getDataSetStream(ds: String): InputStream {
        var dataSet = ds
        if (!dataSet.startsWith("/")) {
            dataSet = "/$dataSet"
        }
        var `is` = javaClass.getResourceAsStream(dataSet)
        if (`is` == null) { // if not found try to get from datasets folder
            `is` = javaClass.getResourceAsStream("/datasets$dataSet")
        }
        if (`is` == null) {
            throw RuntimeException(String.format("Could not find dataset '%s' under 'resources' or 'resources/datasets' directory.",
                dataSet.substring(1)))
        }
        return `is`
    }

    @JvmOverloads
    @Throws(DatabaseUnitException::class)
    fun compareCurrentDataSetWith(
        expectedDataSetConfig: DataSetConfig, eval: Evaluator, excludeCols: Array<String> = emptyArray(), orderBy: Array<String> = emptyArray(), compareOperation: CompareOperation = EQUALS
    ): DataSetsCompareResult {
        dbTester.dbUnitConfig.diffFailureHandler.diffList.clear()
        val rowsMismatchFailures = mutableListOf<Triple<ITable, ITable, DbComparisonFailure>>();
        val current: IDataSet = dbTester.connection.createDataSet()
        val expected: IDataSet = loadDataSets(eval, expectedDataSetConfig.datasets)
        return expected.tableNames.map { tableName ->
            var expectedTable = expected.getTable(tableName)
            val sortCols: Array<String> = if (orderBy.isEmpty()) expectedTable.columnNamesArray() else orderBy
            expectedTable = sortedTable(expectedTable, sortCols, dbTester.dbUnitConfig.overrideRowSortingComparer)
            var actualTable = DefaultColumnFilter.includedColumnsTable(
                sortedTable(current.getTable(tableName), sortCols, dbTester.dbUnitConfig.overrideRowSortingComparer),
                expectedTable.tableMetaData.columns
            )
            if (compareOperation == CompareOperation.CONTAINS) {
                actualTable = ContainsFilterTable(actualTable, expectedTable, listOf(*excludeCols))
            }
            try {
                Assertion.assertWithValueComparer(
                    expectedTable,
                    actualTable,
                    dbTester.dbUnitConfig.diffFailureHandler,
                    dbTester.dbUnitConfig.valueComparer,
                    dbTester.dbUnitConfig.columnValueComparers as Map<String?, ValueComparer?>
                )
            } catch (f: DbComparisonFailure) {
                rowsMismatchFailures.add(Triple(expectedTable, actualTable, f))
            }
            expectedTable to actualTable
        }.toMap().let {
            DataSetsCompareResult(
                CompositeDataSet(it.keys.toTypedArray()),
                CompositeDataSet(it.values.toTypedArray()),
                dbTester.dbUnitConfig.diffFailureHandler.diffList as List<Difference>,
                rowsMismatchFailures
            ).apply {
                log.info(this.toString())
            }
        }
    }

    data class DataSetsCompareResult(
        val expected: IDataSet,
        val actual: IDataSet,
        val diff: List<Difference>,
        val rowsMismatch: List<Triple<ITable, ITable, DbComparisonFailure>>
    )

    companion object {
        private val log = LoggerFactory.getLogger(DataSetExecutor::class.java)
    }
}