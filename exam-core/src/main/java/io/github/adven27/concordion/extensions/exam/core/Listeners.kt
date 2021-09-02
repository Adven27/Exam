package io.github.adven27.concordion.extensions.exam.core

import com.github.jknack.handlebars.Helper
import io.github.adven27.concordion.extensions.exam.core.ExamDocumentParsingListener.Companion.CONTENT_ID
import io.github.adven27.concordion.extensions.exam.core.ExamExtension.Companion.PARSED_COMMANDS
import io.github.adven27.concordion.extensions.exam.core.commands.BeforeParseExamCommand
import io.github.adven27.concordion.extensions.exam.core.handlebars.HelperMissing.Companion.helpersDesc
import io.github.adven27.concordion.extensions.exam.core.handlebars.MissingHelperException
import io.github.adven27.concordion.extensions.exam.core.html.CLASS
import io.github.adven27.concordion.extensions.exam.core.html.Html
import io.github.adven27.concordion.extensions.exam.core.html.ID
import io.github.adven27.concordion.extensions.exam.core.html.buttonCollapse
import io.github.adven27.concordion.extensions.exam.core.html.codeHighlight
import io.github.adven27.concordion.extensions.exam.core.html.div
import io.github.adven27.concordion.extensions.exam.core.html.footerOf
import io.github.adven27.concordion.extensions.exam.core.html.italic
import io.github.adven27.concordion.extensions.exam.core.html.menuItemA
import io.github.adven27.concordion.extensions.exam.core.html.pill
import io.github.adven27.concordion.extensions.exam.core.html.span
import io.github.adven27.concordion.extensions.exam.core.html.tag
import io.github.adven27.concordion.extensions.exam.core.html.trWithTDs
import nu.xom.Attribute
import nu.xom.Document
import nu.xom.Element
import org.concordion.api.ImplementationStatus.EXPECTED_TO_FAIL
import org.concordion.api.ImplementationStatus.EXPECTED_TO_PASS
import org.concordion.api.ResultSummary
import org.concordion.api.listener.DocumentParsingListener
import org.concordion.api.listener.ExampleEvent
import org.concordion.api.listener.ExampleListener
import org.concordion.api.listener.SpecificationProcessingEvent
import org.concordion.api.listener.SpecificationProcessingListener
import org.concordion.api.listener.ThrowableCaughtEvent
import org.concordion.api.listener.ThrowableCaughtListener
import org.concordion.internal.FailFastException
import java.io.ByteArrayInputStream
import java.io.File
import java.util.UUID
import java.util.function.Predicate
import javax.xml.parsers.DocumentBuilderFactory
import kotlin.collections.set
import org.concordion.api.Element as ConcordionElement

val exampleResults: MutableMap<String, ResultSummary> = mutableMapOf()

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
        val card = Html(event.element)
        card.attrs(
            "data-summary-success" to summary.successCount.toString(),
            "data-summary-ignore" to summary.ignoredCount.toString(),
            "data-summary-failure" to summary.failureCount.toString(),
            "data-summary-exception" to summary.exceptionCount.toString(),
            "data-summary-status" to summary.implementationStatus.tag,
        )
        card.first("div")!!.childs().first().let {
            if (summary.failureCount == 0L && summary.exceptionCount == 0L) {
                it.prependChild(italic("").css("fa fa-check m-1 text-success "))
            } else {
                it.prependChild(italic("").css("fa fa-bug m-1 text-danger "))
            }
        }
//        removeConcordionExpectedToFailWarning(card)
        exampleResults[card.attr("id")!!] = summary
    }

    private fun removeConcordionExpectedToFailWarning(card: Html) {
        card.first("p")?.let { card.remove(it) }
    }
}

@Suppress("NestedBlockDepth")
class FocusOnErrorsListener : SpecificationProcessingListener {
    override fun beforeProcessingSpecification(event: SpecificationProcessingEvent) = exampleResults.clear()
    override fun afterProcessingSpecification(event: SpecificationProcessingEvent) {
        exampleResults.filter { it.failed() }.let { failed ->
            if (failed.isNotEmpty()) {
                (exampleResults - failed.keys).forEach {
                    findExample(event.rootElement, it.key)?.collapse()
                }

                failed.forEach { (id, summary) ->
                    val example = findExample(event.rootElement, id)
                    example?.first("p")?.first("a")?.invoke(
                        summary.successCount.toPill("success"),
                        summary.ignoredCount.toPill("secondary"),
                        summary.failureCount.toPill("warning"),
                        summary.exceptionCount.toPill("danger"),
                        pill(summary.implementationStatus.tag, "warning"),
                    )
                    ownerOf(example, event.rootElement)?.let { markWithFailedExampleAnchor(it, id) }
                }
            }
        }
        exampleResults.clear()
    }

    private fun ownerOf(example: Html?, content: ConcordionElement): ConcordionElement? {
        var result: ConcordionElement? = null
        for (it in content.getElementById(CONTENT_ID).childElements) {
            when {
                it.localName.matches("h\\d".toRegex()) -> result = it
                it == example?.el -> break
            }
        }
        return result
    }

    private fun markWithFailedExampleAnchor(it: org.concordion.api.Element, id: String) {
        Html(it)(
            tag("a").attrs("class" to "examples", "href" to "#$id").text("")
        )
    }

    private fun Long.toPill(style: String): Html? = if (this > 0) pill(toString(), style) else null
    private fun pill(text: String, style: String) = tag("span")
        .css("translate-middle-y badge bg-$style")
        .text(text)
}

private fun Map.Entry<String, ResultSummary>.failed() =
    ((value.exceptionCount > 0 || value.failureCount > 0) && value.implementationStatus == EXPECTED_TO_PASS) ||
        ((value.exceptionCount == 0L || value.failureCount == 0L) && value.implementationStatus == EXPECTED_TO_FAIL)

private fun findExample(el: ConcordionElement, id: String) = Html(el).findBy(id)
private fun Html.collapse() {
    descendants("a").firstByClass("bd-example-title")?.css("collapsed")
    childs("div").firstByClass("bd-example")?.removeClass("show")
}

fun List<Html>.firstByClass(cssClass: String) = firstOrNull { it.attr("class")?.contains(cssClass) ?: false }

internal class ExamDocumentParsingListener(private val registry: CommandRegistry) : DocumentParsingListener {
    companion object {
        const val CONTENT_ID = "content"
        const val MENU_ID = "table-of-contents"
    }

    override fun beforeParsing(document: Document) {
        layout(document)
        visit(document.rootElement)
    }

    private fun layout(document: Document) {
        val content = div("class" to "bd-content ps-lg-4", "id" to CONTENT_ID)
        val toc = div("class" to "bd-toc mt-4 mb-5 my-md-0 ps-xl-3 mb-lg-5 text-muted")(
            tag("strong").css("d-block h6 my-2 pb-2 border-bottom").text("On this page"),
            tag("nav").attrs("id" to MENU_ID, "class" to "js-toc toc toc-right")
        )
        val container = div("class" to "container-fluid")(
            div("class" to "container-xxl my-md-4 bd-layout")(
                Html("main").css("bd-main order-1")(
                    toc, content
                )
            )
        )
        document.rootElement.html().first("body")!!.moveChildrenTo(content)(container)
    }

    private fun visit(elem: Element) {
        elem.childElements.forEach { visit(it) }
        if (ExamExtension.NS == elem.namespaceURI && registry.commands().map { it.name }.contains(elem.localName)) {
            val cmdId = UUID.randomUUID().toString()
            PARSED_COMMANDS[cmdId] = elem.toXML().let {
                it.lines().last().takeWhile { c -> c == ' ' } + it
            }.trimIndent()
            elem.addAttribute(Attribute("cmdId", cmdId))
            registry.getBy(elem.localName)?.let { if (it is BeforeParseExamCommand) it.beforeParse(elem) }
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
                .prependChild(pill(extractElapsedTime(rootExampleEl), "light"))
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

class ErrorListener : ThrowableCaughtListener {
    override fun throwableCaught(event: ThrowableCaughtEvent) {
        val (id, errorMessage) = errorMessage(
            header = "Error while executing command",
            message = "${event.throwable.rootCause().message}",
            help = help(event),
            html = PARSED_COMMANDS[event.element.getAttributeValue("cmdId")]?.let {
                div("while executing:")(
                    codeHighlight(it, "xml")
                )
            } ?: span(" "),
            type = "text"
        )
        val html = Html(event.element)
        when (html.el.localName) {
            "tr" -> html.below(trWithTDs(errorMessage))
            else -> html.below(errorMessage)
        }

        html.parent().remove(html)
        errorMessage.findBy(id)?.below(
            div()(
                html.childs().filter { it.attr("class") in listOf("stackTrace", "stackTraceButton") }.map {
                    it.parent().remove(it)
                    it
                }
            )
        )
    }

    private fun help(event: ThrowableCaughtEvent) =
        if (event.throwable.rootCause() is MissingHelperException) // language=xml
            """
            <p>Available helpers:</p>
            <div class='table-responsive'>${helpersDesc().map { packageWithHelpers(it) }.joinToString("")}</div>
            """.trimIndent()
        else
            ""

    private fun packageWithHelpers(it: Map.Entry<Package, Map<String, Helper<*>>>) = // language=xml
        """
        <var>${it.key}</var>:
        <hr/>
        <dl>
            ${it.value.map { (n, v) -> tr(n, v) }.joinToString("")}
        </dl>
        """.trimIndent()

    private fun tr(n: String, v: Helper<*>) = // language=xml
        """<dt><code>$n</code></dt><dd><pre class='doc-code language-kotlin'><code>$v</code></pre></dd>"""
}
