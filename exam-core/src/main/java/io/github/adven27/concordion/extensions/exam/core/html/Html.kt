@file:JvmName("HtmlBuilder")

package io.github.adven27.concordion.extensions.exam.core.html

import io.github.adven27.concordion.extensions.exam.core.resolve
import org.concordion.api.CommandCall
import org.concordion.api.Element
import org.concordion.api.Evaluator
import java.util.Optional
import java.util.UUID

const val ID = "id"
const val ONCLICK = "onclick"
const val CLASS = "class"
const val STYLE = "style"
const val NAME = "name"
const val TABLE = "table"

class Html(val el: Element) {
    constructor(tag: String) : this(Element(tag))
    constructor(tag: String, vararg attrs: Pair<String, String>) : this(tag, null, *attrs)
    constructor(tag: String, text: String? = null, vararg attrs: Pair<String, String>) : this(Element(tag)) {
        if (text != null) {
            this.text(text)
        }
        attrs(*attrs)
    }

    @JvmName("childs")
    operator fun invoke(vararg htmls: Html?): Html {
        htmls.filterNotNull().forEach { el.appendChild(it.el) }
        return this
    }

    @JvmName("childs")
    operator fun invoke(htmls: Collection<Html?>): Html {
        htmls.filterNotNull().forEach { el.appendChild(it.el) }
        return this
    }

    fun childs(): List<Html> {
        val result = ArrayList<Html>()
        for (e in el.childElements) {
            result.add(Html(e))
        }
        return result
    }

    fun childs(name: String): List<Html> {
        val result = ArrayList<Html>()
        for (e in el.getChildElements(name)) {
            result.add(Html(e))
        }
        return result
    }

    fun attrs(vararg attrs: Pair<String, String>): Html {
        attrs.forEach {
            el.addAttribute(it.first, it.second)
        }
        return this
    }

    fun attr(name: String): String? = el.getAttributeValue(name)

    fun attrOrFail(name: String): String =
        el.getAttributeValue(name) ?: throw IllegalArgumentException("Attribute $name not found")

    fun attr(attr: String, value: String): Html {
        el.addAttribute(attr, value)
        return this
    }

    infix fun collapse(target: String) =
        attrs("data-bs-toggle" to "collapse", "data-bs-target" to "#$target", "aria-expanded" to "true")

    infix fun css(classes: String): Html {
        el.addStyleClass(classes)
        return this
    }

    infix fun style(style: String): Html {
        attrs("style" to style)
        return this
    }

    fun muted(): Html {
        css("text-muted")
        return this
    }

    infix fun dropAllTo(element: Html): Html {
        moveChildrenTo(element)
        el.appendChild(element.el)
        return this
    }

    infix fun prependChild(html: Html): Html {
        el.prependChild(html.el)
        return this
    }

    infix fun below(html: Html): Html {
        el.appendSister(html.el)
        return this
    }

    @JvmOverloads
    fun takeAwayAttr(name: String, eval: Evaluator? = null): String? {
        var attr = attr(name)
        if (attr != null) {
            attr = eval?.resolve(attr) ?: attr
//            el.removeAttribute(name)
        }
        return attr
    }

    fun takeAwayAttr(name: String, def: String): String = takeAwayAttr(name) ?: def
    fun takeAwayAttr(attrName: String, def: String, eval: Evaluator? = null) = takeAwayAttr(attrName, eval) ?: def

    fun el() = el

    fun success(): Html {
        css("bd-callout bd-callout-success")
        return this
    }

    fun panel(header: String, lvl: Int): Html = generateId().let {
        this(
            title(header, it, lvl),
            body(this, it)
        ).css("exam-example mb-3").attrs("data-type" to "example")
    }

    private fun body(root: Html, id: String) = div()
        .css("bd-example collapse show rounded bg-light bg-gradient")
        .attrs("id" to id).apply {
            root.moveChildrenTo(this)
        }

    private fun title(header: String, id: String, lvl: Int) = div()(
        tag("h$lvl").text(header).style("visibility: hidden; height:0;").css("test-class"),
        italic("", "class" to "far fa-caret-square-down"),
        tag("span").text(" "),
        tag("a").text(header).css("bd-example-title text-muted fw-lighter")
    ) collapse id

    fun localName() = el.localName!!

    fun hasChildren() = el.hasChildren()

    fun moveChildrenTo(html: Html): Html {
        el.moveChildrenTo(html.el)
        return this
    }

    fun moveAttributesTo(html: Html): Html {
        el.moveAttributesTo(html.el)
        return this
    }

    fun text() = el.text!!

    infix fun text(txt: String): Html {
        el.appendText(txt)
        return this
    }

    infix fun insteadOf(original: Element): Html {
        original.moveChildrenTo(this.el)
        original.moveAttributesTo(this.el)
        // FIXME may skip some attributes after first turn, repeat to move the rest... probably bug
        original.moveAttributesTo(this.el)
        original.appendSister(this.el)
        original.parentElement.removeChild(original)
        return this
    }

    infix fun insteadOf(original: Html) = insteadOf(original.el)

    fun first(tag: String): Html? {
        val first = el.childElements.firstOrNull { it.localName == tag }
        return if (first == null) null else Html(first)
    }

    fun last(tag: String): Html? {
        val last = el.childElements.lastOrNull { it.localName == tag }
        return if (last == null) null else Html(last)
    }

    fun firstOptional(tag: String): Optional<Html> = Optional.ofNullable(first(tag))

    fun all(tag: String): Collection<Html> {
        return el.childElements.asList().filter { it.localName == tag }.map { Html(it) }
    }

    fun firstOrThrow(tag: String) = first(tag) ?: throw IllegalStateException("<$tag> tag is required")

    fun removeChildren(): Html {
        moveChildrenTo(Html("tmp"))
        return this
    }

    fun remove(vararg children: Html?): Html {
        children.filterNotNull().forEach { el.removeChild(it.el) }
        return this
    }

    fun deepClone() = Html(el.deepClone())

    fun parent() = Html(el.parentElement)

    fun findBy(id: String): Html? {
        val e = this.el.getElementById(id)
        return if (e == null) null else Html(e)
    }

    fun descendants(tag: String) = this.el.getDescendantElements(tag).toList().map(::Html)

    fun span(txt: String? = null, vararg attrs: Pair<String, String>) {
        (Html("span", txt, *attrs))
    }

    fun tooltip(text: String, decorate: Boolean = false) = attrs(
        "title" to text,
        "data-toggle" to "tooltip",
        "data-placement" to "top",
        "style" to if (decorate) "text-decoration: underline grey dashed !important;" else ""
    )

    fun removeClass(name: String): Html {
        el.addAttribute(
            "class",
            el.getAttributeValue("class").let {
                it.split(" ").filterNot { it == name }.joinToString(" ")
            }
        )
        return this
    }

    override fun toString(): String {
        return el.toXML()
    }
}

fun div(txt: String? = null, vararg attrs: Pair<String, String>) = Html("div", txt, *attrs)

fun div(vararg attrs: Pair<String, String>) = Html("div", *attrs)

fun table(vararg attrs: Pair<String, String>) = table(Html("table", *attrs))

fun table(el: Html): Html = el.css("table table-sm caption-top")

fun table(el: Element): Html = table(Html(el))

fun thead(vararg attrs: Pair<String, String>) = Html("thead", *attrs).css("thead-default")

@JvmOverloads
fun th(txt: String? = null, vararg attrs: Pair<String, String>) = Html("th", txt, *attrs)

fun tbody(vararg attrs: Pair<String, String>) = Html("tbody", *attrs)

fun tr(vararg attrs: Pair<String, String>) = Html("tr", *attrs)

fun trWithTDs(vararg cellElements: Html): Html {
    val tr = tr()
    cellElements.forEach { tr(td()(it)) }
    return tr
}

fun td(txt: String? = null, vararg attrs: Pair<String, String>) = Html("td", txt, *attrs)

fun td(vararg attrs: Pair<String, String>) = Html("td", *attrs)

@JvmOverloads
fun italic(txt: String? = null, vararg attrs: Pair<String, String>) = Html("i", txt, *attrs)

fun code(txt: String) = Html("code", txt)

@JvmOverloads
fun span(txt: String? = null, vararg attrs: Pair<String, String>) = Html("span", txt, *attrs)

fun badge(txt: String, style: String) = span(txt).css("badge bg-$style me-1 ms-1")

fun pill(count: Long, style: String) = pill(if (count == 0L) "" else count.toString(), style)

fun pill(txt: String, style: String) = span(txt).css("badge badge-pill badge-$style")

fun `var`(txt: String) = Html("var", txt)

fun link(txt: String) = Html("a", txt)

fun link(txt: String, vararg childs: Html) = link(txt)(*childs)

fun link(txt: String, src: String) = link(txt).attrs("href" to src)

@JvmOverloads
fun thumbnail(src: String, size: Int = 360) = link("", src)(image(src, size, size))

fun imageOverlay(src: String, size: Int, title: String, desc: String, descStyle: String): Html {
    return div().css("card bg-light")(
        link("", src)(
            image(src, size, size)
        ),
        div().css("card-img-top $descStyle")(
            h(4, title),
            paragraph(desc) css "card-text"
        )
    )
}

fun noImageOverlay(title: String, desc: String, descStyle: String): Html {
    return div().css("card bg-light")(
        div().css("card-img-top $descStyle")(
            h(4, title),
            paragraph(desc) css "card-text"
        )
    )
}

fun descendantTextContainer(element: Element): Element {
    val child = element.childElements.firstOrNull()
    return if (child == null) element else descendantTextContainer(child)
}

fun image(src: String) = Html("image").attrs("src" to src)

fun image(src: String, width: Int, height: Int) = image(src).css("img-thumbnail")
    .attrs("width" to "$width", "height" to "$height")

fun h(n: Int, text: String) = Html("h$n", text)

@JvmOverloads
fun caption(txt: String? = null) = Html("caption", txt)

@JvmOverloads
fun pre(txt: String? = null, vararg attrs: Pair<String, String>) = Html("pre", txt, *attrs)

fun paragraph(txt: String) = Html("p", txt)

fun codeXml(text: String?) = pre(text ?: "") css "xml card"

fun codeHighlight(text: String?, lang: String? = null) =
    pre().attrs("class" to "doc-code ${if (lang != null) "language-$lang" else ""}")(code(text ?: ""))

fun tag(tag: String) = Html(tag)

fun body() = Html("body")

fun body(txt: String) = Html("body", txt)

fun ul(vararg attrs: Pair<String, String>) = Html("ul", *attrs)

fun list() = ul() css "list-group"

@JvmOverloads
fun li(text: String? = null) = Html("li", text)

fun menuItemA(txt: String) =
    link(txt) css "list-group-item list-group-item-action" style "border-left: none; border-right: none;"

fun menuItemA(txt: String, vararg children: Html) =
    link(txt, *children) css "list-group-item list-group-item-action" style "border-left: none; border-right: none;"

fun button(txt: String = "", vararg attrs: Pair<String, String>) =
    Html("button", txt, *attrs).attrs("type" to "button") css "btn btn-light btn-sm text-muted me-1"

fun buttonCollapse(txt: String, target: String) = button(txt) collapse target

fun divCollapse(txt: String, target: String) = div(txt).css("far fa-caret-square-down") collapse target

fun footerOf(card: Html) = Html(card.el.getChildElements("div")[2])

fun stat() = Html("small")

fun CommandCall?.html() = Html(this!!.element)
fun CommandCall?.takeAttr(name: String) = html().takeAwayAttr(name)
fun CommandCall?.attr(name: String, def: String) = html().attr(name) ?: def

fun generateId(): String = "e${UUID.randomUUID()}"
