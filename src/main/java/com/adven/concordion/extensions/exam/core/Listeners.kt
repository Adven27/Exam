package com.adven.concordion.extensions.exam.core

import com.adven.concordion.extensions.exam.core.ExamExtension.Companion.PARSED_COMMANDS
import com.adven.concordion.extensions.exam.core.html.*
import com.adven.concordion.extensions.exam.core.utils.content
import nu.xom.Attribute
import nu.xom.Document
import nu.xom.Element
import nu.xom.XPathContext
import nu.xom.converters.DOMConverter
import org.concordion.api.ImplementationStatus
import org.concordion.api.ImplementationStatus.*
import org.concordion.api.listener.*
import java.io.ByteArrayInputStream
import java.util.*
import javax.xml.parsers.DocumentBuilderFactory
import kotlin.collections.ArrayList
import kotlin.collections.List
import kotlin.collections.MutableList
import kotlin.collections.filter
import kotlin.collections.flatMap
import kotlin.collections.forEach
import kotlin.collections.isNotEmpty
import kotlin.collections.listOf
import kotlin.collections.map
import kotlin.collections.set
import kotlin.collections.toTypedArray
import org.concordion.api.Element as ConcordionElement

val examplesToFocus: MutableList<String?> = ArrayList()

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

        if (summary.failureCount > 0 || summary.exceptionCount > 0) {
            examplesToFocus.add(card.attr("id"))
        }
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

class FocusOnErrorsListener : SpecificationProcessingListener {
    override fun beforeProcessingSpecification(event: SpecificationProcessingEvent) {
        examplesToFocus.clear()
    }

    override fun afterProcessingSpecification(event: SpecificationProcessingEvent) {
        if (examplesToFocus.isNotEmpty()) {
            val body = Html(event.rootElement).first("body")
            body!!.descendants("a")
                .filter { "example" == it.attr("data-type") }
                .map { it.parent().parent() }
                .filter { !examplesToFocus.contains(it.attr("id")) }
                .forEach { bodyOf(it).attr("class", "card-body collapse") }
        }
    }
}

internal class ExamDocumentParsingListener(private val registry: CommandRegistry) : DocumentParsingListener {
    override fun beforeParsing(document: Document) {
        document.rootElement.apply {
            resolveIncludes()
            visit(this)
            addToTopButton(this)
        }
    }

    private fun Element.resolveIncludes() {
        val name = "include"
        this.query(".//$name | .//e:$name", XPathContext("e", ExamExtension.NS))?.let {
            for (i in 0 until it.size()) {
                val node = it[i]
                val template = DOMConverter.convert(
                    loadXMLFromString(Html(ConcordionElement(node as Element)).content())).rootElement
                val parent = node.parent
                val position = parent.indexOf(node)
                for (j in template.childElements.size() - 1 downTo 0) {
                    parent.insertChild(template.childElements[j].apply { detach() }, position)
                }
                parent.removeChild(node)
            }
        }
    }

    private fun addToTopButton(elem: Element) {
        Html(ConcordionElement(elem))(
            button("", ID to "btnToTop", ONCLICK to "topFunction()")(
                italic("").css("fa fa-arrow-up fa-3x")))
    }

    private fun visit(elem: Element) {
        log(ConcordionElement(elem))
        val children = elem.childElements

        for (i in 0 until children.size()) {
            visit(children.get(i))
        }

        if (ExamExtension.NS == elem.namespaceURI && registry.commands().map { it.name() }.contains(elem.localName)) {
            val cmdId = UUID.randomUUID().toString()
            PARSED_COMMANDS[cmdId] = elem.toXML()
            elem.addAttribute(Attribute("cmdId", cmdId))
            registry.getBy(elem.localName)?.beforeParse(elem)
        }
    }

    private fun log(elem: ConcordionElement) {
        if ((elem.getAttributeValue("print") ?: "false").toBoolean()) {
            val sb = StringBuilder()
            for (e in elem.childElements) {
                sb.append("\n" + e.toXML())
            }
            elem.prependChild(codeXml(sb.toString()).el())
        }
    }
}

fun loadXMLFromString(xml: String): org.w3c.dom.Document? {
    return DocumentBuilderFactory.newInstance().let {
        it.isNamespaceAware = true
        it.newDocumentBuilder().parse(ByteArrayInputStream(xml.toByteArray()))
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
                menuItemA(anchor, italic(""))
                    .attrs("href" to "#$anchor")
                    .muted()
            })
    }
}