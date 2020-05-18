package com.adven.concordion.extensions.exam.db.builder

data class DataSetConfig(var datasets: String, var strategy: SeedStrategy = SeedStrategy.CLEAN_INSERT, var isUseSequenceFiltering: Boolean = true, var isFillIdentityColumns: Boolean = false, var tableOrdering: Array<String> = arrayOf()) {

    fun from(dataSet: DataSetImpl): DataSetConfig = DataSetConfig(
        dataSet.value(),
        dataSet.strategy(),
        dataSet.useSequenceFiltering(),
        dataSet.fillIdentityColumns(),
        dataSet.tableOrdering()
    )
}