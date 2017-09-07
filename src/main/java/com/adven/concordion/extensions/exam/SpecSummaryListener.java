package com.adven.concordion.extensions.exam;

import com.adven.concordion.extensions.exam.html.Html;
import org.concordion.api.Element;
import org.concordion.api.listener.SpecificationProcessingEvent;
import org.concordion.api.listener.SpecificationProcessingListener;

import static com.adven.concordion.extensions.exam.html.Html.*;

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
                Html summary = ul().css("list-group");
                for (Element a : body.getDescendantElements("a")) {
                    if ("example".equals(a.getAttributeValue("data-type"))) {
                        String anchor = a.getAttributeValue("name");
                        a.addAttribute("href", "#summary");

                        summary.childs(
                                menuItem().childs(
                                        link(anchor).attr("href", "#" + anchor),
                                        footerOf(a.getParentElement().getParentElement()).first("small").deepClone().
                                                css("card-img-overlay m-1").style("padding:0; left:inherit;")
                                )
                        );
                    }
                }
                menu.appendChild(summary.el());
            }
        }
    }
}