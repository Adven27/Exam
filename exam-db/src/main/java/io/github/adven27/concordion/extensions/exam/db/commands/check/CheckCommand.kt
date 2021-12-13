package io.github.adven27.concordion.extensions.exam.db.commands.check

import io.github.adven27.concordion.extensions.exam.core.commands.ActualProvider
import io.github.adven27.concordion.extensions.exam.core.commands.AwaitConfig
import io.github.adven27.concordion.extensions.exam.core.commands.BeforeParseExamCommand
import io.github.adven27.concordion.extensions.exam.core.commands.CommandParser
import io.github.adven27.concordion.extensions.exam.core.commands.ExamAssertCommand
import io.github.adven27.concordion.extensions.exam.core.commands.FirstSuitableCommandParser
import io.github.adven27.concordion.extensions.exam.core.commands.FirstSuitableResultRenderer
import io.github.adven27.concordion.extensions.exam.core.commands.NamedExamCommand
import io.github.adven27.concordion.extensions.exam.core.commands.VerifyListener
import io.github.adven27.concordion.extensions.exam.db.DbPlugin
import io.github.adven27.concordion.extensions.exam.db.DbTester
import io.github.adven27.concordion.extensions.exam.db.commands.check.CheckCommand.Expected
import io.github.adven27.concordion.extensions.exam.db.commands.tableName
import org.concordion.api.CommandCall
import org.concordion.api.Evaluator
import org.concordion.api.Fixture
import org.concordion.api.ResultRecorder
import org.dbunit.dataset.ITable
import org.dbunit.util.QualifiedTableName

@Suppress("LongParameterList")
class CheckCommand(
    override val name: String,
    val dbTester: DbTester,
    valuePrinter: DbPlugin.ValuePrinter,
    verifier: DbVerifier = DbVerifier(dbTester),
    actualProvider: DbActualProvider = DbActualProvider(dbTester),
    commandParser: CommandParser<Expected> = FirstSuitableCommandParser(MdCheckParser(), HtmlCheckParser()),
    resultRenderer: VerifyListener<Expected, ITable> = FirstSuitableResultRenderer(
        MdResultRenderer(valuePrinter),
        HtmlResultRenderer(valuePrinter)
    )
) : ExamAssertCommand<Expected, ITable>(commandParser, verifier, actualProvider, resultRenderer),
    NamedExamCommand,
    BeforeParseExamCommand {
    override val tag = "div"

    override fun verify(cmd: CommandCall, eval: Evaluator, resultRecorder: ResultRecorder, fixture: Fixture) {
        dbTester.dbUnitConfig.valueComparer.setEvaluator(eval)
        dbTester.dbUnitConfig.columnValueComparers.forEach { it.value.setEvaluator(eval) }
        super.verify(cmd, eval, resultRecorder, fixture)
    }

    data class Expected(
        val ds: String,
        val caption: String?,
        val table: ITable,
        val orderBy: List<String>,
        val where: String,
        val await: AwaitConfig?
    )
}

class DbActualProvider(val dbTester: DbTester) : ActualProvider<Expected, Pair<Boolean, ITable>> {
    override fun provide(source: Expected): Pair<Boolean, ITable> = source.let {
        with(dbTester.connectionFor(it.ds)) {
            val qualifiedName = QualifiedTableName(it.table.tableName(), schema).qualifiedName
            false to createQueryTable(
                qualifiedName,
                "SELECT * FROM $qualifiedName ${if (it.where.isEmpty()) "" else "WHERE ${it.where}"}"
            )
        }
    }
}
