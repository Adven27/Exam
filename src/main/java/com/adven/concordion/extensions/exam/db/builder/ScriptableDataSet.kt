package com.adven.concordion.extensions.exam.db.builder

import com.adven.concordion.extensions.exam.core.resolveToObj
import org.concordion.api.Evaluator
import org.dbunit.dataset.*
import org.dbunit.operation.CompositeOperation
import org.dbunit.operation.DatabaseOperation
import org.slf4j.LoggerFactory
import java.util.*
import java.util.logging.Level
import java.util.logging.Logger
import java.util.regex.Pattern
import javax.script.ScriptEngine
import javax.script.ScriptEngineManager
import javax.script.ScriptException

class ScriptableDataSet(caseSensitiveTableNames: Boolean, private val delegate: IDataSet) : AbstractDataSet(caseSensitiveTableNames) {
    @Throws(DataSetException::class)
    override fun createIterator(reversed: Boolean): ITableIterator {
        return ScriptableDataSetIterator(if (reversed) delegate.reverseIterator() else delegate.iterator())
    }

}

class ScriptableDataSetIterator(private val delegate: ITableIterator) : ITableIterator {
    @Throws(DataSetException::class)
    override fun next(): Boolean {
        return delegate.next()
    }

    @Throws(DataSetException::class)
    override fun getTableMetaData(): ITableMetaData {
        return delegate.tableMetaData
    }

    @Throws(DataSetException::class)
    override fun getTable(): ITable {
        return ScriptableTable(delegate.table)
    }

}

class ScriptableTable(private val delegate: ITable) : ITable {
    var manager: ScriptEngineManager
    private val engines: MutableMap<String, ScriptEngine>
    override fun getTableMetaData(): ITableMetaData {
        return delegate.tableMetaData
    }

    override fun getRowCount(): Int {
        return delegate.rowCount
    }

    @Throws(DataSetException::class)
    override fun getValue(row: Int, column: String): Any {
        val value = delegate.getValue(row, column)
        if (value != null && scriptEnginePattern.matcher(value.toString()).matches()) {
            val engine = getScriptEngine(value.toString().trim { it <= ' ' })
            if (engine != null) {
                try {
                    return getScriptResult(value.toString(), engine)
                } catch (e: Exception) {
                    log.log(Level.WARNING, String.format("Could not evaluate script expression for table '%s', column '%s'. The original value will be used.", tableMetaData.tableName, column), e)
                }
            }
        }
        return value
    }

    /**
     * Parses table cell to get script engine
     *
     * @param value the table cell
     * @return scriptEngine
     */
    private fun getScriptEngine(value: String): ScriptEngine? {
        val engineName = value.substring(0, value.indexOf(':'))
        return if (engines.containsKey(engineName)) {
            engines[engineName]
        } else {
            val engine = manager.getEngineByName(engineName)
            if (engine != null) {
                engines[engineName] = engine
            } else {
                log.warning(String.format("Could not find script engine by name '%s'", engineName))
            }
            engine
        }
    }

    /**
     * Evaluates the script expression
     *
     * @return script expression result
     */
    @Throws(ScriptException::class)
    private fun getScriptResult(script: String, engine: ScriptEngine): Any {
        val scriptToExecute = script.substring(script.indexOf(':') + 1)
        return engine.eval(scriptToExecute)
    }

    companion object {
        //any non digit char (except 'regex') followed by ':' followed by 1 or more chars e.g: js: new Date().toString()
        private val scriptEnginePattern = Pattern.compile("^(?!regex)[a-zA-Z]+:.+")
        var log = Logger.getLogger(ScriptableTable::class.java.name)
    }

    init {
        engines = HashMap()
        manager = ScriptEngineManager()
    }
}


class ExamDataSet(caseSensitiveTableNames: Boolean = false, private val delegate: IDataSet, val eval: Evaluator) : AbstractDataSet(caseSensitiveTableNames) {
    constructor(table: ITable, eval: Evaluator, caseSensitiveTableNames: Boolean = false) : this(caseSensitiveTableNames, DefaultDataSet(table), eval)

    @Throws(DataSetException::class)
    override fun createIterator(reversed: Boolean): ITableIterator {
        return ExamDataSetIterator(if (reversed) delegate.reverseIterator() else delegate.iterator(), eval)
    }
}

class ExamDataSetIterator(private val delegate: ITableIterator, val eval: Evaluator) : ITableIterator {
    @Throws(DataSetException::class)
    override fun next(): Boolean {
        return delegate.next()
    }

    @Throws(DataSetException::class)
    override fun getTableMetaData(): ITableMetaData {
        return delegate.tableMetaData
    }

    @Throws(DataSetException::class)
    override fun getTable(): ITable {
        return ExamTable(delegate.table, eval)
    }

}

class ExamTable(private val delegate: ITable, private val eval: Evaluator) : ITable {
    override fun getTableMetaData(): ITableMetaData {
        return delegate.tableMetaData
    }

    override fun getRowCount(): Int {
        return delegate.rowCount
    }

    @Throws(DataSetException::class)
    override fun getValue(row: Int, column: String): Any? {
        val value = delegate.getValue(row, column)
        return when {
            value == null -> null
            value is String && value.isRange() -> value.toRange().toList().let { it[row % it.size] }
            value is String && !value.startsWith("!{") -> eval.resolveToObj(value)
            else -> value
        }
    }

    private fun String.isRange() = this.matches("^[0-9]+[.]{2}[0-9]+$".toRegex())

    private fun String.toRange(): IntProgression = when {
        this.isRange() -> {
            val (start, end) = this.split("[.]{2}".toRegex()).map(String::toInt)
            IntProgression.fromClosedRange(start, end, end.compareTo(start))
        }
        else -> throw IllegalArgumentException("Couldn't parse range from string $this")
    }
}

class ContainsFilterTable(actualTable: ITable?, expectedTable: ITable?, ignoredCols: List<String>) : ITable {
    private val originalTable: ITable

    /** mapping of filtered rows, i.e, each entry on this list has the value of
     * the index on the original table corresponding to the desired index.
     * For instance, if the original table is:
     * row Value
     * 0    v1
     * 1    v2
     * 2    v3
     * 3    v4
     * And the expected values are:
     * row Value
     * 0   v2
     * 1   v4
     * The new table should be:
     * row Value
     * 0    v2
     * 1    v4
     * Consequently, the mapping will be {1, 3}
     */
    private val filteredRowIndexes: List<Int>

    /**
     * logger
     */
    private val logger = LoggerFactory.getLogger(RowFilterTable::class.java)
    private fun toUpper(ignoredCols: List<String>): List<String> {
        val upperCaseColumns: MutableList<String> = ArrayList()
        for (ignoredCol in ignoredCols) {
            upperCaseColumns.add(ignoredCol.toUpperCase())
        }
        return upperCaseColumns
    }

    @Throws(DataSetException::class)
    private fun setRows(expectedTable: ITable, ignoredCols: List<String>): List<Int> {
        val tableMetadata = originalTable.tableMetaData
        logger.debug("Setting rows for table {}", tableMetadata.tableName)
        val fullSize = expectedTable.rowCount
        val columns: MutableList<String> = ArrayList()
        if (fullSize > 0) {
            for (column in expectedTable.tableMetaData.columns) {
                columns.add(column.columnName)
            }
        }
        val filteredRowIndexes: MutableList<Int> = ArrayList()
        for (row in 0 until fullSize) {
            val values: MutableList<Any?> = ArrayList()
            for (column in columns) {
                values.add(expectedTable.getValue(row, column))
            }
            val actualRowIndex = tableContains(columns, values, filteredRowIndexes, ignoredCols)
            if (actualRowIndex == null) {
                logger.debug("Discarding row {}", row)
                continue
            }
            logger.debug("Adding row {}", row)
            filteredRowIndexes.add(actualRowIndex)
        }
        return filteredRowIndexes
    }

    /**
     * Searches for full match in original table by values from expected table
     * @param columns column names
     * @param values column values
     * @param filteredRowIndexes list of row indexes already found by previous runs
     * @return row index of original table containing all requested values
     * @throws DataSetException throws DataSetException
     */
    @Throws(DataSetException::class)
    private fun tableContains(columns: List<String>, values: List<Any?>, filteredRowIndexes: List<Int>, ignoredCols: List<String>?): Int? {
        val fullSize = originalTable.rowCount
        for (row in 0 until fullSize) {
            var match = true
            for (column in columns.indices) {
                if (ignoredCols != null && ignoredCols.contains(columns[column].toUpperCase())) {
                    continue
                }
                if (values[column] != null && values[column].toString().startsWith("regex:")) {
                    if (!regexMatches(values[column].toString(), originalTable.getValue(row, columns[column]).toString())) {
                        match = false
                        break
                    }
                    continue
                }
                val columnIndex = originalTable.tableMetaData.getColumnIndex(columns[column])
                val dataType = originalTable.tableMetaData.columns[columnIndex].dataType
                if (dataType.compare(values[column], originalTable.getValue(row, columns[column])) != 0) {
                    match = false
                    break
                }
            }
            if (match && !filteredRowIndexes.contains(row)) {
                return row
            }
        }
        return null
    }

    private fun regexMatches(expectedValue: String, actualValue: String): Boolean {
        val pattern = Pattern.compile(expectedValue.substring(expectedValue.indexOf(':') + 1).trim { it <= ' ' })
        return pattern.matcher(actualValue).matches()
    }

    override fun getTableMetaData(): ITableMetaData {
        logger.debug("getTableMetaData() - start")
        return originalTable.tableMetaData
    }

    override fun getRowCount(): Int {
        logger.debug("getRowCount() - start")
        return filteredRowIndexes.size
    }

    @Throws(DataSetException::class)
    override fun getValue(row: Int, column: String): Any {
        if (logger.isDebugEnabled) logger.debug("getValue(row={}, columnName={}) - start", Integer.toString(row), column)
        val max = filteredRowIndexes.size
        return if (row < max) {
            val realRow = filteredRowIndexes[row]
            originalTable.getValue(realRow, column)
        } else {
            throw RowOutOfBoundsException("tried to access row " + row +
                " but rowCount is " + max)
        }
    }

    /**
     * Creates a new [ITable] where some rows can be filtered out from the original table
     * @param actualTable The table to be wrapped
     * @param expectedTable actualTable will be filtered by this table
     * @throws DataSetException throws DataSetException
     */
    init {
        require(!(expectedTable == null || actualTable == null)) { "Constructor cannot receive null arguments" }
        originalTable = actualTable
        // sets the rows for the new table
        // NOTE: this conversion might be an issue for long tables, as it iterates for
        // all values of the original table and that might take time and memory leaks.
        filteredRowIndexes = setRows(expectedTable, toUpper(ignoredCols))
    }
}

class DataSetImpl(private var value: String, private var strategy: SeedStrategy, private var useSequenceFiltering: Boolean, private var tableOrdering: Array<String>, private var disableConstraints: Boolean, private var fillIdentityColumns: Boolean) {

    fun value(): String {
        return value
    }

    fun strategy(): SeedStrategy {
        return strategy
    }

    fun useSequenceFiltering(): Boolean {
        return useSequenceFiltering
    }

    fun tableOrdering(): Array<String> {
        return tableOrdering
    }

    fun disableConstraints(): Boolean {
        return disableConstraints
    }

    fun fillIdentityColumns(): Boolean {
        return fillIdentityColumns
    }
}

/**
 * Same as arquillian persistence: https://docs.jboss.org/author/display/ARQ/Persistence
 * Data insert strategies
 * DBUnit, and hence Arquillian Persistence Extension, provides following strategies for inserting data
 *
 *
 * INSERT
 * Performs insert of the data defined in provided data sets. This is the default strategy.
 *
 *
 * CLEAN_INSERT
 * Performs insert of the data defined in provided data sets, after removal of all data present in the tables (DELETE_ALL invoked by DBUnit before INSERT).
 *
 *
 * REFRESH
 * During this operation existing rows are updated and new ones are inserted. Entries already existing in the database which are not defined in the provided data set are not affected.
 *
 *
 * UPDATE
 * This strategy updates existing rows using data provided in the datasets. If dataset contain a row which is not present in the database (identified by its primary key) then exception is thrown.
 */
enum class SeedStrategy(val operation: DatabaseOperation) {
    CLEAN_INSERT(DatabaseOperation.CLEAN_INSERT),
    TRUNCATE_INSERT(CompositeOperation(DatabaseOperation.TRUNCATE_TABLE, DatabaseOperation.INSERT)),
    INSERT(DatabaseOperation.INSERT), REFRESH(DatabaseOperation.REFRESH), UPDATE(DatabaseOperation.UPDATE);

}

enum class CompareOperation {
    EQUALS, CONTAINS
}

class DataBaseSeedingException(message: String?, cause: Throwable?) : RuntimeException(message, cause)