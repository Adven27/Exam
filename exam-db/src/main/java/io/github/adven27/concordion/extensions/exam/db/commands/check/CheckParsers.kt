package io.github.adven27.concordion.extensions.exam.db.commands.check

import io.github.adven27.concordion.extensions.exam.core.commands.SuitableCommandParser
import io.github.adven27.concordion.extensions.exam.core.commands.awaitConfig
import io.github.adven27.concordion.extensions.exam.core.html.DbRowParser
import io.github.adven27.concordion.extensions.exam.core.html.Html
import io.github.adven27.concordion.extensions.exam.core.html.attr
import io.github.adven27.concordion.extensions.exam.core.html.html
import io.github.adven27.concordion.extensions.exam.db.DbTester.Companion.DEFAULT_DATASOURCE
import io.github.adven27.concordion.extensions.exam.db.MarkedHasNoDefaultValue
import io.github.adven27.concordion.extensions.exam.db.TableData
import io.github.adven27.concordion.extensions.exam.db.builder.DataSetBuilder
import io.github.adven27.concordion.extensions.exam.db.builder.ExamTable
import io.github.adven27.concordion.extensions.exam.db.commands.ColParser
import io.github.adven27.concordion.extensions.exam.db.commands.check.CheckCommand.Expected
import org.concordion.api.CommandCall
import org.concordion.api.Element
import org.concordion.api.Evaluator
import org.dbunit.dataset.Column
import org.dbunit.dataset.DefaultTable
import org.dbunit.dataset.ITable
import org.dbunit.dataset.datatype.DataType

abstract class CheckParser : SuitableCommandParser<Expected> {
    abstract fun table(command: CommandCall, evaluator: Evaluator): ITable
    abstract fun caption(command: CommandCall): String?

    override fun parse(command: CommandCall, evaluator: Evaluator) = Expected(
        ds = command.attr("ds", DEFAULT_DATASOURCE),
        caption = caption(command),
        table = table(command, evaluator),
        orderBy = command.html().takeAwayAttr("orderBy", evaluator)?.split(",")?.map { it.trim() } ?: listOf(),
        where = command.html().takeAwayAttr("where", evaluator) ?: "",
        await = command.awaitConfig()
    )
}

class MdCheckParser : CheckParser() {
    override fun isSuitFor(element: Element): Boolean = element.localName != "div"
    override fun caption(command: CommandCall) = command.element.text.ifBlank { null }
    private fun root(command: CommandCall) = command.element.parentElement.parentElement

    override fun table(command: CommandCall, evaluator: Evaluator): ITable {
        val builder = DataSetBuilder()
        val tableName = command.expression
        return root(command)
            .let { cols(it) to values(it) }
            .let { (cols, rows) ->
                rows.forEach { row -> builder.newRowTo(tableName).withFields(cols.zip(row).toMap()).add() }
                builder.build().let {
                    ExamTable(
                        if (it.tableNames.isEmpty()) DefaultTable(tableName, toColumns(cols))
                        else it.getTable(tableName),
                        evaluator
                    )
                }
            }
    }

    private fun toColumns(cols: List<String>) = cols.map { Column(it, DataType.UNKNOWN) }.toTypedArray()

    private fun cols(it: Element) =
        it.getFirstChildElement("thead").getFirstChildElement("tr").childElements.map { it.text.trim() }

    private fun values(it: Element) = it.getFirstChildElement("tbody").childElements.map { tr ->
        tr.childElements.map { it.text.trim() }
    }
}

class HtmlCheckParser : CheckParser() {
    private val remarks = HashMap<String, Int>()
    private val colParser = ColParser()

    override fun isSuitFor(element: Element): Boolean = element.localName == "div"
    override fun caption(command: CommandCall) = command.html().attr("caption")
    override fun table(command: CommandCall, evaluator: Evaluator): ITable = command.html().let {
        TableData.filled(
            it.takeAwayAttr("table", evaluator)!!,
            DbRowParser(it, "row", null, null).parse(),
            parseCols(it),
            evaluator
        )
    }

    protected fun parseCols(el: Html): Map<String, Any?> {
        val attr = el.takeAwayAttr("cols")
        return if (attr == null) emptyMap()
        else {
            val remarkAndVal = colParser.parse(attr)
            remarks += remarkAndVal.map { it.key to it.value.first }.filter { it.second > 0 }
            remarkAndVal.mapValues { if (it.value.second == null) MarkedHasNoDefaultValue() else it.value.second }
        }
    }
}
