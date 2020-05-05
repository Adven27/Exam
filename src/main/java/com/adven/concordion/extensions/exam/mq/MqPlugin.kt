package com.adven.concordion.extensions.exam.mq

import com.adven.concordion.extensions.exam.core.ExamPlugin
import com.adven.concordion.extensions.exam.core.commands.ExamCommand
import com.adven.concordion.extensions.exam.core.utils.DateFormatMatcher
import com.adven.concordion.extensions.exam.core.utils.DateWithin
import com.adven.concordion.extensions.exam.core.utils.XMLDateWithin
import net.javacrumbs.jsonunit.JsonAssert.`when`
import net.javacrumbs.jsonunit.core.Configuration
import net.javacrumbs.jsonunit.core.Option

class MqPlugin @JvmOverloads constructor(
    private val mqTesters: Map<String, MqTester>,
    private var jsonUnitCfg: Configuration = `when`(Option.IGNORING_ARRAY_ORDER)
        .withMatcher("formattedAs", DateFormatMatcher())
        .withMatcher("formattedAndWithin", DateWithin.param())
        .withMatcher("formattedAndWithinNow", DateWithin.now())
        .withMatcher("xmlDateWithinNow", XMLDateWithin())
) : ExamPlugin {

    override fun commands(): List<ExamCommand> = listOf(
        MqCheckCommand("mq-check", "div", jsonUnitCfg, mqTesters),
        MqSendCommand("mq-send", "div", mqTesters),
        MqPurgeCommand("mq-purge", "div", mqTesters)
    )
}