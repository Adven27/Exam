package com.adven.concordion.extensions.exam.core.commands

import com.adven.concordion.extensions.exam.core.ExamExtension
import nu.xom.Attribute
import org.concordion.api.*
import org.concordion.api.listener.ExecuteEvent
import org.concordion.api.listener.ExecuteListener
import org.concordion.internal.util.Announcer

open class ExamCommand(private val name: String, private val tag: String) : AbstractCommand() {
    private val listeners = Announcer.to(ExecuteListener::class.java)

    override fun execute(commandCall: CommandCall, evaluator: Evaluator, resultRecorder: ResultRecorder) {
        commandCall.children.processSequentially(evaluator, resultRecorder)
        announceExecuteCompleted(commandCall.element)
    }

    private fun announceExecuteCompleted(element: Element) =
        listeners.announce().executeCompleted(ExecuteEvent(element))

    fun tag(): String = tag

    fun name(): String = name

    open fun beforeParse(elem: nu.xom.Element) {
        val attr = Attribute(elem.localName, "")
        attr.setNamespace("e", ExamExtension.NS)
        elem.addAttribute(attr)

        elem.namespacePrefix = ""
        elem.namespaceURI = null
        elem.localName = tag
        Element(elem).appendNonBreakingSpaceIfBlank()
    }
}