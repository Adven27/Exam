package com.adven.concordion.extensions.exam.core.commands

import com.adven.concordion.extensions.exam.core.html.Html
import com.google.common.base.Strings.isNullOrEmpty
import nu.xom.Attribute
import nu.xom.Element
import org.concordion.api.ImplementationStatus.*
import org.concordion.internal.ConcordionBuilder.NAMESPACE_CONCORDION_2007

class ExamExampleCommand(tag: String) : ExamCommand("example", tag) {

    override fun beforeParse(elem: Element) {
        transformToConcordionExample(elem)
        super.beforeParse(elem)
    }

    private fun transformToConcordionExample(elem: Element) {
        val name = elem.getAttributeValue("name")
        val exampleAttr = Attribute("example", name)
        exampleAttr.setNamespace("c", NAMESPACE_CONCORDION_2007)
        elem.addAttribute(exampleAttr)

        var status = UNIMPLEMENTED.tag
        if (elem.childElements.size() == 0) {
            elem.appendChild(status)
        } else {
            status = statusAttr(elem)
        }
        addStatus(elem, status)

        Html(org.concordion.api.Element(elem)).panel(name)
    }

    private fun statusAttr(elem: Element): String {
        var status = elem.getAttributeValue("status")
        if (isNullOrEmpty(status)) {
            return ""
        }
        when {
            "fail" == status.toLowerCase() -> status = EXPECTED_TO_FAIL.tag
            "unimpl" == status.toLowerCase() -> status = UNIMPLEMENTED.tag
            "pass" == status.toLowerCase() -> status = EXPECTED_TO_PASS.tag
        }
        return status
    }

    private fun addStatus(elem: Element, `val`: String) {
        if (!isNullOrEmpty(`val`)) {
            val statusAttr = Attribute("status", `val`)
            statusAttr.setNamespace("c", NAMESPACE_CONCORDION_2007)
            elem.addAttribute(statusAttr)
        }
    }
}