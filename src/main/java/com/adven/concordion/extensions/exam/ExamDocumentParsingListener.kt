package com.adven.concordion.extensions.exam

import com.adven.concordion.extensions.exam.html.Html
import com.adven.concordion.extensions.exam.html.button
import com.adven.concordion.extensions.exam.html.codeXml
import com.adven.concordion.extensions.exam.html.italic
import nu.xom.Document
import nu.xom.Element
import org.concordion.api.listener.DocumentParsingListener

internal class ExamDocumentParsingListener(private val registry: CommandRegistry) : DocumentParsingListener {

    override fun beforeParsing(document: Document) {
        visit(document.rootElement)
        addToTopButton(document)
    }

    private fun addToTopButton(document: Document) {
        Html(org.concordion.api.Element(document.rootElement))(
            button("", "id" to "btnToTop", "onclick" to "topFunction()")(
                italic("").css("fa fa-arrow-up fa-3x")
            )
        )
    }

    private fun visit(elem: Element) {
        log(org.concordion.api.Element(elem))
        val children = elem.childElements

        for (i in 0 until children.size()) {
            visit(children.get(i))
        }

        if (ExamExtension.NS == elem.namespaceURI) {
            val command = registry.getBy(elem.localName)
            command?.beforeParse(elem)
        }
    }

    private fun log(elem: org.concordion.api.Element) {
        if ((elem.getAttributeValue("print") ?: "false").toBoolean()) {
            val sb = StringBuilder()
            for (e in elem.childElements) {
                sb.append("\n" + e.toXML())
            }
            elem.prependChild(codeXml(sb.toString()).el())
        }
    }
}