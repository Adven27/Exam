package io.github.adven27.concordion.extensions.exam.db.commands

import io.github.adven27.concordion.extensions.exam.core.commands.ExamCommand
import io.github.adven27.concordion.extensions.exam.core.commands.swapText
import io.github.adven27.concordion.extensions.exam.core.html.html
import io.github.adven27.concordion.extensions.exam.db.DbTester
import io.github.adven27.concordion.extensions.exam.db.builder.DataSetBuilder
import org.concordion.api.CommandCall
import org.concordion.api.Evaluator
import org.concordion.api.Fixture
import org.concordion.api.ResultRecorder
import org.dbunit.operation.DatabaseOperation

class DBCleanCommand(name: String, tag: String, private val dbTester: DbTester) : ExamCommand(name, tag) {

    override fun setUp(cmd: CommandCall, eval: Evaluator, resultRecorder: ResultRecorder, fixture: Fixture) {
        val el = cmd.html()
        val ds = el.takeAwayAttr("ds", DbTester.DEFAULT_DATASOURCE)
        val builder = DataSetBuilder()
        val tables = el.takeAwayAttr("tables", eval)?.also { cmd.swapText(it) } ?: eval.evaluate(cmd.expression).toString()
        tables.split(",").map { builder.newRowTo(it.trim()).add() }
        dbTester.executors[ds]!!.apply {
            setUpOperation = DatabaseOperation.DELETE_ALL
            dataSet = builder.build()
            onSetup()
        }
    }
}
