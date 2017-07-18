package com.sberbank.pfm.test.concordion.extensions.exam.html;

import org.concordion.api.Element;

import java.util.ArrayList;
import java.util.List;

public class Html {
    final Element el;

    public Html(Element el) {
        this.el = el;
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


    public static Html i(String txt) {
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

    private static Html a(String txt) {
        return new Html(new Element("a").appendText(txt));
    }

    public Html childs(Html... htmls) {
        for (Html html : htmls) {
            el.appendChild(html.el);
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
        el.appendSister(element.el);
        return this;
    }

    public Html below(Html html) {
        el.appendSister(html.el);
        return this;
    }

    public String takeAwayAttr(String name) {
        String val = attr(name);
        if (val != null) {
            el.removeAttribute(name);
        }
        return val;
    }

    public Element el() {
        return el;
    }

    public Html success() {
        el.addStyleClass("bs-callout bs-callout-success");
        return this;
    }

    public String takeAwayAttr(String attrName, String defaultValue) {
        String val = takeAwayAttr(attrName);
        return val == null ? defaultValue : val;
    }

    public Html panel(String header) {
        el.addStyleClass("panel panel-info table-responsive");
        Html body = div().style("panel-body");
        el.moveChildrenTo(body.el);
        this.childs(div().style("panel-heading").childs(
                Html.a(header).attr("data-type", "example").attr("name", header))
        );
        el.appendChild(body.el);
        return this;
    }


    public static Html h4(String title) {
        return new Html(new Element("h4").appendText(title));
    }

    public static Html caption(String txt) {
        return new Html(new Element("caption").appendText(txt));
    }

    public String text() {
        return el.getText();
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

    public static Html pre() {
        return new Html(new Element("pre"));
    }

    public Html text(String txt) {
        el.appendText(txt);
        return this;
    }
}
