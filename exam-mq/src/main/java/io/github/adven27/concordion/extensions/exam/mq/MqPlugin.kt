package io.github.adven27.concordion.extensions.exam.mq

import io.github.adven27.concordion.extensions.exam.core.ExamPlugin
import io.github.adven27.concordion.extensions.exam.core.commands.ExamCommand

class MqPlugin constructor(private val testers: Map<String, MqTester>) : ExamPlugin {
    override fun commands(): List<ExamCommand> = listOf(
        MqCheckCommand("mq-check", "div", testers),
        MqSendCommand("mq-send", "div", testers),
        MqPurgeCommand("mq-purge", "div", testers)
    )

    override fun setUp() = testers.forEach { (_, t) -> t.start() }

    override fun tearDown() = testers.forEach { (_, t) -> t.stop() }
}
