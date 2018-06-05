package com.adven.concordion.extensions.exam

import com.adven.concordion.extensions.exam.html.*
import org.concordion.api.Element
import org.concordion.api.listener.SpecificationProcessingEvent
import org.concordion.api.listener.SpecificationProcessingListener
import java.util.*

class SpecSummaryListener : SpecificationProcessingListener {

    override fun beforeProcessingSpecification(event: SpecificationProcessingEvent) {}

    override fun afterProcessingSpecification(event: SpecificationProcessingEvent) {
        val body = event.rootElement.getFirstChildElement("body")

        if (body != null) {
            val menu = body.getElementById("summary")
            if (menu != null) {
                menu.parentElement.addStyleClass("pin")
                val summary = div().css("list-group")
                for (a in body.getDescendantElements("a")) {
                    if ("example" == a.getAttributeValue("data-type")) {
                        val anchor = a.getAttributeValue("name")
                        val id = UUID.randomUUID().toString()

                        val rootExampleEl = a.parentElement.parentElement
                        val item = menuItemA(anchor).attrs("href" to "#$anchor")(
                            footerOf(rootExampleEl).firstOrThrow("small").deepClone()
                                .css("card-img-overlay m-1").style("padding:0; left:inherit;")
                        )
                        val cases = getCase(rootExampleEl, id)
                        summary(if (cases.childs().isEmpty()) {
                            listOf(item)
                        } else {
                            listOf(item(buttonCollapse("cases", id)), cases)
                        })
                    }
                }
                menu.appendChild(summary.el())
            }
        }
    }

    private fun getCase(rootExampleEl: Element, id: String): Html {
        val div = div("id" to id).css("collapse")
        for (tr in rootExampleEl.getDescendantElements("tr")) {
            if ("case" == tr.getAttributeValue("data-type")) {
                val anchor = tr.getAttributeValue("id")
                div(
                    menuItemA(
                        anchor,
                        italic("").css("fa fa-circle fa-fw")).attrs("href" to "#$anchor").muted()
                )
            }
        }
        return div
    }
}