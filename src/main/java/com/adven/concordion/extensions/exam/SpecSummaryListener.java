package com.adven.concordion.extensions.exam;

import com.adven.concordion.extensions.exam.html.Html;
import org.concordion.api.Element;
import org.concordion.api.listener.SpecificationProcessingEvent;
import org.concordion.api.listener.SpecificationProcessingListener;

import java.util.UUID;

import static com.adven.concordion.extensions.exam.html.HtmlBuilder.*;

public class SpecSummaryListener implements SpecificationProcessingListener {

    @Override
    public void beforeProcessingSpecification(SpecificationProcessingEvent event) {
    }

    @Override
    public void afterProcessingSpecification(SpecificationProcessingEvent event) {
        Element body = event.getRootElement().getFirstChildElement("body");

        if (body != null) {
            Element menu = body.getElementById("summary");
            if (menu != null) {
                menu.getParentElement().addStyleClass("pin");
                Html summary = div().css("list-group");
                for (Element a : body.getDescendantElements("a")) {
                    if ("example".equals(a.getAttributeValue("data-type"))) {
                        String anchor = a.getAttributeValue("name");
                        String id = UUID.randomUUID().toString();

                        Element rootExampleEl = a.getParentElement().getParentElement();
                        Html item = menuItemA(anchor).attr("href", "#" + anchor).childs(
                            footerOf(rootExampleEl).firstOrThrow("small").deepClone()
                                .css("card-img-overlay m-1").style("padding:0; left:inherit;")
                        );
                        Html cases = getCase(rootExampleEl, id);
                        if (cases == null || cases.childs().isEmpty()) {
                            summary.childs(item);
                        } else {
                            summary.childs(item.childs(buttonCollapse("cases", id)), cases);
                        }
                    }
                }
                menu.appendChild(summary.el());
            }
        }
    }

    private Html getCase(Element rootExampleEl, String id) {
        Html div = div().css("collapse").attr("id", id);
        for (Element tr : rootExampleEl.getDescendantElements("tr")) {
            if ("case".equals(tr.getAttributeValue("data-type"))) {
                String anchor = tr.getAttributeValue("id");
                div.childs(
                    menuItemA(anchor, italic("").css("fa fa-circle fa-fw")).attr("href", "#" + anchor).muted()
                );
            }
        }
        return div;
    }
}