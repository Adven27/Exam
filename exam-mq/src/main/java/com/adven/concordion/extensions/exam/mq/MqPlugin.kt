package com.adven.concordion.extensions.exam.mq

import com.adven.concordion.extensions.exam.core.ContentVerifier
import com.adven.concordion.extensions.exam.core.ExamExtension
import com.adven.concordion.extensions.exam.core.ExamPlugin
import com.adven.concordion.extensions.exam.core.commands.ExamCommand
import net.javacrumbs.jsonunit.core.Configuration
import org.xmlunit.diff.NodeMatcher

class MqPlugin @JvmOverloads constructor(
    private val testers: Map<String, MqTester>,
    private var jsonUnitCfg: Configuration = ExamExtension.DEFAULT_JSON_UNIT_CFG,
    private val nodeMatcher: NodeMatcher = ExamExtension.DEFAULT_NODE_MATCHER,
    private val addContentVerifiers: Map<String, ContentVerifier> = mapOf()
) : ExamPlugin {
    override fun commands(): List<ExamCommand> = listOf(
        MqCheckCommand(
            "mq-check",
            "div",
            jsonUnitCfg,
            nodeMatcher,
            testers,
            mapOf("json" to ContentVerifier.Json(), "xml" to ContentVerifier.Xml()) + addContentVerifiers
        ),
        MqSendCommand("mq-send", "div", testers),
        MqPurgeCommand("mq-purge", "div", testers)
    )

    override fun setUp() {
        testers.forEach { (_, t) -> t.start() }
    }

    override fun tearDown() {
        testers.forEach { (_, t) -> t.stop() }
    }
}
