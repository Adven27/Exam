package io.github.adven27.concordion.extensions.exam.db.commands

import io.github.adven27.concordion.extensions.exam.core.commands.SuitableCommandParser
import io.github.adven27.concordion.extensions.exam.core.html.DbRowParser
import io.github.adven27.concordion.extensions.exam.core.html.Html
import io.github.adven27.concordion.extensions.exam.core.html.html
import io.github.adven27.concordion.extensions.exam.db.MarkedHasNoDefaultValue
import io.github.adven27.concordion.extensions.exam.db.TableData
import io.github.adven27.concordion.extensions.exam.db.builder.DataSetBuilder
import io.github.adven27.concordion.extensions.exam.db.builder.ExamTable
import org.concordion.api.CommandCall
import org.concordion.api.Element
import org.concordion.api.Evaluator
import org.dbunit.dataset.DefaultTable
import org.dbunit.dataset.IDataSet
import org.dbunit.dataset.ITable

interface TableParser : SuitableCommandParser<ITable>

class MdTableParser : TableParser {
    override fun isSuitFor(element: Element): Boolean = element.localName != "div"

    override fun parse(command: CommandCall, evaluator: Evaluator): ITable {
        val builder = DataSetBuilder()
        val tableName = command.expression
        root(command).let { parseCols(it) to parseValues(it) }.let { (cols, rows) ->
            rows.forEach { row -> builder.newRowTo(tableName).withFields(cols.zip(row).toMap()).add() }
            return ExamTable(tableFrom(builder.build(), tableName), evaluator)
        }
    }

    private fun root(command: CommandCall) = command.element.parentElement.parentElement

    private fun tableFrom(dataSet: IDataSet, tableName: String) =
        if (dataSet.tableNames.isEmpty()) DefaultTable(tableName) else dataSet.getTable(tableName)

    private fun parseCols(it: Element) =
        it.getFirstChildElement("thead").getFirstChildElement("tr").childElements.map { it.text.trim() }

    private fun parseValues(it: Element) =
        it.getFirstChildElement("tbody").childElements.map { tr -> tr.childElements.map { it.text.trim() } }
}

class HtmlTableParser(
    private val remarks: MutableMap<String, Int> = mutableMapOf(),
    private val colParser: ColParser = ColParser()
) : TableParser {
    override fun isSuitFor(element: Element): Boolean = element.localName == "div"

    override fun parse(command: CommandCall, evaluator: Evaluator): ITable = command.html().let {
        TableData.filled(
            it.takeAwayAttr("table", evaluator)!!,
            DbRowParser(it, "row", null, null).parse(),
            parseCols(it),
            evaluator
        )
    }

    private fun parseCols(el: Html): Map<String, Any?> {
        val attr = el.takeAwayAttr("cols")
        return if (attr == null) emptyMap()
        else {
            val remarkAndVal = colParser.parse(attr)
            remarks += remarkAndVal.map { it.key to it.value.first }.filter { it.second > 0 }
            remarkAndVal.mapValues { if (it.value.second == null) MarkedHasNoDefaultValue() else it.value.second }
        }
    }
}

class ColParser {
    fun parse(attr: String): Map<String, Pair<Int, String?>> {
        return attr.split(",")
            .map {
                val (r, n, v) = ("""(\**)([^=]+)=?(.*)""".toRegex()).matchEntire(it.trim())!!.destructured
                mapOf(n to (r.length to (if (v.isBlank()) null else v)))
            }
            .reduce { acc, next -> acc + next }
    }
}
