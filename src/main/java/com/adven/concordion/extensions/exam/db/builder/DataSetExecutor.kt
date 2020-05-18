package com.adven.concordion.extensions.exam.db.builder

import com.adven.concordion.extensions.exam.db.DbTester
import com.adven.concordion.extensions.exam.db.builder.CompareOperation.EQUALS
import org.concordion.api.Evaluator
import org.dbunit.Assertion
import org.dbunit.DatabaseUnitException
import org.dbunit.assertion.comparer.value.ValueComparer
import org.dbunit.database.AmbiguousTableNameException
import org.dbunit.dataset.*
import org.dbunit.dataset.csv.CsvDataSet
import org.dbunit.dataset.excel.XlsDataSet
import org.dbunit.dataset.filter.DefaultColumnFilter
import org.dbunit.dataset.filter.SequenceTableFilter
import org.dbunit.dataset.xml.FlatXmlDataSet
import org.dbunit.dataset.xml.FlatXmlDataSetBuilder
import org.slf4j.LoggerFactory
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.net.URL
import java.nio.file.Files
import java.util.*
import java.util.concurrent.atomic.AtomicBoolean

class DataSetExecutor(private val dbTester: DbTester) {
    private val printDBUnitConfig = AtomicBoolean(true)

    fun insertDataSet(dataSetConfig: DataSetConfig, eval: Evaluator) {
        printDBUnitConfiguration()
        var resultingDataSet: IDataSet? = null
        try {
            if (dataSetConfig.datasets.isNotEmpty()) {
                resultingDataSet = loadDataSet(dataSetConfig.datasets, eval)
                resultingDataSet = performTableOrdering(dataSetConfig, resultingDataSet)
                dataSetConfig.strategy.operation.execute(dbTester.connection, resultingDataSet)
            } else {
                log.warn("Database will not be populated because no dataset has been provided.")
            }
        } catch (e: Exception) {
            if (log.isDebugEnabled && resultingDataSet != null) {
                logDataSet(resultingDataSet, e)
            }
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

    private fun logDataSet(resultingDataSet: IDataSet, e: Exception) {
        try {
            val datasetFile = Files.createTempFile("dataset-log", ".xml").toFile()
            log.info("Saving current dataset to " + datasetFile.absolutePath)
            FileOutputStream(datasetFile).use { fos -> FlatXmlDataSet.write(resultingDataSet, fos) }
        } catch (e1: Exception) {
            log.error("Could not log created dataset.", e)
        }
    }

    /**
     * @param name one or more (comma separated) dataset names to instance
     * @return loaded dataset (in case of multiple dataSets they will be merged
     * in one using composite dataset)
     */
    @Throws(DataSetException::class, IOException::class)
    fun loadDataSet(name: String, eval: Evaluator): IDataSet {
        val dataSetNames = name.trim().split(",").toTypedArray()
        val dataSets: MutableList<IDataSet> = ArrayList()
        val sensitiveTableNames = dbTester.dbUnitConfig.isCaseSensitiveTableNames()
        dataSetNames.forEach { dataSet ->
            var target: IDataSet? = null
            val dataSetName = dataSet.trim()
            when (dataSetName.substring(dataSetName.lastIndexOf('.') + 1).toLowerCase()) {
                "xml" -> {
                    target = try {
                        ExamDataSet(sensitiveTableNames, FlatXmlDataSetBuilder()
                            .setColumnSensing(dbTester.dbUnitConfig.isColumnSensing)
                            .setCaseSensitiveTableNames(sensitiveTableNames)
                            .build(getDataSetUrl(dataSetName)), eval)
                    } catch (e: Exception) {
                        ExamDataSet(sensitiveTableNames, FlatXmlDataSetBuilder()
                            .setColumnSensing(dbTester.dbUnitConfig.isColumnSensing)
                            .setCaseSensitiveTableNames(sensitiveTableNames)
                            .build(getDataSetStream(dataSetName)), eval)
                    }
                }
                "csv" -> {
                    target = ExamDataSet(sensitiveTableNames, CsvDataSet(
                        File(javaClass.classLoader.getResource(dataSetName).file).parentFile), eval)
                }
                "xls" -> {
                    target = ExamDataSet(sensitiveTableNames, XlsDataSet(getDataSetStream(dataSetName)), eval)
                }
                "json" -> {
                    target = ExamDataSet(sensitiveTableNames, JSONDataSet(getDataSetStream(dataSetName)), eval)
                }
                else -> log.error("Unsupported dataset extension")
            }
            if (target != null) {
                dataSets.add(target)
            }
        }
        if (dataSets.isEmpty()) {
            throw RuntimeException("No dataset loaded for name $name")
        }
        return CompositeDataSet(dataSets.toTypedArray(), true, sensitiveTableNames)
    }

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
    private fun performTableOrdering(dataSet: DataSetConfig, target: IDataSet?): IDataSet? {
        var ordered = target
        if (dataSet.tableOrdering.isNotEmpty()) {
            ordered = FilteredDataSet(SequenceTableFilter(dataSet.tableOrdering, dbTester.dbUnitConfig.isCaseSensitiveTableNames()), ordered)
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
        expectedDataSetConfig: DataSetConfig, eval: Evaluator, excludeCols: Array<String> = emptyArray(), orderBy: Array<String>? = null, compareOperation: CompareOperation = EQUALS
    ) {
        val current: IDataSet = dbTester.connection.createDataSet()
        val expected: IDataSet = loadDataSet(expectedDataSetConfig.datasets, eval)
        try {
            expected.tableNames
        } catch (e: DataSetException) {
            throw RuntimeException("Could not extract dataset table names.", e)
        }.forEach { tableName ->
            var expectedTable: ITable? = null
            var actualTable: ITable? = null
            try {
                expectedTable = expected.getTable(tableName)
                actualTable = current.getTable(tableName)
            } catch (e: DataSetException) {
                throw RuntimeException("DataSet comparison failed due to following exception: ", e)
            }
            if (orderBy != null && orderBy.isNotEmpty()) {
                val validOrderByColumns: MutableList<String> = ArrayList() //gather valid columns for sorting expected dataset
                for (i in 0 until expectedTable.rowCount) {
                    for (orderColumn in orderBy) {
                        try {
                            validOrderByColumns.add(orderColumn) //add only existing columns on current table
                        } catch (ignored: NullPointerException) {
                        }
                    }
                }
                expectedTable = SortedTable(expectedTable, validOrderByColumns.toTypedArray())
                actualTable = SortedTable(actualTable, validOrderByColumns.toTypedArray())
            }
            var filteredActualTable = DefaultColumnFilter.includedColumnsTable(actualTable,
                expectedTable!!.tableMetaData.columns)
            if (compareOperation == CompareOperation.CONTAINS) {
                filteredActualTable = ContainsFilterTable(filteredActualTable, expectedTable, listOf(*excludeCols))
            }
            Assertion.assertWithValueComparer(
                expectedTable,
                filteredActualTable,
                dbTester.dbUnitConfig.diffFailureHandler,
                dbTester.dbUnitConfig.valueComparer,
                dbTester.dbUnitConfig.columnValueComparers as Map<String?, ValueComparer?>
            )
        }
    }

    companion object {
        private val log = LoggerFactory.getLogger(DataSetExecutor::class.java)
    }
}