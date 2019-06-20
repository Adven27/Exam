package com.adven.concordion.extensions.exam.core

import com.adven.concordion.extensions.exam.core.html.*
import nu.xom.Document
import nu.xom.Element
import org.concordion.api.ImplementationStatus
import org.concordion.api.ImplementationStatus.*
import org.concordion.api.listener.*
import java.util.*

internal class ExamExampleListener : ExampleListener {
    override fun beforeExample(event: ExampleEvent) {}

    override fun afterExample(event: ExampleEvent) {
        val summary = event.resultSummary
        val status = summary.implementationStatus
        val card = Html(event.element)
        removeConcordionExpectedToFailWarning(card)
        footerOf(card)(
            stat()(
                pill(summary.successCount, "success"),
                pill(summary.ignoredCount, "secondary"),
                pill(summary.failureCount, "warning"),
                pill(summary.exceptionCount, "danger"),
                if (status != null) badgeFor(status) else null))
    }

    private fun removeConcordionExpectedToFailWarning(card: Html) {
        card.first("p")?.let { card.remove(it) }
    }

    private fun badgeFor(status: ImplementationStatus): Html {
        return when (status) {
            EXPECTED_TO_PASS -> pill(EXPECTED_TO_PASS.tag, "success")
            EXPECTED_TO_FAIL -> pill(EXPECTED_TO_FAIL.tag, "warning")
            UNIMPLEMENTED -> pill(UNIMPLEMENTED.tag, "primary")
            else -> throw UnsupportedOperationException("Unsupported spec implementation status $status")
        }
    }
}

internal class ExamDocumentParsingListener(private val registry: CommandRegistry) : DocumentParsingListener {
    override fun beforeParsing(document: Document) {
        visit(document.rootElement)
        addToTopButton(document)
    }

    private fun addToTopButton(document: Document) {
        Html(org.concordion.api.Element(document.rootElement))(
            button("", ID to "btnToTop", ONCLICK to "topFunction()")(
                italic("").css("fa fa-arrow-up fa-3x")))
    }

    private fun visit(elem: Element) {
        log(org.concordion.api.Element(elem))
        val children = elem.childElements

        for (i in 0 until children.size()) {
            visit(children.get(i))
        }

        if (ExamExtension.NS == elem.namespaceURI) {
            registry.getBy(elem.localName)?.beforeParse(elem)
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

class SpecSummaryListener : SpecificationProcessingListener {
    override fun beforeProcessingSpecification(event: SpecificationProcessingEvent) {}

    override fun afterProcessingSpecification(event: SpecificationProcessingEvent) {
        val body = Html(event.rootElement).first("body")
        if (body != null) {
            val menu = body.findBy("summary")
            if (menu != null) {
                menu.parent().css("pin")
                menu(
                    div(CLASS to "list-group")(
                        *body.descendants("a").filter { "example" == it.attr("data-type") }.flatMap {
                            menuItem(it)
                        }.toTypedArray()))
            }
        }
    }

    private fun menuItem(it: Html): List<Html> {
        val anchor = it.attr("name")!!
        val id = UUID.randomUUID().toString()
        val rootExampleEl = it.parent().parent()
        val item = menuItemA(anchor).attrs("href" to "#$anchor")(
            footerOf(rootExampleEl).firstOrThrow("small").deepClone()
                .css("card-img-overlay m-1").style("padding:0; left:inherit;"))
        val cases = cases(rootExampleEl, id)

        return if (cases.childs().isEmpty())
            listOf(item) else listOf(item(buttonCollapse("cases", id)), cases)
    }

    private fun cases(exampleEl: Html, id: String): Html {
        return div(ID to id, CLASS to "collapse")(
            exampleEl.descendants("tr").filter { "case" == it.attr("data-type") }.map {
                val anchor = it.attr("id")!!
                menuItemA(anchor, italic("").css("fa fa-circle fa-fw"))
                    .attrs("href" to "#$anchor")
                    .muted()
            })
    }
}