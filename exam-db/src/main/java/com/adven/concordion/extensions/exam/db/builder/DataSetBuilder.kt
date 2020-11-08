package com.adven.concordion.extensions.exam.db.builder

import org.dbunit.dataset.CachedDataSet
import org.dbunit.dataset.IDataSet
import org.dbunit.dataset.ITableMetaData
import org.dbunit.dataset.stream.BufferedConsumer
import org.dbunit.dataset.stream.IDataSetConsumer
import java.util.HashMap

@Suppress("TooManyFunctions")
class DataSetBuilder(private val stringPolicy: IStringPolicy = CaseInsensitiveStringPolicy()) : IDataSetManipulator {
    private var dataSet = CachedDataSet()
    private var consumer: IDataSetConsumer = BufferedConsumer(dataSet)
    private val tableNameToMetaData = HashMap<String, TableMetaDataBuilder>()
    private var currentTableName: String? = null

    init {
        consumer.startDataSet()
    }

    fun newRowTo(tableName: String): DataRowBuilder {
        return DataRowBuilder(this, tableName)
    }

    fun build(): IDataSet {
        endTableIfNecessary()
        consumer.endDataSet()
        return dataSet
    }

    /**
     * {@inheritDoc}
     */
    override fun add(row: BasicDataRowBuilder): DataSetBuilder {
        row.fillUndefinedColumns()
        notifyConsumer(extractValues(row, updateTableMetaData(row)))
        return this
    }

    private fun extractValues(row: BasicDataRowBuilder, metaData: ITableMetaData): Array<Any?> =
        row.values(metaData.columns)

    private fun notifyConsumer(values: Array<Any?>) = consumer.row(values)

    private fun updateTableMetaData(row: BasicDataRowBuilder): ITableMetaData {
        val builder = metaDataBuilderFor(row.tableName)
        val previousNumberOfColumns = builder.numberOfColumns()

        val metaData = builder.with(row.toMetaData()).build()
        val newNumberOfColumns = metaData.columns.size

        val addedNewColumn = newNumberOfColumns > previousNumberOfColumns
        handleTable(metaData, addedNewColumn)

        return metaData
    }

    private fun handleTable(metaData: ITableMetaData, addedNewColumn: Boolean) {
        when {
            isNewTable(metaData.tableName) -> {
                endTableIfNecessary()
                startTable(metaData)
            }
            addedNewColumn -> startTable(metaData)
        }
    }

    private fun startTable(metaData: ITableMetaData) {
        currentTableName = metaData.tableName
        consumer.startTable(metaData)
    }

    private fun endTable() {
        consumer.endTable()
        currentTableName = null
    }

    private fun endTableIfNecessary() {
        if (hasCurrentTable()) {
            endTable()
        }
    }

    private fun hasCurrentTable() = currentTableName != null

    private fun isNewTable(tableName: String) =
        !hasCurrentTable() || !stringPolicy.areEqual(currentTableName!!, tableName)

    private fun metaDataBuilderFor(tableName: String): TableMetaDataBuilder = tableNameToMetaData.getOrPut(
        stringPolicy.toKey(tableName),
        { createNewTableMetaDataBuilder(tableName) }
    )

    private fun createNewTableMetaDataBuilder(tableName: String) = TableMetaDataBuilder(tableName, stringPolicy)
}

interface IDataSetManipulator {

    /**
     * Added a row so that a Dataset can be manipulated or created.
     * @param row
     */
    fun add(row: BasicDataRowBuilder): DataSetBuilder
}
