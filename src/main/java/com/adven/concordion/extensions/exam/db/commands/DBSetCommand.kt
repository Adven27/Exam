package com.adven.concordion.extensions.exam.db.commands

import com.adven.concordion.extensions.exam.html.Html
import com.adven.concordion.extensions.exam.html.html
import com.github.database.rider.core.api.dataset.SeedStrategy
import com.github.database.rider.core.api.dataset.SeedStrategy.CLEAN_INSERT
import com.github.database.rider.core.api.dataset.SeedStrategy.INSERT
import com.github.database.rider.core.configuration.DataSetConfig
import com.github.database.rider.core.dataset.DataSetExecutorImpl
import org.concordion.api.CommandCall
import org.concordion.api.Evaluator
import org.concordion.api.ResultRecorder
import org.dbunit.dataset.ITable

class DBSetCommand(name: String, tag: String, dbTester: DataSetExecutorImpl) : DBCommand(name, tag, dbTester) {
    override fun setUp(cmd: CommandCall?, eval: Evaluator?, resultRecorder: ResultRecorder?) {
        super.setUp(cmd, eval, resultRecorder)
        val el = cmd.html()
        renderTable(el, expectedTable)
        setUpDB(expectedTable, parseInsertMode(el))
    }

    private fun setUpDB(table: ITable, insertMode: SeedStrategy) {
        dbTester.createDataSet(dataSetConfig(insertMode, table))
    }

    private fun dataSetConfig(insertMode: SeedStrategy, table: ITable): DataSetConfig {
        ExamDataSetProvider.table = table
        return DataSetConfig()
            .datasetProvider(ExamDataSetProvider::class.java)
            .strategy(insertMode)
            .executorId(ds)
            .useSequenceFiltering(false)
            .name(emptyArray())
    }

    private fun parseInsertMode(el: Html) = if (el.attr("mode") == "add") INSERT else CLEAN_INSERT
}