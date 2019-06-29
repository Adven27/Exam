package com.adven.concordion.extensions.exam.db

import com.adven.concordion.extensions.exam.db.builder.DataSetBuilder
import org.dbunit.dataset.Column
import org.dbunit.dataset.DefaultTable
import org.dbunit.dataset.ITable
import org.dbunit.dataset.datatype.DataType.UNKNOWN

class TableData(private val table: String, private val columns: Map<String, Any?>) {
    private var dataSetBuilder = DataSetBuilder()
    private var currentRow = 0

    private fun resolveValue(value: Any?): Any? {
        return if (value is IntProgression) {
            val list = value.toList()
            list[currentRow % list.size]
        } else value
    }

    fun row(vararg values: Any?): TableData {
        val colsToSet = columns.filterValues { it == null }.keys
        validate(values, colsToSet)
        dataSetBuilder = dataSetBuilder.newRowTo(table)
            .withFields(columns.mapValues { resolveValue(it.value) } + colsToSet.zip(values).toMap())
            .add()
        currentRow++
        return this
    }

    private fun validate(values: Array<out Any?>, columns: Set<String>) {
        if (values.size != columns.size) {
            throw IllegalArgumentException(
                String.format(
                    "Number of columns (%s) for table %s is different than the number of provided values (%s)",
                    columns.size,
                    table,
                    values.size
                )
            )
        }
    }

    fun rows(rows: List<List<Any?>>): TableData {
        rows.forEach { row(*it.toTypedArray()) }
        return this
    }

    fun build() = dataSetBuilder.build()

    fun table(): ITable {
        val dataSet = build()
        return if (dataSet.tableNames.isEmpty())
            DefaultTable(table, columns(columns.keys))
        else
            dataSet.getTable(table)
    }

    private fun columns(c: Set<String>): Array<Column?> = c.map { Column(it, UNKNOWN) }.toTypedArray()

    companion object {
        fun filled(table: String, rows: List<List<Any?>>, cols: Map<String, Any?>) = TableData(table, cols).rows(rows).table()
    }
}