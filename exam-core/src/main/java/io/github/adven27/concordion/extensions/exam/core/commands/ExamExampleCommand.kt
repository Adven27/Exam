package io.github.adven27.concordion.extensions.exam.core.commands

import io.github.adven27.concordion.extensions.exam.core.ExamDocumentParsingListener
import io.github.adven27.concordion.extensions.exam.core.html.Html
import nu.xom.Attribute
import nu.xom.Element
import org.concordion.api.ImplementationStatus.EXPECTED_TO_PASS
import org.concordion.api.ImplementationStatus.UNIMPLEMENTED
import org.concordion.api.ImplementationStatus.implementationStatusFor
import org.concordion.internal.ConcordionBuilder.NAMESPACE_CONCORDION_2007
import org.concordion.api.Element as ConcordionElement

class ExamExampleCommand(tag: String) : ExamCommand("example", tag) {

    override fun beforeParse(elem: Element) {
        transformToConcordionExample(elem)
        super.beforeParse(elem)
    }

    private fun transformToConcordionExample(elem: Element) {
        elem.addCcAttr("example", elem.getAttributeValue("name"))
        elem.addCcAttr(
            "status",
            if (elem.childElements.size() == 0) {
                elem.appendChild(UNIMPLEMENTED.tag)
                UNIMPLEMENTED.tag
            } else implementationStatusFor(
                elem.getAttributeValue("status") ?: EXPECTED_TO_PASS.tag
            ).tag
        )
        Html(ConcordionElement(elem)).panel(elem.getAttributeValue("name"), levelOfOwnerHeader(elem) + 1)
    }

    private fun levelOfOwnerHeader(elem: Element) = ownerOf(
        ConcordionElement(elem),
        ConcordionElement(elem.parent.document.rootElement)
    )?.localName?.substring(1)?.toInt() ?: 0

    private fun ownerOf(example: ConcordionElement?, content: ConcordionElement): ConcordionElement? {
        var result: ConcordionElement? = null
        for (it in content.getElementById(ExamDocumentParsingListener.CONTENT_ID).childElements) {
            when {
                it.localName.matches("h\\d".toRegex()) -> result = it
                it == example -> break
            }
        }
        return result
    }
}

private fun Element.addCcAttr(name: String, value: String) {
    if (value.isNotEmpty()) {
        addAttribute(
            Attribute(name, value).apply { setNamespace("c", NAMESPACE_CONCORDION_2007) }
        )
    }
}
