package com.adven.concordion.extensions.exam.db.commands

import com.adven.concordion.extensions.exam.html.Html
import com.adven.concordion.extensions.exam.html.html
import org.concordion.api.CommandCall
import org.concordion.api.Evaluator
import org.concordion.api.ResultRecorder
import org.dbunit.IDatabaseTester
import org.dbunit.dataset.DefaultDataSet
import org.dbunit.dataset.ITable
import org.dbunit.operation.DatabaseOperation
import org.dbunit.operation.DatabaseOperation.CLEAN_INSERT
import org.dbunit.operation.DatabaseOperation.INSERT

class DBSetCommand(name: String, tag: String, dbTester: IDatabaseTester) : DBCommand(name, tag, dbTester) {
    override fun setUp(cmd: CommandCall?, eval: Evaluator?, resultRecorder: ResultRecorder?) {
        super.setUp(cmd, eval, resultRecorder)
        val el = cmd.html()
        renderTable(el, expectedTable)
        setUpDB(expectedTable, parseInsertMode(el))
    }

    private fun setUpDB(table: ITable, insertMode: DatabaseOperation) {
        dbTester.setSetUpOperation(insertMode)
        dbTester.dataSet = DefaultDataSet(table)
        dbTester.onSetup()
    }

    private fun parseInsertMode(el: Html): DatabaseOperation {
        return if (el.attr("mode") == "add") INSERT else CLEAN_INSERT
    }
}