package com.adven.concordion.extensions.exam.db

import org.dbunit.dataset.Column
import org.dbunit.dataset.DefaultTable
import org.dbunit.dataset.IDataSet
import org.dbunit.dataset.ITable
import org.dbunit.dataset.builder.DataSetBuilder
import org.dbunit.dataset.datatype.DataType.UNKNOWN
import java.util.*
import java.util.Arrays.asList

class TableData(private val table: String, private val defaults: Map<String, Any?>, vararg columns: String) {
    private val columns: List<String> = asList(*columns)
    private var dataSetBuilder = DataSetBuilder()
    private var currentRow = 0

    constructor(table: String, vararg columns: String) : this(table, HashMap(), *columns)
    constructor(table: String, columns: Cols) : this(table, columns.defaults, *columns.cols.toTypedArray())

    private fun resolveValue(value: Any?): Any? {
        return if (value is IntProgression) {
            val list = value.toList()
            list[currentRow % list.size]
        } else value
    }

    fun row(vararg values: Any?): TableData {
        val rowBuilder = dataSetBuilder.newRow(table)
        val notDefaultsColumns = columns.filter { column -> !defaults.containsKey(column) }
        for (i in values.indices) {
            rowBuilder.with(notDefaultsColumns[i], values[i])
        }
        for ((key, value) in defaults) {
            rowBuilder.with(key, resolveValue(value))
        }
        dataSetBuilder = rowBuilder.add()
        currentRow++
        return this
    }

    fun rows(rows: List<List<Any?>>): TableData {
        rows.forEach { row(*it.toTypedArray()) }
        return this
    }

    fun build(): IDataSet {
        return dataSetBuilder.build()
    }

    fun table(): ITable {
        val dataSet = build()
        return if (dataSet.tableNames.isEmpty())
            DefaultTable(table, columns(columns))
        else
            dataSet.getTable(table)
    }

    private fun columns(c: List<String>): Array<Column?> {
        val columns = arrayOfNulls<Column>(c.size)
        for (i in c.indices) {
            columns[i] = Column(c[i], UNKNOWN)
        }
        return columns
    }

    data class Cols(val defaults: Map<String, Any?> = emptyMap(), val cols: List<String> = emptyList())

    companion object {
        fun filled(table: String, rows: List<List<Any?>>, cols: Cols): ITable {
            return TableData(table, cols).rows(rows).table()
        }
    }
}