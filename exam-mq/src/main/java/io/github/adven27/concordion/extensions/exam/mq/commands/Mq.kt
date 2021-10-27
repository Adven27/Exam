package io.github.adven27.concordion.extensions.exam.mq.commands

import io.github.adven27.concordion.extensions.exam.core.readFile
import org.concordion.api.Element
import org.concordion.api.Evaluator

fun parsePayload(link: Element, eval: Evaluator): String {
    link.childElements.filter { it.localName == "code" }.forEach {
        parseOption(it).also { (name, value) -> eval.setVariable("#$name", value) }
    }
    return link.getAttributeValue("href").readFile(eval)
}

fun parseOption(el: Element) = el.text.split("=", limit = 2).let { it[0] to it[1] }
