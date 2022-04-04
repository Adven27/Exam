@file:JvmName("PlaceholdersResolver")

package io.github.adven27.concordion.extensions.exam.core

import io.github.adven27.concordion.extensions.exam.core.handlebars.HANDLEBARS
import io.github.adven27.concordion.extensions.exam.core.handlebars.matchers.PLACEHOLDER_TYPE
import io.github.adven27.concordion.extensions.exam.core.handlebars.resolveObj
import org.concordion.api.Evaluator

fun Evaluator.resolveForContentType(body: String, type: String): String =
    if (type.contains("xml", true)) resolveXml(body) else resolveJson(body)

fun Evaluator.resolveNoType(body: String): String = resolveTxt(body, "text", this)
fun Evaluator.resolveJson(body: String): String = resolveTxt(body, "json", this)
fun Evaluator.resolveXml(body: String): String = resolveTxt(body, "xml", this)

private fun resolveTxt(body: String, type: String, eval: Evaluator): String =
    eval.apply { setVariable("#$PLACEHOLDER_TYPE", type) }.resolveToObj(body).toString()

fun Evaluator.resolveToObj(placeholder: String?): Any? = HANDLEBARS.resolveObj(this, placeholder)

fun String?.vars(eval: Evaluator, setVar: Boolean = false, separator: String = ","): Map<String, Any?> =
    pairs(separator)
        .mapValues { (k, v) -> k to eval.resolveToObj(v).apply { if (setVar) eval.setVariable("#$k", this) } }

fun String?.headers(separator: String = ","): Map<String, String> =
    pairs(separator)

private fun String?.pairs(separator: String) = this?.split(separator)
    ?.map { it.split('=', limit = 2) }
    ?.associate { (k, v) -> k.trim() to v.trim() }
    ?: emptyMap()
