package com.adven.concordion.extensions.exam.db.commands

import com.adven.concordion.extensions.exam.core.commands.ExamCommand
import com.adven.concordion.extensions.exam.core.html.html
import com.adven.concordion.extensions.exam.db.DbPlugin
import com.adven.concordion.extensions.exam.db.DbTester
import com.adven.concordion.extensions.exam.db.builder.DataSetConfig
import com.adven.concordion.extensions.exam.db.builder.DataSetExecutor
import org.concordion.api.CommandCall
import org.concordion.api.Evaluator
import org.concordion.api.ResultRecorder

class DataSetExecuteCommand(name: String, tag: String, val dbTester: DbTester, var valuePrinter: DbPlugin.ValuePrinter) : ExamCommand(name, tag) {
    override fun setUp(commandCall: CommandCall, evaluator: Evaluator, resultRecorder: ResultRecorder) {
        DataSetExecutor(dbTester).insertDataSet(DataSetConfig(commandCall.html().takeAwayAttr("datasets", "")), evaluator)
    }
}

class DataSetVerifyCommand(name: String, tag: String, val dbTester: DbTester, var valuePrinter: DbPlugin.ValuePrinter) : ExamCommand(name, tag) {
    override fun setUp(commandCall: CommandCall, evaluator: Evaluator, resultRecorder: ResultRecorder) {

    }

    override fun verify(commandCall: CommandCall, evaluator: Evaluator, resultRecorder: ResultRecorder) {
        DataSetExecutor(dbTester).compareCurrentDataSetWith(DataSetConfig(commandCall.html().takeAwayAttr("datasets", "")), evaluator)
    }
}