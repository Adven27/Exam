package com.adven.concordion.extensions.exam.html;

import com.adven.concordion.extensions.exam.PlaceholdersResolver;
import org.concordion.api.Element;
import org.concordion.api.Evaluator;

import java.util.ArrayList;
import java.util.List;

public class Html {
    final Element el;

    public Html(Element el) {
        this.el = el;
    }

    public Html(String tag) {
        this(new Element(tag));
    }

    public static Html div() {
        return new Html("div");
    }

    public static Html div(String txt) {
        return div().text(txt);
    }

    public static Html table() {
        return table(new Element("table"));
    }

    public static Html table(Element el) {
        return table(new Html(el));
    }

    public static Html table(Html el) {
        return el.css("table");
    }

    public static Html tableSlim() {
        return tableSlim(new Element("table"));
    }

    public static Html tableSlim(Element el) {
        return tableSlim(new Html(el));
    }

    public static Html tableSlim(Html el) {
        return el.css("table table-sm");
    }

    public static Html thead() {
        return new Html("thead").css("thead-default");
    }

    public static Html th(String txt) {
        return th().text(txt);
    }

    public static Html th() {
        return new Html("th");
    }

    public static Html tbody() {
        return new Html("tbody");
    }

    public static Html tr() {
        return new Html("tr");
    }

    public static Html trWithTDs(Html... cellElements) {
        Html tr = tr();
        for (Html e : cellElements) {
            tr.childs(td(e));
        }
        return tr;
    }

    public static Html td() {
        return new Html("td");
    }

    public static Html td(Html... childs) {
        return new Html("td").childs(childs);
    }

    public static Html td(String txt) {
        return td().text(txt);
    }

    public static Html italic(String txt) {
        return italic().text(txt);
    }

    public static Html italic() {
        return new Html("i");
    }

    public static Html code(String txt) {
        return new Html("code").text(txt);
    }

    public static Html span(String txt) {
        return new Html("span").text(txt);
    }

    public static Html badge(String txt, String style) {
        return span(txt).css("badge badge-" + style + " ml-1 mr-1");
    }

    public static Html pill(long count, String style) {
        return pill(count == 0 ? "" : String.valueOf(count), style);
    }

    public static Html pill(String txt, String style) {
        return span(txt).css("badge badge-pill badge-" + style);
    }

    public static Html var(String txt) {
        return new Html("var").text(txt);
    }

    public static Html link(String txt) {
        return new Html("a").text(txt);
    }

    public static Html link(String txt, String src) {
        return link(txt).attr("href", src);
    }

    public static Html thumbnail(String src) {
        return thumbnail(src, 360);
    }

    public static Html thumbnail(String src, int size) {
        return link("", src).childs(
                image(src, size, size)
        );
    }

    public static Html imageOverlay(String src, int size, String title, String desc, String descStyle) {
        return div().css("card bg-light").childs(
                link("", src).childs(
                        image(src, size, size)
                ),
                div().css("card-img-top " + descStyle).childs(
                        h(4, title),
                        paragraph(desc).css("card-text")

                )
        );
    }

    public static Html image(String src) {
        return new Html("image").attr("src", src);
    }

    public static Html image(String src, int width, int height) {
        return image(src).css("img-thumbnail").
                attr("width", String.valueOf(width)).
                attr("height", String.valueOf(height));
    }

    public static Html h(int n, String text) {
        return new Html("h" + n).text(text);
    }

    public static Html caption(String txt) {
        return caption().text(txt);
    }

    public static Html caption() {
        return new Html("caption");
    }

    public static Html pre() {
        return new Html("pre");
    }

    public static Html pre(String txt) {
        return pre().text(txt);
    }

    public static Html paragraph(String txt) {
        return new Html("p").text(txt);
    }

    public static Html codeXml(String text) {
        return pre(text).css("xml card");
    }

    public static Html tag(String tag) {
        return new Html(tag);
    }

    public static Html ul() {
        return new Html("ul");
    }

    public static Html list() {
        return ul().css("list-group");
    }

    public static Html li(String text) {
        return li().text(text);
    }

    public static Html li() {
        return new Html("li");
    }

    public static Html menuItemLi() {
        return li().css("list-group-item list-group-item-action d-flex justify-content-between align-items-center");
    }

    public static Html menuItemA(String txt) {
        return link(txt).css("list-group-item list-group-item-action");
    }

    public static Html button(String txt) {
        return new Html("button").
                text(txt).css("btn btn-light btn-sm text-muted ml-1").attr("type", "button");
    }

    public static Html buttonCollapse(String txt, String target) {
        return button(txt).collapse(target);
    }

    public static Html footerOf(Element card) {
        return new Html(card.getChildElements("div")[2]);
    }

    public static Html stat() {
        return new Html("small");
    }

    public Html childs(Html... htmls) {
        for (Html html : htmls) {
            if (html != null) {
                el.appendChild(html.el);
            }
        }
        return this;
    }

    public List<Html> childs() {
        List<Html> result = new ArrayList<>();
        for (Element e : el.getChildElements()) {
            result.add(new Html(e));
        }
        return result;
    }

    public String attr(String name) {
        return el.getAttributeValue(name);
    }

    public Html attr(String attr, String val) {
        el.addAttribute(attr, val);
        return this;
    }

    public Html collapse(String target) {
        return attr("data-toggle", "collapse").
                attr("data-target", "#" + target).
                attr("aria-expanded", "true").
                attr("aria-controls", target);
    }

    public Html css(String classes) {
        el.addStyleClass(classes);
        return this;
    }

    public Html style(String style) {
        attr("style", style);
        return this;
    }

    public Html muted() {
        css("text-muted");
        return this;
    }

    public Html dropAllTo(Html element) {
        moveChildrenTo(element);
        el.appendChild(element.el);
        return this;
    }

    public Html above(Html html) {
        el.prependChild(html.el);
        return this;
    }

    public Html below(Html html) {
        el.appendSister(html.el);
        return this;
    }

    public String takeAwayAttr(String name, Evaluator eval) {
        String val = attr(name);
        if (val != null) {
            val = PlaceholdersResolver.resolveJson(val, eval);
            el.removeAttribute(name);
        }
        return val;
    }

    public String takeAwayAttr(String name) {
        String val = attr(name);
        if (val != null) {
            el.removeAttribute(name);
        }
        return val;
    }

    public String takeAwayAttr(String name, String def) {
        String val = takeAwayAttr(name);
        if (val == null) {
            val = def;
        }
        return val;
    }

    public String takeAwayAttr(String attrName, String defaultValue, Evaluator eval) {
        String val = takeAwayAttr(attrName, eval);
        return val == null ? defaultValue : val;
    }

    public Element el() {
        return el;
    }

    public Html success() {
        css("bd-callout bd-callout-success");
        return this;
    }

    public Html panel(String header) {
        css("card mb-3");
        String id = String.valueOf(header.hashCode());
        Html body = div().css("card-body collapse show").attr("id", id);
        moveChildrenTo(body);
        this.childs(
                div().css("card-header").childs(
                        link(header).attr("name", header).attr("data-type", "example")
                ).collapse(id)
        );
        Html footer = div().css("card-footer text-muted").collapse(id);
        el.appendChild(body.el);
        el.appendChild(footer.el());
        return this;
    }

    public String localName() {
        return el.getLocalName();
    }

    public boolean hasChildren() {
        return el.hasChildren();
    }

    public Html moveChildrenTo(Html html) {
        el.moveChildrenTo(html.el);
        return this;
    }

    public Html moveAttributesTo(Html html) {
        el.moveAttributesTo(html.el);
        return this;
    }

    public String text() {
        return el.getText();
    }

    public Html text(String txt) {
        el.appendText(txt);
        return this;
    }

    public Html insteadOf(Element original) {
        original.moveChildrenTo(this.el);
        original.moveAttributesTo(this.el);
        original.appendSister(this.el);
        original.getParentElement().removeChild(original);
        return this;
    }

    public Html insteadOf(Html original) {
        return insteadOf(original.el);
    }

    public Html first(String tag) {
        Element first = this.el.getFirstChildElement(tag);
        return first == null ? null : new Html(first);
    }

    public Html removeAllChild() {
        this.moveChildrenTo(new Html("tmp"));
        return this;
    }

    public void remove(Html html) {
        el.removeChild(html.el);
    }

    public Html remove(Html... childs) {
        for (Html child : childs) {
            if (child != null) {
                this.remove(child);
            }
        }
        return this;
    }

    public Html deepClone() {
        return new Html(el.deepClone());
    }

    public Html parent() {
        return new Html(el.getParentElement());
    }
}