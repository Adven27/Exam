package io.github.adven27.concordion.extensions.exam.db.commands.show

import io.github.adven27.concordion.extensions.exam.core.commands.SuitableCommandParser
import io.github.adven27.concordion.extensions.exam.core.html.Html
import io.github.adven27.concordion.extensions.exam.core.html.attr
import io.github.adven27.concordion.extensions.exam.core.html.html
import io.github.adven27.concordion.extensions.exam.db.DbTester.Companion.DEFAULT_DATASOURCE
import io.github.adven27.concordion.extensions.exam.db.MarkedHasNoDefaultValue
import io.github.adven27.concordion.extensions.exam.db.commands.ColParser
import org.concordion.api.CommandCall
import org.concordion.api.Element
import org.concordion.api.Evaluator

open class ShowParser(private val colParser: ColParser = ColParser()) : SuitableCommandParser<ShowCommand.Attrs> {
    override fun isSuitFor(element: Element): Boolean = true

    override fun parse(command: CommandCall, evaluator: Evaluator) = ShowCommand.Attrs(
        ds = command.attr("ds", DEFAULT_DATASOURCE),
        table = command.expression.ifBlank { null } ?: command.html().takeAwayAttr("table", evaluator)!!,
        where = command.html().takeAwayAttr("where", evaluator),
        createDataSet = command.html().takeAwayAttr("createDataSet", "false").toBoolean(),
        saveToResources = command.html().takeAwayAttr("saveToResources", evaluator),
        cols = parseCols(command.html()).keys
    )

    protected fun parseCols(el: Html): Map<String, Any?> {
        val attr = el.takeAwayAttr("cols")
        return if (attr == null) emptyMap()
        else {
            val remarkAndVal = colParser.parse(attr)
            remarkAndVal.mapValues { if (it.value.second == null) MarkedHasNoDefaultValue() else it.value.second }
        }
    }
}
