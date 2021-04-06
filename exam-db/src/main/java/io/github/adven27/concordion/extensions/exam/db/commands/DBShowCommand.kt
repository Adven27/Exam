package io.github.adven27.concordion.extensions.exam.db.commands

import io.github.adven27.concordion.extensions.exam.core.fileExt
import io.github.adven27.concordion.extensions.exam.core.html.html
import io.github.adven27.concordion.extensions.exam.core.html.pre
import io.github.adven27.concordion.extensions.exam.db.DbPlugin
import io.github.adven27.concordion.extensions.exam.db.DbTester
import io.github.adven27.concordion.extensions.exam.db.builder.JSONWriter
import org.concordion.api.CommandCall
import org.concordion.api.Evaluator
import org.concordion.api.Fixture
import org.concordion.api.ResultRecorder
import org.dbunit.database.IDatabaseConnection
import org.dbunit.database.search.TablesDependencyHelper.getAllDependentTables
import org.dbunit.dataset.IDataSet
import org.dbunit.dataset.ITable
import org.dbunit.dataset.csv.CsvDataSetWriter
import org.dbunit.dataset.excel.XlsDataSet
import org.dbunit.dataset.filter.DefaultColumnFilter.includedColumnsTable
import org.dbunit.dataset.xml.FlatXmlDataSet
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.nio.file.Paths

class DBShowCommand(name: String, tag: String, dbTester: DbTester, valuePrinter: DbPlugin.ValuePrinter) :
    DBCommand(name, tag, dbTester, valuePrinter) {

    override fun setUp(cmd: CommandCall?, eval: Evaluator?, resultRecorder: ResultRecorder?, fixture: Fixture) {
        val el = cmd.html()
        val tableName = el.takeAwayAttr("table", eval)!!
        val where = el.takeAwayAttr("where", eval)
        val saveToResources = el.takeAwayAttr("saveToResources", eval)
        val ds = el.takeAwayAttr("ds", DbTester.DEFAULT_DATASOURCE)
        val conn = dbTester.connectionFor(ds)

        el(
            renderTable(
                el.takeAwayAttr("caption"),
                includedColumnsTable(
                    if (where == null || where.isEmpty()) {
                        conn.createTable(tableName)
                    } else {
                        getFilteredTable(conn, tableName, where)
                    },
                    parseCols(el).keys.toTypedArray()
                ),
                remarks,
                valuePrinter
            )
        )
        val dataSet = conn.createDataSet(getAllDependentTables(conn, tableName))
        ByteArrayOutputStream().use {
            save(saveToResources, dataSet, it)
            el(pre(it.toString("UTF-8")))
        }
    }

    private fun save(saveToResources: String?, dataSet: IDataSet, it: ByteArrayOutputStream) {
        when (saveToResources?.fileExt() ?: "xml") {
            "json" -> JSONWriter(dataSet, it).write().run { saveIfNeeded(saveToResources, it) }
            "xls" -> XlsDataSet.write(dataSet, it).run { saveIfNeeded(saveToResources, it) }
            "csv" -> {
                saveToResources?.apply {
                    File(Paths.get("src", "test", "resources").toFile(), substringBeforeLast(".")).apply {
                        mkdirs()
                        CsvDataSetWriter.write(dataSet, this)
                    }
                }
            }
            else -> FlatXmlDataSet.write(dataSet, it).run { saveIfNeeded(saveToResources, it) }
        }
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

    private fun getFilteredTable(connection: IDatabaseConnection, tableName: String, rowFilter: String): ITable =
        connection.createQueryTable(tableName, "SELECT * FROM $tableName WHERE $rowFilter")
}
