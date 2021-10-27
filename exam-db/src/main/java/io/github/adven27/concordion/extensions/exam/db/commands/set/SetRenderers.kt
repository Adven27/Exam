package io.github.adven27.concordion.extensions.exam.db.commands.set

import io.github.adven27.concordion.extensions.exam.core.commands.SetUpEvent
import io.github.adven27.concordion.extensions.exam.core.commands.SuitableSetUpListener
import io.github.adven27.concordion.extensions.exam.db.DbPlugin.ValuePrinter
import io.github.adven27.concordion.extensions.exam.db.commands.renderTable
import org.concordion.api.Element
import org.concordion.api.listener.AbstractElementEvent

class MdSetRenderer(printer: ValuePrinter) : BaseSetRenderer(printer) {
    override fun root(event: AbstractElementEvent): Element = event.element.parentElement.parentElement
    override fun isSuitFor(element: Element) = element.localName != "div"
}

class HtmlSetRenderer(printer: ValuePrinter) : BaseSetRenderer(printer) {
    override fun root(event: AbstractElementEvent): Element = event.element
    override fun isSuitFor(element: Element) = element.localName == "div"
}

abstract class BaseSetRenderer(private val printer: ValuePrinter) : SuitableSetUpListener<SetCommand.Operation>() {
    abstract fun root(event: AbstractElementEvent): Element

    override fun setUpCompleted(event: SetUpEvent<SetCommand.Operation>) = with(root(event)) {
        appendSister(renderTable(event.target.table, printer, event.target.caption).el)
        parentElement.removeChild(this)
    }
}
