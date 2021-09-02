package io.github.adven27.concordion.extensions.exam.core

import io.github.adven27.concordion.extensions.exam.core.commands.BeforeEachExampleCommand
import io.github.adven27.concordion.extensions.exam.core.commands.ExamExampleCommand
import io.github.adven27.concordion.extensions.exam.core.commands.GivenCommand
import io.github.adven27.concordion.extensions.exam.core.commands.JsonCheckCommand
import io.github.adven27.concordion.extensions.exam.core.commands.JsonEqualsCommand
import io.github.adven27.concordion.extensions.exam.core.commands.JsonEqualsFileCommand
import io.github.adven27.concordion.extensions.exam.core.commands.NamedExamCommand
import io.github.adven27.concordion.extensions.exam.core.commands.SetVarCommand
import io.github.adven27.concordion.extensions.exam.core.commands.ThenCommand
import io.github.adven27.concordion.extensions.exam.core.commands.WaitCommand
import io.github.adven27.concordion.extensions.exam.core.commands.WhenCommand
import io.github.adven27.concordion.extensions.exam.core.commands.XmlCheckCommand
import io.github.adven27.concordion.extensions.exam.core.commands.XmlEqualsCommand
import io.github.adven27.concordion.extensions.exam.core.commands.XmlEqualsFileCommand

class CommandRegistry(jsonVerifier: ContentVerifier, xmlVerifier: ContentVerifier) {
    private val commands = mutableListOf<NamedExamCommand>(
        GivenCommand(),
        WhenCommand(),
        ThenCommand(),

        ExamExampleCommand("div"),
        BeforeEachExampleCommand("div"),

        SetVarCommand(),
        WaitCommand("span"),

        JsonCheckCommand("div", jsonVerifier),
        XmlCheckCommand("div", xmlVerifier),
        XmlEqualsCommand(),
        XmlEqualsFileCommand(),
        JsonEqualsCommand(),
        JsonEqualsFileCommand(),
    )

    fun getBy(name: String): NamedExamCommand? = commands.firstOrNull { it.name == name }

    fun register(cmds: List<NamedExamCommand>) {
        commands.addAll(cmds)
    }

    fun commands(): List<NamedExamCommand> = ArrayList(commands)
}
