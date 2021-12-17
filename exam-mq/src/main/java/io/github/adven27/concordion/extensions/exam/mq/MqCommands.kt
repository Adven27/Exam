package io.github.adven27.concordion.extensions.exam.mq

import io.github.adven27.concordion.extensions.exam.core.commands.ExamCommand
import io.github.adven27.concordion.extensions.exam.core.commands.FromAttrs
import io.github.adven27.concordion.extensions.exam.core.commands.VarsAttrs
import io.github.adven27.concordion.extensions.exam.core.headers
import io.github.adven27.concordion.extensions.exam.core.html.CLASS
import io.github.adven27.concordion.extensions.exam.core.html.Html
import io.github.adven27.concordion.extensions.exam.core.html.caption
import io.github.adven27.concordion.extensions.exam.core.html.div
import io.github.adven27.concordion.extensions.exam.core.html.divCollapse
import io.github.adven27.concordion.extensions.exam.core.html.generateId
import io.github.adven27.concordion.extensions.exam.core.html.html
import io.github.adven27.concordion.extensions.exam.core.html.italic
import io.github.adven27.concordion.extensions.exam.core.html.span
import io.github.adven27.concordion.extensions.exam.core.html.table
import io.github.adven27.concordion.extensions.exam.core.html.td
import org.concordion.api.CommandCall
import org.concordion.api.Evaluator
import org.concordion.api.Fixture
import org.concordion.api.ResultRecorder

open class TypedMessage(val type: String, body: String = "", headers: Map<String, String> = emptyMap()) :
    MqTester.Message(body, headers)

open class ParametrizedTypedMessage(
    type: String,
    body: String = "",
    headers: Map<String, String> = emptyMap(),
    val params: Map<String, String> = emptyMap()
) : TypedMessage(type, body, headers)

data class VerifyPair(val actual: MqTester.Message, val expected: TypedMessage) {
    override fun toString() = "actual={$actual}, expected={$expected}"
}

class MessageAttrs(root: Html, evaluator: Evaluator) {
    private val verifyAs: String? = root.takeAwayAttr(VERIFY_AS)
    private val formatAs: String? = root.takeAwayAttr(FORMAT_AS)
    val from: FromAttrs = FromAttrs(root, evaluator, verifyAs ?: formatAs)
    val vars: VarsAttrs = VarsAttrs(root, evaluator)
    val headers: Map<String, String> = root.takeAwayAttr(HEADERS).attrToMap()

    companion object {
        private const val VERIFY_AS = "verifyAs"
        private const val FORMAT_AS = "formatAs"
        private const val HEADERS = "headers"
        private const val PARAMS = "params"
    }
}

class MqPurgeCommand(name: String, tag: String, private val mqTesters: Map<String, MqTester>) : ExamCommand(name, tag) {
    override fun execute(
        commandCall: CommandCall,
        evaluator: Evaluator,
        resultRecorder: ResultRecorder,
        fixture: Fixture
    ) {
        super.execute(commandCall, evaluator, resultRecorder, fixture)
        commandCall.html().also { root ->
            Attrs.from(root).also {
                mqTesters.getOrFail(it.mqName).purge()
                renderCommand(root, it.mqName)
            }
        }
    }

    private fun renderCommand(root: Html, mqName: String) {
        root.removeChildren()(
            table()(
                caption()(
                    italic(" ", CLASS to "far fa-envelope-open me-1"),
                    span("$mqName purged")
                )
            )
        )
    }

    data class Attrs(val mqName: String) {
        companion object {
            private const val NAME = "name"
            fun from(root: Html) = Attrs(root.attrOrFail(NAME))
        }
    }
}

private fun String?.attrToMap(): Map<String, String> = this?.headers()?.mapValues { it.value } ?: emptyMap()

private fun Map<String, MqTester>.getOrFail(mqName: String?): MqTester = this[mqName]
    ?: throw IllegalArgumentException("MQ with name $mqName not registered in MqPlugin")

private fun container(text: String, type: String, collapsable: Boolean) =
    if (collapsable) collapsableContainer(text, type) else fixedContainer(text, type)

private fun collapsed(container: Html) = td("class" to "exp-body")(
    div().style("position: relative")(
        divCollapse("", container.attr("id").toString()).css("default-collapsed"),
        container
    )
)

private fun fixedContainer(text: String, type: String) = td(text).css("$type exp-body")

private fun collapsableContainer(text: String, type: String) =
    div(text, "id" to generateId()).css("$type file collapse show")
