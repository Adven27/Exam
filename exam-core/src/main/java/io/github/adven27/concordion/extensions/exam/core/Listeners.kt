package io.github.adven27.concordion.extensions.exam.core

import io.github.adven27.concordion.extensions.exam.core.ExamExtension.Companion.PARSED_COMMANDS
import io.github.adven27.concordion.extensions.exam.core.html.CLASS
import io.github.adven27.concordion.extensions.exam.core.html.Html
import io.github.adven27.concordion.extensions.exam.core.html.ID
import io.github.adven27.concordion.extensions.exam.core.html.ONCLICK
import io.github.adven27.concordion.extensions.exam.core.html.bodyOf
import io.github.adven27.concordion.extensions.exam.core.html.button
import io.github.adven27.concordion.extensions.exam.core.html.buttonCollapse
import io.github.adven27.concordion.extensions.exam.core.html.codeHighlight
import io.github.adven27.concordion.extensions.exam.core.html.div
import io.github.adven27.concordion.extensions.exam.core.html.footerOf
import io.github.adven27.concordion.extensions.exam.core.html.italic
import io.github.adven27.concordion.extensions.exam.core.html.menuItemA
import io.github.adven27.concordion.extensions.exam.core.html.pill
import io.github.adven27.concordion.extensions.exam.core.utils.content
import io.github.adven27.concordion.extensions.exam.core.utils.prettyXml
import nu.xom.Attribute
import nu.xom.Document
import nu.xom.Element
import nu.xom.XPathContext
import nu.xom.converters.DOMConverter
import org.concordion.api.ImplementationStatus
import org.concordion.api.ImplementationStatus.EXPECTED_TO_FAIL
import org.concordion.api.ImplementationStatus.EXPECTED_TO_PASS
import org.concordion.api.ImplementationStatus.IGNORED
import org.concordion.api.ImplementationStatus.UNIMPLEMENTED
import org.concordion.api.listener.DocumentParsingListener
import org.concordion.api.listener.ExampleEvent
import org.concordion.api.listener.ExampleListener
import org.concordion.api.listener.SpecificationProcessingEvent
import org.concordion.api.listener.SpecificationProcessingListener
import org.concordion.internal.FailFastException
import java.io.ByteArrayInputStream
import java.io.File
import java.util.UUID
import java.util.function.Predicate
import javax.xml.parsers.DocumentBuilderFactory
import kotlin.collections.set
import org.concordion.api.Element as ConcordionElement

val examplesToFocus: MutableList<String?> = ArrayList()

interface SkipDecider : Predicate<ExampleEvent> {
    fun reason(): String

    class NoSkip : SkipDecider {
        override fun reason(): String = ""

        override fun test(t: ExampleEvent): Boolean = false
    }
}

internal class ExamExampleListener(private val skipDecider: SkipDecider) : ExampleListener {
    override fun beforeExample(event: ExampleEvent) {
        val name = event.resultSummary.specificationDescription.substringAfterLast(File.separator)
        val elem = event.element
        if (skipDecider.test(event)) {
            elem.appendSister(
                ConcordionElement("div").apply {
                    appendText("Example \"$name\" is skipped by ${skipDecider.javaClass.simpleName} because ${skipDecider.reason()}")
                }
            )
            elem.parentElement.removeChild(elem)
            throw FailFastException("Skipping example", AssertionError("Skipping example"))
        }
    }

    override fun afterExample(event: ExampleEvent) {
        val summary = event.resultSummary
        val status = summary.implementationStatus
        val card = Html(event.element)
        removeConcordionExpectedToFailWarning(card)
       /* footerOf(card)(
            stat()(
                pill(summary.successCount, "success"),
                pill(summary.ignoredCount, "secondary"),
                pill(summary.failureCount, "warning"),
                pill(summary.exceptionCount, "danger"),
                if (status != null && status != EXPECTED_TO_PASS) badgeFor(status) else null
            )
        )*/

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
            IGNORED -> pill(IGNORED.tag, "primary")
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
                    loadXMLFromString(Html(ConcordionElement(node as Element)).content())
                ).rootElement
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
                italic("").css("fa fa-arrow-up")
            )
        )
    }

    private fun visit(elem: Element) {
        log(elem)
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

    private fun log(elem: Element) {
        if ((elem.getAttributeValue("print") ?: "false").toBoolean()) {
            ConcordionElement(elem).prependChild(
                codeHighlight(
                    "xml",
                    Document(elem.copy() as Element).prettyXml()
                        .replace(" xmlns:e=\"http://exam.extension.io\"", "")
                        .replace(" print=\"true\"", "")
                        .lines()
                        .filterNot { it.startsWith("<?xml version") }
                        .filterNot { it.isBlank() }
                        .joinToString(separator = "\n")
                ).css("mt-2").el()
            )
        }
    }
}

fun loadXMLFromString(xml: String): org.w3c.dom.Document? = DocumentBuilderFactory.newInstance().let {
    it.isNamespaceAware = true
    it.newDocumentBuilder().parse(ByteArrayInputStream(xml.toByteArray()))
}

class SpecSummaryListener : SpecificationProcessingListener {
    override fun beforeProcessingSpecification(event: SpecificationProcessingEvent) {
        // NOOP
    }

    @Suppress("SpreadOperator")
    override fun afterProcessingSpecification(event: SpecificationProcessingEvent) {
        val body = Html(event.rootElement).first("body")
        if (body != null) {
            val menu = body.findBy("summary")
            val examples = body.descendants("a").filter { "example" == it.attr("data-type") }
            if (menu != null && examples.isNotEmpty()) {
                menu.parent().css("pin")
                menu(div(CLASS to "list-group")(*examples.flatMap { menuItem(it) }.toTypedArray()))
            }
        }
    }

    private fun menuItem(it: Html): List<Html> {
        val anchor = it.attr("name")!!
        val id = UUID.randomUUID().toString()
        val rootExampleEl = it.parent().parent()
        val item = menuItemA(anchor).attrs("href" to "#$anchor")(
            footerOf(rootExampleEl)
                .firstOrThrow("small")
                .deepClone()
                .css("card-img-overlay m-1")
                .style("padding:0; left:inherit;")
                .above(pill(extractElapsedTime(rootExampleEl), "light"))
        )
        val cases = cases(rootExampleEl, id)

        return if (cases.childs().isEmpty()) {
            listOf(item)
        } else listOf(item(buttonCollapse("cases", id)), cases)
    }

    private fun extractElapsedTime(card: Html): String =
        card.childs().firstOrNull { "time-fig" == it.attr("class") }.let {
            card.remove(it)
            it?.text() ?: ""
        }

    private fun cases(exampleEl: Html, id: String): Html {
        return div(ID to id, CLASS to "collapse")(
            exampleEl.descendants("tr").filter { "case" == it.attr("data-type") }.map {
                val anchor = it.attr("id")!!
                menuItemA(anchor, italic(""))
                    .attrs("href" to "#$anchor", "style" to "font-size: small;")
                    .muted()
            }
        )
    }
}
