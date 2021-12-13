package io.github.adven27.concordion.extensions.exam.db.commands.show

import io.github.adven27.concordion.extensions.exam.core.commands.BeforeParseExamCommand
import io.github.adven27.concordion.extensions.exam.core.commands.CommandParser
import io.github.adven27.concordion.extensions.exam.core.commands.NamedExamCommand
import io.github.adven27.concordion.extensions.exam.core.fileExt
import io.github.adven27.concordion.extensions.exam.core.html.code
import io.github.adven27.concordion.extensions.exam.core.html.html
import io.github.adven27.concordion.extensions.exam.core.html.pre
import io.github.adven27.concordion.extensions.exam.db.DbPlugin
import io.github.adven27.concordion.extensions.exam.db.DbTester
import io.github.adven27.concordion.extensions.exam.db.builder.JSONWriter
import io.github.adven27.concordion.extensions.exam.db.commands.renderTable
import org.concordion.api.AbstractCommand
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
import org.dbunit.util.QualifiedTableName
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.nio.file.Paths

class ShowCommand(
    override val name: String,
    val dbTester: DbTester,
    private val valuePrinter: DbPlugin.ValuePrinter,
    private val commandParser: CommandParser<Attrs> = ShowParser(),
) : AbstractCommand(), NamedExamCommand, BeforeParseExamCommand {
    override val tag = "div"

    data class Attrs(
        val ds: String,
        val table: String,
        val createDataSet: Boolean,
        val saveToResources: String?,
        val where: String?,
        val cols: Set<String>
    )

    override fun setUp(cmd: CommandCall, eval: Evaluator, resultRecorder: ResultRecorder?, fixture: Fixture) {
        with(commandParser.parse(cmd, eval)) {
            val conn = dbTester.connectionFor(ds)
            val el = cmd.html()
            el(
                renderTable(
                    includedColumnsTable(
                        if (where == null || where.isEmpty()) {
                            conn.createTable(table)
                        } else {
                            getFilteredTable(conn, table, where)
                        },
                        cols.toTypedArray()
                    ),
                    valuePrinter,
                    el.takeAwayAttr("caption"),
                )
            )
            if (createDataSet || !saveToResources.isNullOrEmpty()) {
                ByteArrayOutputStream().use {
                    save(
                        saveToResources,
                        conn.createDataSet(
                            getAllDependentTables(
                                conn,
                                QualifiedTableName(table, conn.schema).qualifiedName
                            )
                        ),
                        it
                    )
                    el(pre().attrs("class" to "doc-code language-xml")(code(it.toString("UTF-8"))))
                }
            }
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
