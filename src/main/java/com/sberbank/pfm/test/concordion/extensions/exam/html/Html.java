package com.sberbank.pfm.test.concordion.extensions.exam.html;

import com.sberbank.pfm.test.concordion.extensions.exam.PlaceholdersResolver;
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
        return new Html(new Element("div").appendText(txt));
    }


    public static Html td() {
        return new Html(new Element("td"));
    }

    public static Html td(String txt) {
        return new Html(new Element("td").appendText(txt));
    }


    public static Html tr() {
        return new Html(new Element("tr"));
    }


    public static Html italic(String txt) {
        return new Html(new Element("i").appendText(txt));
    }

    public static Html code(String txt) {
        return new Html(new Element("code").appendText(txt));
    }

    public static Html span(String txt) {
        return new Html(new Element("span").appendText(txt));
    }

    public static Html table() {
        return new Html(new Element("table")).style("table table-condensed");
    }

    public static Html th(String txt) {
        return new Html(new Element("th").appendText(txt));
    }

    public static Html link(String txt) {
        return new Html(new Element("a").appendText(txt));
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

    public static Html image(String src) {
        return new Html(new Element("image").addAttribute("src", src));
    }

    public static Html image(String src, int width, int height) {
        return image(src).style("img-thumbnail").
                attr("width", String.valueOf(width)).
                attr("height", String.valueOf(height));
    }

    public static Html h4(String title) {
        return new Html(new Element("h4").appendText(title));
    }

    public static Html caption(String txt) {
        return new Html(new Element("caption").appendText(txt));
    }

    public static Html pre() {
        return new Html(new Element("pre"));
    }

    public static Html tag(String tag) {
        return new Html(tag);
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

    public Html style(String style) {
        el.addStyleClass(style);
        return this;
    }

    public Html muted() {
        el.addStyleClass("text-muted");
        return this;
    }

    public Html dropAllTo(Html element) {
        el.moveChildrenTo(element.el);
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

    public String takeAwayAttr(String attrName, String defaultValue, Evaluator eval) {
        String val = takeAwayAttr(attrName, eval);
        return val == null ? defaultValue : val;
    }

    public Element el() {
        return el;
    }

    public Html success() {
        el.addStyleClass("bs-callout bs-callout-success");
        return this;
    }

    public Html panel(String header) {
        el.addStyleClass("panel panel-info table-responsive");
        Html body = div().style("panel-body");
        el.moveChildrenTo(body.el);
        this.childs(
                div().style("panel-heading").childs(
                        link(header).attr("data-type", "example").attr("name", header)
                )
        );
        el.appendChild(body.el);
        return this;
    }

    public void remove(Html html) {
        el.removeChild(html.el);
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

    public Html removeChildren() {
        this.moveChildrenTo(new Html(new Element("tmp")));
        return this;
    }

    public Html removeChilds(Html... childs) {
        for (Html child : childs) {
            if (child != null) {
                this.remove(child);
            }
        }
        return this;
    }
}
