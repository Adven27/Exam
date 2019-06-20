@file:JvmName("HtmlBuilder")

package com.adven.concordion.extensions.exam.core.html

import com.adven.concordion.extensions.exam.core.resolveJson
import org.concordion.api.CommandCall
import org.concordion.api.Element
import org.concordion.api.Evaluator

const val ID = "id"
const val ONCLICK = "onclick"
const val CLASS = "class"
const val STYLE = "style"
const val NAME = "name"

class Html(internal val el: Element) {
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

    fun attrs(vararg attrs: Pair<String, String>): Html {
        attrs.forEach {
            el.addAttribute(it.first, it.second)
        }
        return this
    }

    fun attr(name: String): String? = el.getAttributeValue(name)

    fun attr(attr: String, value: String): Html {
        el.addAttribute(attr, value)
        return this
    }

    infix fun collapse(target: String) = attrs(
        "data-toggle" to "collapse",
        "data-target" to "#$target",
        "aria-expanded" to "true",
        "aria-controls" to target)

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

    infix fun above(html: Html): Html {
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
            attr = if (eval != null) resolveJson(attr, eval) else attr
            el.removeAttribute(name)
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

    fun panel(header: String): Html {
        css("card mb-3")
        val id = header.hashCode().toString()
        val body = div().css("card-body collapse show").attrs("id" to id)
        moveChildrenTo(body)
        this(
            div().css("card-header")(
                link(header).attrs("name" to header, "data-type" to "example")
            ).collapse(id)
        )
        val footer = div().css("card-footer text-muted").collapse(id)
        el.appendChild(body.el)
        el.appendChild(footer.el())
        return this
    }

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
        original.appendSister(this.el)
        original.parentElement.removeChild(original)
        return this
    }

    infix fun insteadOf(original: Html) = insteadOf(original.el)

    fun first(tag: String): Html? {
        val first = el.childElements.firstOrNull { it.localName == tag }
        return if (first == null) null else Html(first)
    }

    fun firstOrThrow(tag: String) = first(tag) ?: throw IllegalStateException("<$tag> tag is required")

    fun removeAllChild(): Html {
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

    fun span(txt: String? = null, vararg attrs: Pair<String, String>, block: Html.() -> Unit) {
        (Html("span", txt, *attrs))
    }
}

fun div(txt: String? = null, vararg attrs: Pair<String, String>) = Html("div", txt, *attrs)

fun div(vararg attrs: Pair<String, String>) = Html("div", *attrs)

fun div(vararg attrs: Pair<String, String>, block: Html.() -> Unit) = Html("div", *attrs)

fun table(vararg attrs: Pair<String, String>) = table(Html("table", *attrs))

fun table(el: Html): Html = el.css("table table-sm")

fun table(el: Element): Html = table(Html(el))

@JvmOverloads
fun tableSlim(el: Element = Element("table")) = tableSlim(Html(el))

fun tableSlim(el: Html) = el.css("table table-sm")

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

fun badge(txt: String, style: String) = span(txt).css("badge badge-$style ml-1 mr-1")

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

fun image(src: String) = Html("image").attrs("src" to src)

fun image(src: String, width: Int, height: Int) = image(src).css("img-thumbnail")
    .attrs("width" to "$width", "height" to "$height")

fun h(n: Int, text: String) = Html("h$n", text)

@JvmOverloads
fun caption(txt: String? = null) = Html("caption", txt)

@JvmOverloads
fun pre(txt: String? = null) = Html("pre", txt)

fun paragraph(txt: String) = Html("p", txt)

fun codeXml(text: String?) = pre(text ?: "") css "xml card"

fun tag(tag: String) = Html(tag)

fun body() = Html("body")

fun body(txt: String) = Html("body", txt)

fun ul() = Html("ul")

fun list() = ul() css "list-group"

@JvmOverloads
fun li(text: String? = null) = Html("li", text)

fun menuItemLi() = li() css "list-group-item list-group-item-action d-flex justify-content-between align-items-center"

fun menuItemA(txt: String) = link(txt) css "list-group-item list-group-item-action"

fun menuItemA(txt: String, vararg children: Html) = link(txt, *children) css "list-group-item list-group-item-action"

fun button(txt: String = "", vararg attrs: Pair<String, String>) = Html("button", txt, *attrs).attrs("type" to "button") css "btn btn-light btn-sm text-muted ml-1"

fun buttonCollapse(txt: String, target: String) = button(txt) collapse target

fun footerOf(card: Html) = Html(card.el.getChildElements("div")[2])

fun stat() = Html("small")

fun CommandCall?.htmlCss(styleClass: String) {
    this.html().css(styleClass)
}

fun CommandCall?.html() = Html(this!!.element)