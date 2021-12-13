package io.github.adven27.concordion.extensions.exam.db.commands.set

import io.github.adven27.concordion.extensions.exam.core.commands.SuitableCommandParser
import io.github.adven27.concordion.extensions.exam.core.html.attr
import io.github.adven27.concordion.extensions.exam.core.html.takeAttr
import io.github.adven27.concordion.extensions.exam.db.DbTester.Companion.DEFAULT_DATASOURCE
import io.github.adven27.concordion.extensions.exam.db.builder.SeedStrategy
import io.github.adven27.concordion.extensions.exam.db.commands.SetAttrs
import io.github.adven27.concordion.extensions.exam.db.commands.TableParser
import io.github.adven27.concordion.extensions.exam.db.commands.set.SetCommand.Operation
import org.concordion.api.CommandCall
import org.concordion.api.Element
import org.concordion.api.Evaluator
import org.dbunit.dataset.ITable

class SetParser(
    private val parser: TableParser,
    private val allowedSeedStrategies: List<SeedStrategy>
) : SuitableCommandParser<Operation> {
    override fun isSuitFor(element: Element): Boolean = parser.isSuitFor(element)
    fun table(command: CommandCall, evaluator: Evaluator): ITable = parser.parse(command, evaluator)

    override fun parse(command: CommandCall, evaluator: Evaluator) = Operation(
        operation = SetAttrs.from(command, allowedSeedStrategies).seedStrategy.operation,
        ds = command.attr("ds", DEFAULT_DATASOURCE),
        table = table(command, evaluator),
        caption = command.takeAttr("caption") ?: command.element.text.ifBlank { null }
    )
}
