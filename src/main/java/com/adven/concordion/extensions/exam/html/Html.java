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
        return new Html(new Element("div"));
    }

    public static Html div(String txt) {
        return new Html(new Element("div")).text(txt);
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
        return new Html(new Element("th")).text(txt);
    }

    public static Html tbody() {
        return new Html(new Element("tbody"));
    }

    public static Html tr() {
        return new Html(new Element("tr"));
    }

    public static Html td() {
        return new Html(new Element("td"));
    }

    public static Html td(String txt) {
        return td().text(txt);
    }

    public static Html italic(String txt) {
        return new Html(new Element("i")).text(txt);
    }

    public static Html code(String txt) {
        return new Html(new Element("code")).text(txt);
    }

    public static Html span(String txt) {
        return new Html(new Element("span")).text(txt);
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
        return new Html(new Element("var")).text(txt);
    }

    public static Html link(String txt) {
        return new Html(new Element("a")).text(txt);
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

    public static Html imageOverlay(String src, int size, String title, String txt) {
        return div().css("card bg-light").childs(
                link("", src).childs(
                        image(src, size, size)
                ),
                div().css("card-img-top").childs(
                        h(4, title),
                        paragraph(txt).css("card-text")

                )
        );
    }

    public static Html image(String src) {
        return new Html(new Element("image")).attr("src", src);
    }

    public static Html image(String src, int width, int height) {
        return image(src).css("img-thumbnail").
                attr("width", String.valueOf(width)).
                attr("height", String.valueOf(height));
    }

    public static Html h(int n, String text) {
        return new Html(new Element("h" + n)).text(text);
    }

    public static Html caption(String txt) {
        return new Html(new Element("caption")).text(txt);
    }

    public static Html pre() {
        return new Html(new Element("pre"));
    }

    public static Html pre(String txt) {
        return pre().text(txt);
    }

    public static Html paragraph(String txt) {
        return new Html(new Element("p")).text(txt);
    }

    public static Html codeXml(String text) {
        return pre(text).css("xml card");
    }

    public static Html tag(String tag) {
        return new Html(tag);
    }

    public static Html ul() {
        return new Html(new Element("ul"));
    }

    public static Html list() {
        return new Html(new Element("ul")).css("list-group");
    }

    public static Html li(String text) {
        return li().text(text);
    }

    public static Html li() {
        return new Html(new Element("li"));
    }

    public static Html menuItem() {
        return li().css("list-group-item list-group-item-action d-flex justify-content-between align-items-center");
    }

    public static Html button(String txt) {
        return new Html(new Element("button")).
                text(txt).css("btn btn-light btn-sm text-muted ml-1").attr("type", "button");
    }

    public static Html buttonCollapse(String txt, String target) {
        return button(txt).
                attr("data-toggle", "collapse").
                attr("data-target", "#" + target).
                attr("aria-expanded", "true").
                attr("aria-controls", target);
    }

    public static Html footerOf(Element card) {
        return new Html(card.getChildElements("div")[2]);
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

    public Html attr(String attr, String val) {
        el.addAttribute(attr, val);
        return this;
    }

    public String attr(String name) {
        return el.getAttributeValue(name);
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
            val = PlaceholdersResolver.resolve(val, eval);
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
                        link(header).attr("data-type", "example").attr("name", header),
                        buttonCollapse("collapse", id)
                )
        );
        Html footer = div().css("card-footer text-muted").attr("data-toggle", "collapse").
                attr("data-target", "#" + id).
                attr("aria-expanded", "true").
                attr("aria-controls", id);
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
        this.moveChildrenTo(new Html(new Element("tmp")));
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

    public static Html stat() {
        return new Html("small");
    }
}