package com.adven.concordion.extensions.exam.core.commands

import com.adven.concordion.extensions.exam.core.ExamExtension
import com.adven.concordion.extensions.exam.core.html.html
import com.adven.concordion.extensions.exam.core.resolveToObj
import com.adven.concordion.extensions.exam.core.resolveXml
import nu.xom.Attribute
import nu.xom.Element
import nu.xom.XPathContext
import org.concordion.api.CommandCall
import org.concordion.api.Evaluator
import org.concordion.api.ResultRecorder
import org.concordion.internal.ConcordionBuilder

class SetVarCommand(tag: String) : ExamCommand("set", tag) {
    override fun setUp(cmd: CommandCall, eval: Evaluator, resultRecorder: ResultRecorder) {
        val el = cmd.html()
        var valueAttr = el.attr("value")
        val value = if (valueAttr == null) {
            val body = el.text()
            val silent = el.attr("silent")
            if (silent != null && silent == "true") {
                el.removeAllChild()
            }
            eval.resolveXml(body)
        } else {
            eval.resolveToObj(valueAttr)
        }
        eval.setVariable("#${el.attr("var")!!}", value)
    }
}

class InlineBeforeExampleCommand(tag: String) : ExamCommand("inline", tag) {
    override fun beforeParse(elem: Element) {
        super.beforeParse(elem)
        val ns = XPathContext("e", ExamExtension.NS)
        val examples = elem.document.rootElement.getFirstChildElement("body").query(".//e:example", ns)
        elem.detach()
        for (i in 0 until examples.size()) {
            val example = examples.get(i) as Element
            if (example.childElements.size() > 0) {
                example.insertChild(elem.copy(), 0)
            }
        }
    }
}

class ExamBeforeExampleCommand(tag: String) : ExamCommand("before", tag) {
    override fun beforeParse(elem: Element) {
        transformToConcordionExample(elem)
        super.beforeParse(elem)
    }

    private fun transformToConcordionExample(elem: Element) {
        val attr = Attribute("example", "before")
        attr.setNamespace("c", ConcordionBuilder.NAMESPACE_CONCORDION_2007)
        elem.addAttribute(attr)
    }
}