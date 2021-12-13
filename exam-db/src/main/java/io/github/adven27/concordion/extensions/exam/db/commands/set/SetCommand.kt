package io.github.adven27.concordion.extensions.exam.db.commands.set

import io.github.adven27.concordion.extensions.exam.core.commands.BeforeParseExamCommand
import io.github.adven27.concordion.extensions.exam.core.commands.CommandParser
import io.github.adven27.concordion.extensions.exam.core.commands.ExamSetUpCommand
import io.github.adven27.concordion.extensions.exam.core.commands.FirstSuitableCommandParser
import io.github.adven27.concordion.extensions.exam.core.commands.FirstSuitableSetUpListener
import io.github.adven27.concordion.extensions.exam.core.commands.NamedExamCommand
import io.github.adven27.concordion.extensions.exam.core.commands.SetUpListener
import io.github.adven27.concordion.extensions.exam.db.DbPlugin
import io.github.adven27.concordion.extensions.exam.db.DbTester
import io.github.adven27.concordion.extensions.exam.db.builder.ExamDataSet
import io.github.adven27.concordion.extensions.exam.db.builder.SeedStrategy
import io.github.adven27.concordion.extensions.exam.db.commands.HtmlTableParser
import io.github.adven27.concordion.extensions.exam.db.commands.MdTableParser
import org.concordion.api.Evaluator
import org.dbunit.dataset.ITable
import org.dbunit.operation.DatabaseOperation

class SetCommand(
    override val name: String,
    val dbTester: DbTester,
    valuePrinter: DbPlugin.ValuePrinter,
    allowedSeedStrategies: List<SeedStrategy>,
    commandParser: CommandParser<Operation> = FirstSuitableCommandParser(
        SetParser(MdTableParser(), allowedSeedStrategies),
        SetParser(HtmlTableParser(), allowedSeedStrategies)
    ),
    listener: SetUpListener<Operation> = FirstSuitableSetUpListener(
        MdSetRenderer(valuePrinter),
        HtmlSetRenderer(valuePrinter)
    )
) : ExamSetUpCommand<SetCommand.Operation>(commandParser, listener), NamedExamCommand, BeforeParseExamCommand {
    override val tag = "div"

    override fun setUp(target: Operation, eval: Evaluator) {
        target.operation.execute(dbTester.connectionFor(target.ds), ExamDataSet(target.table, eval))
    }

    data class Operation(val operation: DatabaseOperation, val ds: String, val table: ITable, val caption: String?)
}
