package io.github.adven27.concordion.extensions.exam.mq.commands.check

import io.github.adven27.concordion.extensions.exam.core.commands.ActualProvider
import io.github.adven27.concordion.extensions.exam.mq.MqTester

class MqActualProvider(private val mqTesters: Map<String, MqTester>) :
    ActualProvider<String, Pair<Boolean, CheckCommand.Actual>> {
    override fun provide(source: String) =
        mqTesters.getOrFail(source).let { it.accumulateOnRetries() to CheckCommand.Actual(it.receive()) }

    private fun Map<String, MqTester>.getOrFail(mqName: String?): MqTester = this[mqName]
        ?: throw IllegalArgumentException("MQ with name $mqName not registered in MqPlugin")
}
