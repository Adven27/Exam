package io.github.adven27.concordion.extensions.exam.core.logger

import io.github.adven27.concordion.extensions.exam.core.html.Html
import io.github.adven27.concordion.extensions.exam.core.html.div
import io.github.adven27.concordion.extensions.exam.core.html.link
import io.github.adven27.concordion.extensions.exam.core.readFile
import org.concordion.api.Element
import org.concordion.api.extension.ConcordionExtender
import org.concordion.api.extension.ConcordionExtension
import org.concordion.api.listener.ExampleEvent
import org.concordion.api.listener.ExampleListener
import org.concordion.api.listener.SpecificationProcessingEvent
import org.concordion.api.listener.SpecificationProcessingListener
import java.io.BufferedReader
import java.io.File
import java.io.FileReader
import java.io.IOException
import java.util.regex.Matcher.quoteReplacement

@Suppress("ComplexMethod", "NestedBlockDepth")
class LoggingFormatterExtension @JvmOverloads constructor(loggingAdaptor: LoggingAdaptor = LogbackAdaptor()) :
    ConcordionExtension {
    private val listener = LoggingFormatterListener(loggingAdaptor)

    override fun addTo(extender: ConcordionExtender) {
        extender.withSpecificationProcessingListener(listener)
        extender.withExampleListener(listener)
    }

    class LoggingFormatterListener(
        private val loggingAdaptor: LoggingAdaptor
    ) : SpecificationProcessingListener, ExampleListener {
        private var testPath = ""

        override fun beforeProcessingSpecification(event: SpecificationProcessingEvent) {
            testPath = event.resource.path
            loggingAdaptor.startSpecificationLogFile(testPath)
        }

        override fun afterProcessingSpecification(event: SpecificationProcessingEvent) {
            try {
                val logFile: File = loggingAdaptor.logFile
                if (logFile.exists()) {
                    appendLogFileLinkToFooter(event, logFile)
                }
            } finally {
                loggingAdaptor.stopLogFile()
            }
        }

        private fun appendLogFileLinkToFooter(event: SpecificationProcessingEvent, logFile: File) {
            val logURL = createViewer(logFile)
            val body = event.rootElement.getFirstChildElement("body")
            if (body != null) {
                for (div in Html(body).childs("div")) {
                    if ("footer" == div.attr("class")) {
                        div(
                            div("class" to "testTime")(
                                link("Log File", logURL).style("font-weight: bold; text-decoration: none; color: #89C;")
                            )
                        )
                        break
                    }
                }
            }
        }

        private fun createViewer(logFile: File): String {
            var logName = logFile.name
            if (logName.lowercase().endsWith(".html")) {
                return logName
            }
            val i = logName.lastIndexOf('.')
            if (i > 0) {
                logName = logName.substring(0, i)
            }
            logName += "LogViewer.html"
            try {
                File(logFile.parent, logName).writeText(
                    "/LogViewer.html".readFile()
                        .replace("LOG_FILE_NAME", logFile.name)
                        .replace("LOG_FILE_CONTENT", quoteReplacement(logContent(logFile)))
                )
            } catch (e: IOException) {
                logName = logFile.name
            }
            return logName
        }

        private fun logContent(logFile: File): String {
            val logContent = StringBuilder()
            var br: BufferedReader?
            try {
                var line: String?
                var prevline: String? = null
                var lineLevel = ""
                var prevLineLevel = ""
                var lineNumber = 0
                br = BufferedReader(FileReader(logFile))
                while (br.readLine().also { line = it } != null) {
                    lineNumber++
                    line = line!!.replace("<", "&lt;")
                    line = line!!.replace(">", "&gt;")

                    // starts with a date
                    if (line!!.matches("^.*[0-9 -.:].*".toRegex())) {
                        lineLevel = when {
                            line!!.contains("INFO ") -> "info"
                            line!!.contains("DEBUG ") -> "debug"
                            line!!.contains("TRACE ") -> "trace"
                            line!!.contains("WARN ") -> "warn"
                            line!!.contains("ERROR ") -> "error"
                            else -> "unknown"
                        }
                        if (prevLineLevel !== lineLevel && (lineLevel === "debug" || lineLevel === "trace")) {
                            if (prevline != null) {
                                prevline = prevline.replace(
                                    "<li class=\"line ",
                                    "<li class=\"line split-$lineLevel-levels "
                                )
                            }
                        }
                        prevLineLevel = lineLevel
                    }
                    if (prevline != null) {
                        logContent.append(prevline).append("\n")
                    }
                    prevline =
                        "<li class=\"line $lineLevel $lineLevel-color\"><div class=\"line-numbers\">$lineNumber</div><pre>$line</pre></li>"
                }
                if (prevline != null) logContent.append(prevline).append("\n")
                br.close()
            } catch (expected: Exception) {
                logContent.append(expected.message)
            }
            return logContent.toString()
        }

        override fun beforeExample(event: ExampleEvent) =
            loggingAdaptor.startExampleLogFile(testPath, event.exampleName)

        override fun afterExample(event: ExampleEvent) {
            try {
                loggingAdaptor.logFile.let {
                    if (it.exists()) appendLogFileLinkToExample(event, it)
                }
            } finally {
                loggingAdaptor.stopLogFile()
            }
        }

        private fun appendLogFileLinkToExample(event: ExampleEvent, log: File) {
            val logURL = createViewer(log)
            val anchor = Element("a")
            anchor.addAttribute(
                "style",
                "font-size: 9pt; font-weight: bold; float: right; display: inline-block; margin-top: 20px; text-decoration: none; color: #89C;"
            )
            anchor.addAttribute("href", logURL)
            anchor.appendText("Log File")
            event.element.prependChild(anchor)
        }
    }
}
