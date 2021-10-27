package io.github.adven27.concordion.extensions.exam.db.commands.check

import io.github.adven27.concordion.extensions.exam.core.commands.ActualProvider
import io.github.adven27.concordion.extensions.exam.core.commands.AwaitConfig
import io.github.adven27.concordion.extensions.exam.core.commands.BeforeParseExamCommand
import io.github.adven27.concordion.extensions.exam.core.commands.CommandParser
import io.github.adven27.concordion.extensions.exam.core.commands.ExamAssertCommand
import io.github.adven27.concordion.extensions.exam.core.commands.FirsSuitableCommandParser
import io.github.adven27.concordion.extensions.exam.core.commands.FirsSuitableResultRenderer
import io.github.adven27.concordion.extensions.exam.core.commands.NamedExamCommand
import io.github.adven27.concordion.extensions.exam.core.commands.VerifyListener
import io.github.adven27.concordion.extensions.exam.db.DbPlugin
import io.github.adven27.concordion.extensions.exam.db.DbTester
import io.github.adven27.concordion.extensions.exam.db.commands.check.CheckCommand.Expected
import io.github.adven27.concordion.extensions.exam.db.commands.check.DbActualProvider.Select
import io.github.adven27.concordion.extensions.exam.db.commands.tableName
import org.concordion.api.CommandCall
import org.concordion.api.Evaluator
import org.concordion.api.Fixture
import org.concordion.api.ResultRecorder
import org.dbunit.dataset.ITable
import org.dbunit.util.QualifiedTableName

class CheckCommand(
    override val name: String,
    val dbTester: DbTester,
    valuePrinter: DbPlugin.ValuePrinter,
    private val verifier: DbVerifier = DbVerifier(dbTester),
    private val actualProvider: DbActualProvider = DbActualProvider(dbTester),
    private val commandParser: CommandParser<Expected> = FirsSuitableCommandParser(MdCheckParser(), HtmlCheckParser()),
    resultRenderer: VerifyListener<Expected, ITable> = FirsSuitableResultRenderer(
        MdResultRenderer(valuePrinter),
        HtmlResultRenderer(valuePrinter)
    )
) : ExamAssertCommand<Expected, ITable>(resultRenderer), NamedExamCommand, BeforeParseExamCommand {
    override val tag = "div"

    override fun verify(cmd: CommandCall, eval: Evaluator, resultRecorder: ResultRecorder, fixture: Fixture) {
        dbTester.dbUnitConfig.valueComparer.setEvaluator(eval)
        dbTester.dbUnitConfig.columnValueComparers.forEach { it.value.setEvaluator(eval) }

        commandParser.parse(cmd, eval).apply {
            verifier.verify(this) { actualProvider.provide(Select(ds, table.tableName(), where)) }
                .onSuccess { success(resultRecorder, cmd.element, it.actual, it.expected) }
                .onFailure { failure(resultRecorder, cmd.element, this, it) }
        }
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

class DbActualProvider(val dbTester: DbTester) : ActualProvider<Select, Pair<Boolean, ITable>> {

    override fun provide(source: Select): Pair<Boolean, ITable> = source.let { (ds, table, where) ->
        with(dbTester.connectionFor(ds)) {
            val qualifiedName = QualifiedTableName(table, schema).qualifiedName
            false to createQueryTable(
                qualifiedName,
                "select * from $qualifiedName ${if (where.isEmpty()) "" else "WHERE $where"}"
            )
        }
    }

    data class Select(val ds: String, val table: String, val where: String)
}