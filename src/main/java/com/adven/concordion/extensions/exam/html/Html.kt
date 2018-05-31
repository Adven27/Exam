package com.adven.concordion.extensions.exam.html

import com.adven.concordion.extensions.exam.PlaceholdersResolver
import org.concordion.api.Element
import org.concordion.api.Evaluator

import java.util.ArrayList

class Html(internal val el: Element) {

    constructor(tag: String) : this(Element(tag))

    fun childs(vararg htmls: Html?): Html {
        htmls.forEach {
            if (it != null) {
                el.appendChild(it.el)
            }
        }
        return this
    }

    fun childs(): List<Html> {
        val result = ArrayList<Html>()
        for (e in el.childElements) {
            result.add(Html(e))
        }
        return result
    }

    fun attr(name: String): String? = el.getAttributeValue(name)

    fun attr(attr: String, value: String): Html {
        el.addAttribute(attr, value)
        return this
    }

    fun collapse(target: String) = attr("data-toggle", "collapse").attr("data-target", "#$target").attr("aria-expanded", "true").attr("aria-controls", target)

    fun css(classes: String): Html {
        el.addStyleClass(classes)
        return this
    }

    fun style(style: String): Html {
        attr("style", style)
        return this
    }

    fun muted(): Html {
        css("text-muted")
        return this
    }

    fun dropAllTo(element: Html): Html {
        moveChildrenTo(element)
        el.appendChild(element.el)
        return this
    }

    fun above(html: Html): Html {
        el.prependChild(html.el)
        return this
    }

    fun below(html: Html): Html {
        el.appendSister(html.el)
        return this
    }

    fun takeAwayAttr(name: String, eval: Evaluator?): String? {
        var attr = attr(name)
        if (attr != null) {
            attr = if (eval != null) PlaceholdersResolver.resolveJson(attr, eval) else attr
            el.removeAttribute(name)
        }
        return attr
    }

    fun takeAwayAttr(name: String) = takeAwayAttr(name, null)

    fun takeAwayAttr(name: String, def: String): String = takeAwayAttr(name) ?: def

    fun takeAwayAttr(attrName: String, def: String, eval: Evaluator) = takeAwayAttr(attrName, eval) ?: def

    fun el() = el

    fun success(): Html {
        css("bd-callout bd-callout-success")
        return this
    }

    fun panel(header: String): Html {
        css("card mb-3")
        val id = header.hashCode().toString()
        val body = div().css("card-body collapse show").attr("id", id)
        moveChildrenTo(body)
        this.childs(
                div().css("card-header").childs(
                        link(header).attr("name", header).attr("data-type", "example")
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

    fun text(txt: String): Html {
        el.appendText(txt)
        return this
    }

    fun insteadOf(original: Element): Html {
        original.moveChildrenTo(this.el)
        original.moveAttributesTo(this.el)
        original.appendSister(this.el)
        original.parentElement.removeChild(original)
        return this
    }

    fun insteadOf(original: Html) = insteadOf(original.el)

    fun first(tag: String): Html? {
        val first = this.el.getFirstChildElement(tag)
        return if (first == null) null else Html(first)
    }

    fun firstOrThrow(tag: String) = first(tag) ?: throw IllegalStateException("<$tag> tag is required")

    fun removeAllChild(): Html {
        this.moveChildrenTo(Html("tmp"))
        return this
    }

    fun remove(html: Html) = el.removeChild(html.el)

    fun remove(vararg children: Html?): Html {
        children.forEach {
            if (it != null) {
                remove(it)
            }
        }
        return this
    }

    fun deepClone() = Html(el.deepClone())

    fun parent() = Html(el.parentElement)

    companion object {

        fun div() = Html("div")

        fun div(txt: String) = div().text(txt)

        @JvmOverloads
        fun table(el: Element = Element("table")) = table(Html(el))

        fun table(el: Html): Html = el.css("table")

        @JvmOverloads
        fun tableSlim(el: Element = Element("table")) = tableSlim(Html(el))

        fun tableSlim(el: Html) = el.css("table table-sm")

        fun thead() = Html("thead").css("thead-default")

        fun th(txt: String) = th().text(txt)

        fun th() = Html("th")

        fun tbody() = Html("tbody")

        fun tr() = Html("tr")

        fun trWithTDs(vararg cellElements: Html): Html {
            val tr = tr()
            cellElements.forEach { tr.childs(td(it)) }
            return tr
        }

        fun td() = Html("td")

        fun td(vararg childs: Html) = Html("td").childs(*childs)

        fun td(txt: String) = td().text(txt)

        fun italic(txt: String) = italic().text(txt)

        fun italic() = Html("i")

        fun code(txt: String) = Html("code").text(txt)

        fun span(txt: String) = Html("span").text(txt)

        fun badge(txt: String, style: String) = span(txt).css("badge badge-$style ml-1 mr-1")

        fun pill(count: Long, style: String) = pill(if (count == 0L) "" else count.toString(), style)

        fun pill(txt: String, style: String) = span(txt).css("badge badge-pill badge-$style")

        fun `var`(txt: String) = Html("var").text(txt)

        fun link(txt: String) = Html("a").text(txt)

        fun link(txt: String, vararg childs: Html) = Html("a").childs(*childs).text(txt)

        fun link(txt: String, src: String) = link(txt).attr("href", src)

        @JvmOverloads
        fun thumbnail(src: String, size: Int = 360) = link("", src).childs(image(src, size, size))

        fun imageOverlay(src: String, size: Int, title: String, desc: String, descStyle: String): Html {
            return div().css("card bg-light").childs(
                    link("", src).childs(
                            image(src, size, size)
                    ),
                    div().css("card-img-top $descStyle").childs(
                            h(4, title),
                            paragraph(desc).css("card-text")

                    )
            )
        }

        fun image(src: String) = Html("image").attr("src", src)

        fun image(src: String, width: Int, height: Int) = image(src).css("img-thumbnail").attr("width", width.toString()).attr("height", height.toString())

        fun h(n: Int, text: String) = Html("h$n").text(text)

        fun caption(txt: String) = caption().text(txt)

        fun caption() = Html("caption")

        fun pre() = Html("pre")

        fun pre(txt: String) = pre().text(txt)

        fun paragraph(txt: String) = Html("p").text(txt)

        fun codeXml(text: String?) = pre(text ?: "").css("xml card")

        fun tag(tag: String) = Html(tag)

        fun ul() = Html("ul")

        fun list() = ul().css("list-group")

        fun li(text: String) = li().text(text)

        fun li() = Html("li")

        fun menuItemLi() = li().css("list-group-item list-group-item-action d-flex justify-content-between align-items-center")

        fun menuItemA(txt: String) = link(txt).css("list-group-item list-group-item-action")

        fun menuItemA(txt: String, vararg children: Html) = link(txt, *children).css("list-group-item list-group-item-action")

        fun button(txt: String) = Html("button").text(txt).css("btn btn-light btn-sm text-muted ml-1").attr("type", "button")

        fun buttonCollapse(txt: String, target: String) = button(txt).collapse(target)

        fun footerOf(card: Element) = Html(card.getChildElements("div")[2])

        fun stat() = Html("small")
    }
}