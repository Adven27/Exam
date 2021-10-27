package io.github.adven27.concordion.extensions.exam.db.commands.set

import io.github.adven27.concordion.extensions.exam.core.commands.BeforeParseExamCommand
import io.github.adven27.concordion.extensions.exam.core.commands.CommandParser
import io.github.adven27.concordion.extensions.exam.core.commands.ExamSetUpCommand
import io.github.adven27.concordion.extensions.exam.core.commands.FirsSuitableCommandParser
import io.github.adven27.concordion.extensions.exam.core.commands.FirsSuitableSetUpListener
import io.github.adven27.concordion.extensions.exam.core.commands.NamedExamCommand
import io.github.adven27.concordion.extensions.exam.db.DbPlugin
import io.github.adven27.concordion.extensions.exam.db.DbTester
import io.github.adven27.concordion.extensions.exam.db.builder.ExamDataSet
import io.github.adven27.concordion.extensions.exam.db.builder.SeedStrategy
import io.github.adven27.concordion.extensions.exam.db.commands.HtmlTableParser
import io.github.adven27.concordion.extensions.exam.db.commands.MdTableParser
import org.concordion.api.CommandCall
import org.concordion.api.Evaluator
import org.concordion.api.Fixture
import org.concordion.api.ResultRecorder
import org.dbunit.dataset.ITable
import org.dbunit.operation.DatabaseOperation

class SetCommand(
    override val name: String,
    val dbTester: DbTester,
    valuePrinter: DbPlugin.ValuePrinter,
    allowedSeedStrategies: List<SeedStrategy>,
    private val commandParser: CommandParser<Operation> = FirsSuitableCommandParser(
        SetParser(MdTableParser(), allowedSeedStrategies),
        SetParser(HtmlTableParser(), allowedSeedStrategies)
    ),
) : ExamSetUpCommand<SetCommand.Operation>(
    FirsSuitableSetUpListener(MdSetRenderer(valuePrinter), HtmlSetRenderer(valuePrinter))
), NamedExamCommand, BeforeParseExamCommand {
    override val tag = "div"

    override fun setUp(cmd: CommandCall, eval: Evaluator, resultRecorder: ResultRecorder, fixture: Fixture) =
        commandParser.parse(cmd, eval).let {
            it.operation.execute(dbTester.connectionFor(it.ds), ExamDataSet(it.table, eval))
            setUpCompleted(cmd.element, it)
        }

    data class Operation(val operation: DatabaseOperation, val ds: String, val table: ITable, val caption: String?)
}
