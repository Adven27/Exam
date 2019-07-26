package com.adven.concordion.extensions.exam.core.utils

import com.adven.concordion.extensions.exam.core.ExamExtension
import com.adven.concordion.extensions.exam.core.html.Html
import com.adven.concordion.extensions.exam.core.resolveJson
import com.adven.concordion.extensions.exam.files.commands.PlaceholderSupportDiffEvaluator
import com.adven.concordion.extensions.exam.ws.JsonPrettyPrinter
import net.javacrumbs.jsonunit.core.Configuration
import nu.xom.Builder
import nu.xom.Document
import nu.xom.Serializer
import org.concordion.api.Evaluator
import org.xmlunit.builder.DiffBuilder
import org.xmlunit.diff.DifferenceEvaluators
import org.xmlunit.diff.NodeMatcher
import java.io.ByteArrayOutputStream
import java.io.FileNotFoundException
import java.io.StringReader

fun String.equalToXml(expected: String, nodeMatcher: NodeMatcher?, configuration: Configuration?): Boolean {
    val diff = DiffBuilder.compare(expected.trim())
        .checkForSimilar().withNodeMatcher(nodeMatcher)
        .withTest(this.trim())
        .withDifferenceEvaluator(
            DifferenceEvaluators.chain(
                DifferenceEvaluators.Default,
                PlaceholderSupportDiffEvaluator(configuration)
            )
        )
        .ignoreComments().ignoreWhitespace().build()

    //FIXME Reports are visible only on logs, show them in spec too
    if (diff.hasDifferences()) {
        throw RuntimeException(diff.toString())
    }
    return true
}

fun String.prettyXml(): String = Builder().build(StringReader(this.trim())).prettyXml()

fun String.prettyJson() = JsonPrettyPrinter().prettyPrint(this)

fun Document.prettyXml(): String {
    try {
        val out = ByteArrayOutputStream()
        val serializer = Serializer(out, "UTF-8")
        serializer.indent = 4
        serializer.write(this)
        return out.toString("UTF-8")
    } catch (e: Exception) {
        throw RuntimeException("invalid xml", e)
    }
}

fun Html.content(eval: Evaluator) = this.attr("from")?.findResource(eval)?.readText() ?: this.text()
fun String.findResource(eval: Evaluator) = ExamExtension::class.java.getResource(eval.resolveJson(this))
    ?: throw FileNotFoundException("File not found: $this")

fun Html.content() = this.attr("from")?.findResource()?.readText() ?: this.text()
fun String.readFile() = this.findResource().readText()
fun String.findResource() = ExamExtension::class.java.getResource(this)
    ?: throw FileNotFoundException("File not found: $this")