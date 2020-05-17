package com.adven.concordion.extensions.exam.db.commands

import com.adven.concordion.extensions.exam.core.html.html
import com.adven.concordion.extensions.exam.core.html.pre
import com.adven.concordion.extensions.exam.core.html.table
import com.adven.concordion.extensions.exam.db.DbPlugin
import com.adven.concordion.extensions.exam.db.DbTester
import org.concordion.api.CommandCall
import org.concordion.api.Evaluator
import org.concordion.api.ResultRecorder
import org.dbunit.database.IDatabaseConnection
import org.dbunit.database.search.TablesDependencyHelper.getAllDependentTables
import org.dbunit.dataset.ITable
import org.dbunit.dataset.filter.DefaultColumnFilter.includedColumnsTable
import org.dbunit.dataset.xml.FlatXmlDataSet
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.nio.file.Paths


class DBShowCommand(name: String, tag: String, dbTester: DbTester, valuePrinter: DbPlugin.ValuePrinter) : DBCommand(name, tag, dbTester, valuePrinter) {

    override fun setUp(cmd: CommandCall?, eval: Evaluator?, resultRecorder: ResultRecorder?) {
        val el = table(cmd.html())
        val tableName = el.takeAwayAttr("table", eval)!!
        val where = el.takeAwayAttr("where", eval)
        val saveToResources = el.takeAwayAttr("saveToResources", eval)
        val ds = el.takeAwayAttr("ds", DbTester.DEFAULT_DATASOURCE)
        val conn = dbTester.executors[ds]!!.connection

        renderTable(
            el,
            includedColumnsTable(
                if (where == null || where.isEmpty())
                    conn.createTable(tableName)
                else
                    getFilteredTable(conn, tableName, where),
                parseCols(el, eval!!).keys.toTypedArray()
            ))

        val outputStream = ByteArrayOutputStream().apply {
            FlatXmlDataSet.write(conn.createDataSet(getAllDependentTables(conn, tableName)), this)
        }
        saveIfNeeded(saveToResources, outputStream)
        el.below(pre(outputStream.toString("UTF-8")))
    }

    private fun saveIfNeeded(saveToResources: String?, outputStream: ByteArrayOutputStream) {
        saveToResources?.apply {
            FileOutputStream(
                File(Paths.get("src", "test", "resources").toFile(), this).apply {
                    parentFile.mkdirs()
                    createNewFile()
                },
                false
            ).use { outputStream.writeTo(it) }
        }
    }

    private fun getFilteredTable(connection: IDatabaseConnection, tableName: String, rowFilter: String): ITable {
        return connection.createQueryTable(tableName, "SELECT * FROM $tableName WHERE $rowFilter")
    }
}