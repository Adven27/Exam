package com.adven.concordion.extensions.exam.core

import com.adven.concordion.extensions.exam.core.commands.*
import net.javacrumbs.jsonunit.core.Configuration
import org.xmlunit.diff.NodeMatcher
import java.util.*

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

        SetVarCommand("pre"),
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