package io.github.adven27.concordion.extensions.exam.db.commands

import io.github.adven27.concordion.extensions.exam.core.html.Html
import io.github.adven27.concordion.extensions.exam.core.html.html
import io.github.adven27.concordion.extensions.exam.db.DbPlugin
import io.github.adven27.concordion.extensions.exam.db.DbTester
import io.github.adven27.concordion.extensions.exam.db.builder.ExamDataSet
import io.github.adven27.concordion.extensions.exam.db.builder.SeedStrategy
import org.concordion.api.CommandCall
import org.concordion.api.Evaluator
import org.concordion.api.Fixture
import org.concordion.api.ResultRecorder

class DBSetCommand(
    name: String,
    tag: String,
    dbTester: DbTester,
    pattern: DbPlugin.ValuePrinter,
    private val allowedSeedStrategies: List<SeedStrategy>
) : DBCommand(name, tag, dbTester, pattern) {

    override fun setUp(cmd: CommandCall?, eval: Evaluator?, resultRecorder: ResultRecorder?, fixture: Fixture) {
        super.setUp(cmd, eval, resultRecorder, fixture)
        cmd.html().also { root ->
            Attrs.from(root, allowedSeedStrategies).also { attrs ->
                attrs.setAttrs.seedStrategy.operation
                    .execute(dbTester.connectionFor(ds), ExamDataSet(expectedTable, eval!!))
                root(renderTable(attrs.caption, expectedTable, remarks, valuePrinter))
            }
        }
    }

    data class Attrs(val setAttrs: SetAttrs, val caption: String?) {
        companion object {
            fun from(root: Html, allowedSeedStrategies: List<SeedStrategy>) = Attrs(
                SetAttrs.from(root, allowedSeedStrategies),
                root.takeAwayAttr("caption"),
            )
        }
    }
}
