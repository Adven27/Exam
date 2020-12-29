package com.adven.concordion.extensions.exam.db.commands

import com.adven.concordion.extensions.exam.core.html.html
import com.adven.concordion.extensions.exam.db.DbPlugin
import com.adven.concordion.extensions.exam.db.DbTester
import com.adven.concordion.extensions.exam.db.builder.ExamDataSet
import org.concordion.api.CommandCall
import org.concordion.api.Evaluator
import org.concordion.api.Fixture
import org.concordion.api.ResultRecorder
import org.dbunit.operation.DatabaseOperation
import org.dbunit.operation.DatabaseOperation.CLEAN_INSERT
import org.dbunit.operation.DatabaseOperation.DELETE
import org.dbunit.operation.DatabaseOperation.DELETE_ALL
import org.dbunit.operation.DatabaseOperation.INSERT
import org.dbunit.operation.DatabaseOperation.REFRESH
import org.dbunit.operation.DatabaseOperation.TRUNCATE_TABLE
import org.dbunit.operation.DatabaseOperation.UPDATE

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

    override fun setUp(cmd: CommandCall?, eval: Evaluator?, resultRecorder: ResultRecorder?, fixture: Fixture) {
        super.setUp(cmd, eval, resultRecorder, fixture)
        val el = cmd.html()
        el(renderTable(el.takeAwayAttr("caption"), expectedTable, remarks, valuePrinter))
        operations.getOrElse(
            el.takeAwayAttr("operation", "clean_insert").toLowerCase(),
            { throw IllegalArgumentException("Unsupported dbunit operation. Supported: ${operations.keys}") }
        ).execute(dbTester.connectionFor(ds), ExamDataSet(expectedTable, eval!!))
    }
}
