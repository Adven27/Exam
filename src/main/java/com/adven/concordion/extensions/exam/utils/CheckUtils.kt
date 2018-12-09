package com.adven.concordion.extensions.exam.utils

import com.adven.concordion.extensions.exam.files.commands.PlaceholderSupportDiffEvaluator
import com.adven.concordion.extensions.exam.html.Html
import com.adven.concordion.extensions.exam.rest.JsonPrettyPrinter
import net.javacrumbs.jsonunit.core.Configuration
import nu.xom.Builder
import nu.xom.Document
import nu.xom.Serializer
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.xmlunit.builder.DiffBuilder
import org.xmlunit.diff.DifferenceEvaluators
import org.xmlunit.diff.NodeMatcher
import java.io.ByteArrayOutputStream
import java.io.FileNotFoundException
import java.io.StringReader

fun String.equalToXml(expected: String, nodeMatcher: NodeMatcher?, configuration: Configuration?): Boolean {
    val diff = DiffBuilder.compare(expected.trim { it <= ' ' })
            .checkForSimilar().withNodeMatcher(nodeMatcher)
            .withTest(this.trim { it <= ' ' })
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

fun String.prettyPrintXml(): String = Builder().build(StringReader(this.trim())).prettyPrintXml()

fun String.prettyPrintJson() = JsonPrettyPrinter().prettyPrint(this)

fun Document.prettyPrintXml(): String {
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

fun String.findResource() = javaClass.getResource(this) ?: throw FileNotFoundException("File not found: $this")

fun Html.content() = this.attr("from")?.findResource()?.readText() ?: this.text()

fun <T : Any> T.logger(): Logger {
    return LoggerFactory.getLogger(this.javaClass.name)
}