package com.sberbank.pfm.test.concordion.extensions.exam;

import org.concordion.api.Element;
import org.concordion.api.listener.SpecificationProcessingEvent;
import org.concordion.api.listener.SpecificationProcessingListener;

public class SpecSummaryListener implements SpecificationProcessingListener {

    @Override
    public void beforeProcessingSpecification(SpecificationProcessingEvent event) {
    }

    @Override
    public void afterProcessingSpecification(SpecificationProcessingEvent event) {
        Element body = event.getRootElement().getFirstChildElement("body");

        if (body != null) {
            Element summary = new Element("div");
            for (Element a : body.getDescendantElements("a")) {
                if ("example".equals(a.getAttributeValue("data-type"))) {
                    String anchor = a.getAttributeValue("name");
                    a.addAttribute("href", "#summary");
                    Element example = new Element("a").addAttribute("href", "#" + anchor);
                    example.appendText(anchor);
                    Element div = new Element("div");
                    div.appendChild(example);
                    summary.appendChild(div);
                }
            }
            Element menu = body.getElementById("summary");
            if (menu != null) {
                menu.appendChild(new Element("a").addAttribute("name", "summary"));
                menu.appendChild(summary);
            }
        }
    }
}