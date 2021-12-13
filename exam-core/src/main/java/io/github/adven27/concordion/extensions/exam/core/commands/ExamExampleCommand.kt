package io.github.adven27.concordion.extensions.exam.core.commands

import nu.xom.Attribute
import nu.xom.Element
import org.concordion.api.ImplementationStatus.EXPECTED_TO_PASS
import org.concordion.api.ImplementationStatus.UNIMPLEMENTED
import org.concordion.api.ImplementationStatus.implementationStatusFor
import org.concordion.internal.ConcordionBuilder.NAMESPACE_CONCORDION_2007

class ExamExampleCommand(tag: String) : ExamCommand("example", tag) {

    override fun beforeParse(elem: Element) {
        transformToConcordionExample(elem)
        super.beforeParse(elem)
    }

    private fun transformToConcordionExample(elem: Element) {
        elem.addCcAttr("example", elem.getAttributeValue("name"))
        elem.addCcAttr(
            "status",
            if (elem.childElements.size() == 0) UNIMPLEMENTED.tag.apply { elem.appendChild(this) }
            else implementationStatusFor(elem.getAttributeValue("status") ?: EXPECTED_TO_PASS.tag).tag
        )
    }
}

private fun Element.addCcAttr(name: String, value: String) {
    if (value.isNotEmpty()) {
        addAttribute(
            Attribute(name, value).apply { setNamespace("c", NAMESPACE_CONCORDION_2007) }
        )
    }
}
