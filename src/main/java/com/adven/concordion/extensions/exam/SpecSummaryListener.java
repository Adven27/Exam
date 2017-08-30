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
                Html summary = ul();
                for (Element a : body.getDescendantElements("a")) {
                    if ("example".equals(a.getAttributeValue("data-type"))) {
                        String anchor = a.getAttributeValue("name");
                        a.addAttribute("href", "#summary");

                        Element badge = a.getFirstChildElement("span");
                        a.removeChild(badge);

                        summary.childs(
                                li().childs(
                                        link(anchor).attr("href", "#" + anchor),
                                        new Html(badge)
                                )
                        );

                        Element p = a.getParentElement().getParentElement().getFirstChildElement("p");
                        if (p != null) {
                            //p.addStyleClass("alert alert-warning");
                            //p.addAttribute("role", "alert");
                            p.getParentElement().removeChild(p);
                            //a.appendChild(p);
                        }
                    }
                }
                menu.appendChild(summary.el());
            }
        }
    }
}