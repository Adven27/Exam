package com.adven.concordion.extensions.exam.db.builder

import org.dbunit.dataset.Column
import org.dbunit.dataset.DataSetException
import org.dbunit.dataset.DefaultTableMetaData
import org.dbunit.dataset.ITableMetaData
import org.dbunit.dataset.datatype.DataType
import java.util.HashMap
import java.util.LinkedHashMap

class TableMetaDataBuilder(private val tableName: String, private val policy: IStringPolicy) {
    private val keysToColumns = LinkedHashMap<String, Column>()

    fun with(metaData: ITableMetaData): TableMetaDataBuilder = with(*metaData.columns)

    fun with(vararg columns: Column): TableMetaDataBuilder {
        columns.forEach { with(it) }
        return this
    }

    fun with(column: Column): TableMetaDataBuilder {
        if (column.isUnknown()) {
            add(column)
        }
        return this
    }

    private fun add(column: Column) {
        keysToColumns[toKey(column)] = column
    }

    fun numberOfColumns(): Int = keysToColumns.size

    fun build(): ITableMetaData = DefaultTableMetaData(tableName, columns())

    private fun toKey(column: Column): String = policy.toKey(column.columnName)
    private fun Column.isUnknown(): Boolean = !this.isKnown()
    private fun Column.isKnown(): Boolean = keysToColumns.containsKey(toKey(this))
    private fun columns(): Array<Column> = keysToColumns.values.toTypedArray()
}

class DataRowBuilder(private val dataSet: DataSetBuilder, tableName: String) : BasicDataRowBuilder(tableName) {

    override fun with(columnName: String, value: Any?): DataRowBuilder {
        put(columnName, value)
        return this
    }

    fun withFields(fields: Map<String, Any?>): DataRowBuilder {
        fields.forEach { (col, value) -> with(col, value) }
        return this
    }

    @Throws(DataSetException::class)
    fun add(): DataSetBuilder {
        dataSet.add(this)
        return dataSet
    }
}

open class BasicDataRowBuilder(val tableName: String) {
    private val columnNameToValue: HashMap<String, Any?> = LinkedHashMap()
    private var allColumnNames = emptyArray<String>()
    private val defaultValues = HashMap<String, Any?>()

    /**
     * Added the column to the Data.
     *
     * @param columnName the name of the column.
     * @param value      the value the column should have.
     * @return the current object.
     */
    open fun with(columnName: String, value: Any?): BasicDataRowBuilder {
        columnNameToValue[columnName] = value
        return this
    }

    /**
     * Define all values of the table with null or the defaultvalue.
     * All columns are defined with [.setAllColumnNames].
     * For not null columns you must define default values via
     * [.addDefaultValue].
     *
     * @return the current object.
     * @author niels
     * @since 2.4.10
     */
    fun fillUndefinedColumns(): BasicDataRowBuilder {
        for (column in allColumnNames) {
            if (!columnNameToValue.containsKey(column)) {
                columnNameToValue[column] = defaultValues[column]
            }
        }
        return this
    }

    fun values(columns: Array<Column>): Array<Any?> = columns.map { getValue(it) }.toTypedArray()

    fun toMetaData(): ITableMetaData = createMetaData(columnNameToValue.keys.map { createColumn(it) })

    private fun createMetaData(columns: List<Column>): ITableMetaData =
            DefaultTableMetaData(tableName, columns.toTypedArray())

    private fun createColumn(columnName: String) = Column(columnName, DataType.UNKNOWN)

    protected fun put(columnName: String, value: Any?) {
        columnNameToValue[columnName] = value
    }

    protected fun getValue(column: Column): Any? {
        return getValue(column.columnName)
    }

    protected fun getValue(columnName: String): Any? {
        return columnNameToValue[columnName]
    }
}