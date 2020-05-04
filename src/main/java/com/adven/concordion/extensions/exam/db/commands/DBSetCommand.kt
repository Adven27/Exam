package com.adven.concordion.extensions.exam.db.commands

import com.adven.concordion.extensions.exam.core.html.html
import com.adven.concordion.extensions.exam.db.DbPlugin
import com.adven.concordion.extensions.exam.db.DbTester
import org.concordion.api.CommandCall
import org.concordion.api.Evaluator
import org.concordion.api.ResultRecorder
import org.dbunit.dataset.DefaultDataSet
import org.dbunit.dataset.ITable
import org.dbunit.operation.DatabaseOperation
import org.dbunit.operation.DatabaseOperation.*
import java.lang.IllegalArgumentException

class DBSetCommand(name: String, tag: String, dbTester: DbTester, pattern: DbPlugin.ValuePrinter) : DBCommand(name, tag, dbTester, pattern) {
    private val operations: Map<String, DatabaseOperation> = mapOf(
            "clean_insert" to CLEAN_INSERT,
            "insert" to INSERT,
            "update" to UPDATE,
            "refresh" to REFRESH,
            "delete" to DELETE,
            "delete_all" to DELETE_ALL,
            "truncate_table" to TRUNCATE_TABLE
    )

    override fun setUp(cmd: CommandCall?, eval: Evaluator?, resultRecorder: ResultRecorder?) {
        super.setUp(cmd, eval, resultRecorder)
        val el = cmd.html()
        renderTable(el, expectedTable)
        setUpDB(
            expectedTable,
            operations.getOrElse(
                el.takeAwayAttr("operation", "clean_insert").toLowerCase(),
                { throw IllegalArgumentException("Unsupported dbunit operation. Supported: ${operations.keys}") }
            )
        )
    }

    private fun setUpDB(table: ITable, insertMode: DatabaseOperation) {
        dbTester.executors[ds]!!.apply {
            setUpOperation = insertMode
            dataSet = DefaultDataSet(table)
            onSetup()
        }
    }
}