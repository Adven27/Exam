package com.adven.concordion.extensions.exam.db.builder

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import mu.KLogging
import org.dbunit.dataset.AbstractDataSet
import org.dbunit.dataset.Column
import org.dbunit.dataset.DataSetException
import org.dbunit.dataset.DefaultTable
import org.dbunit.dataset.DefaultTableIterator
import org.dbunit.dataset.DefaultTableMetaData
import org.dbunit.dataset.IDataSet
import org.dbunit.dataset.ITable
import org.dbunit.dataset.ITableIterator
import org.dbunit.dataset.ITableMetaData
import org.dbunit.dataset.datatype.DataType.UNKNOWN
import org.dbunit.dataset.stream.DataSetProducerAdapter
import org.dbunit.dataset.stream.IDataSetConsumer
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.io.OutputStreamWriter
import java.nio.charset.StandardCharsets

class JSONWriter(private val dataSet: IDataSet, outputStream: OutputStream?) : IDataSetConsumer {
    private val out: OutputStreamWriter = OutputStreamWriter(outputStream, StandardCharsets.UTF_8)
    private var metaData: ITableMetaData? = null
    private var tableCount = 0
    private var rowCount = 0

    @Throws(DataSetException::class)
    override fun startDataSet() {
        try {
            tableCount = 0
            out.write("{$NEW_LINE")
        } catch (e: IOException) {
            logger.warn("Could not start dataset.", e)
        }
    }

    @Throws(DataSetException::class)
    override fun endDataSet() {
        try {
            out.write("}")
            out.flush()
        } catch (e: IOException) {
            logger.warn("Could not end dataset.", e)
        }
    }

    @Throws(DataSetException::class)
    override fun startTable(metaData: ITableMetaData) {
        this.metaData = metaData
        rowCount = 0
        try {
            out.write(DOUBLE_SPACES + "\"" + metaData.tableName + "\": [" + NEW_LINE)
        } catch (e: IOException) {
            logger.warn("Could not start table.", e)
        }
    }

    @Throws(DataSetException::class)
    override fun endTable() {
        try {
            tableCount++
            if (dataSet.tableNames.size == tableCount) {
                out.write("$DOUBLE_SPACES]$NEW_LINE")
            } else {
                out.write("$DOUBLE_SPACES],$NEW_LINE")
            }
        } catch (e: IOException) {
            logger.warn("Could end table.", e)
        }
    }

    override fun row(values: Array<Any?>) {
        rowCount++
        try {
            out.write("$FOUR_SPACES{$NEW_LINE")
            val sb = createSetFromValues(values)
            out.write(sb)
            if (dataSet.getTable(metaData!!.tableName).rowCount != rowCount) {
                out.write("$FOUR_SPACES},$NEW_LINE")
            } else {
                out.write("$FOUR_SPACES}$NEW_LINE")
            }
        } catch (expected: Exception) {
            logger.warn("Could not write row.", expected)
        }
    }

    private fun createSetFromValues(values: Array<Any?>): String {
        val sb = StringBuilder()
        for (i in values.indices) {
            val currentValue = values[i]
            if (currentValue != null) {
                val currentColumn = metaData!!.columns[i]
                sb.append(FOUR_SPACES + DOUBLE_SPACES + '"').append(metaData!!.columns[i].columnName).append("\": ")
                val isNumber = currentColumn.dataType.isNumber
                if (!isNumber) {
                    sb.append('"')
                }
                sb.append(currentValue.toString().replace(NEW_LINE.toRegex(), "\\\\n"))
                if (!isNumber) {
                    sb.append('"')
                }
                if (i != values.size - 1) {
                    sb.append(',')
                }
                sb.append(NEW_LINE)
            }
        }
        return replaceExtraCommaInTheEnd(sb)
    }

    private fun replaceExtraCommaInTheEnd(sb: StringBuilder): String {
        val indexOfPenultimateSymbol = sb.length - 2
        if (sb.length > 1 && sb[indexOfPenultimateSymbol] == ',') {
            sb.deleteCharAt(indexOfPenultimateSymbol)
        }
        return sb.toString()
    }

    @Synchronized
    fun write() {
        val provider = DataSetProducerAdapter(dataSet)
        provider.setConsumer(this)
        provider.produce()
    }

    companion object : KLogging() {
        private val NEW_LINE = System.getProperty("line.separator")
        private const val DOUBLE_SPACES = "  "
        private const val FOUR_SPACES = DOUBLE_SPACES + DOUBLE_SPACES
    }
}

/**
 * DBUnit DataSet format for JSON based datasets. It is similar to the flat XML
 * layout, but has some improvements (columns are calculated by parsing the
 * entire dataset, not just the first row).
 */
class JSONDataSet : AbstractDataSet {
    private val tableParser = JSONITableParser(TableParser())
    val mapper = jacksonObjectMapper()
    private var tables: List<ITable>

    constructor(file: File) {
        tables = tableParser.getTables(file)
    }

    constructor(`is`: InputStream) {
        tables = tableParser.getTables(`is`)
    }

    @Throws(DataSetException::class)
    override fun createIterator(reverse: Boolean): ITableIterator {
        return DefaultTableIterator(tables.toTypedArray())
    }

    private inner class JSONITableParser(val parser: TableParser) {
        fun getTables(jsonFile: File): List<ITable> = getTables(FileInputStream(jsonFile))
        fun getTables(jsonStream: InputStream): List<ITable> = parser.fillTable(mapper.readValue(jsonStream))
    }
}

class TableParser {
    fun fillTable(dataset: Map<String, List<Map<String, Any?>>>): List<ITable> = dataset.map { (key, value) ->
        val table = DefaultTable(getMetaData(key, value))
        for ((rowIndex, row) in value.withIndex()) {
            fillRow(table, row, rowIndex)
        }
        table
    }

    private fun getMetaData(tableName: String, rows: List<Map<String, Any?>>): ITableMetaData =
        DefaultTableMetaData(tableName, rows.flatMap { row -> row.keys }.toSet().map { Column(it, UNKNOWN) }.toTypedArray())

    private fun fillRow(table: DefaultTable, row: Map<String, Any?>, rowIndex: Int) {
        if (row.entries.isNotEmpty()) {
            table.addRow()
            row.forEach { (col, value) -> table.setValue(rowIndex, col, value) }
        }
    }
}
