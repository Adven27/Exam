package com.adven.concordion.extensions.exam.core

import com.adven.concordion.extensions.exam.core.commands.BeforeEachExampleCommand
import com.adven.concordion.extensions.exam.core.commands.ExamBeforeExampleCommand
import com.adven.concordion.extensions.exam.core.commands.ExamCommand
import com.adven.concordion.extensions.exam.core.commands.ExamExampleCommand
import com.adven.concordion.extensions.exam.core.commands.ExamplesSummaryCommand
import com.adven.concordion.extensions.exam.core.commands.GivenCommand
import com.adven.concordion.extensions.exam.core.commands.JsonCheckCommand
import com.adven.concordion.extensions.exam.core.commands.ScrollToTopCommand
import com.adven.concordion.extensions.exam.core.commands.SetVarCommand
import com.adven.concordion.extensions.exam.core.commands.ThenCommand
import com.adven.concordion.extensions.exam.core.commands.WaitCommand
import com.adven.concordion.extensions.exam.core.commands.WhenCommand
import com.adven.concordion.extensions.exam.core.commands.XmlCheckCommand
import net.javacrumbs.jsonunit.core.Configuration
import org.xmlunit.diff.NodeMatcher
import java.util.ArrayList

class CommandRegistry(jsonUnitCfg: Configuration, nodeMatcher: NodeMatcher) {
    private val commands = mutableListOf(
        GivenCommand("div"),
        WhenCommand("div"),
        ThenCommand("div"),

        ExamExampleCommand("div"),
        ExamBeforeExampleCommand("div"),
        BeforeEachExampleCommand("div"),
        ExamplesSummaryCommand("summary", "div"),
        ScrollToTopCommand("scrollToTop", "div"),

        SetVarCommand("span"),
        WaitCommand("span"),

        JsonCheckCommand("json-check", "div", jsonUnitCfg),
        XmlCheckCommand("xml-check", "div", jsonUnitCfg, nodeMatcher)
    )

    fun getBy(name: String): ExamCommand? = commands.firstOrNull { it.name() == name }

    fun register(cmds: List<ExamCommand>) {
        commands.addAll(cmds)
    }

    fun commands(): List<ExamCommand> = ArrayList(commands)
}
