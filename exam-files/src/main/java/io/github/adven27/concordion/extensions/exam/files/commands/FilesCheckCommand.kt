package io.github.adven27.concordion.extensions.exam.files.commands

import io.github.adven27.concordion.extensions.exam.core.ContentVerifier
import io.github.adven27.concordion.extensions.exam.core.XmlVerifier
import io.github.adven27.concordion.extensions.exam.core.html.codeXml
import io.github.adven27.concordion.extensions.exam.core.html.div
import io.github.adven27.concordion.extensions.exam.core.html.divCollapse
import io.github.adven27.concordion.extensions.exam.core.html.generateId
import io.github.adven27.concordion.extensions.exam.core.html.html
import io.github.adven27.concordion.extensions.exam.core.html.table
import io.github.adven27.concordion.extensions.exam.core.html.td
import io.github.adven27.concordion.extensions.exam.core.html.tr
import io.github.adven27.concordion.extensions.exam.core.prettyXml
import io.github.adven27.concordion.extensions.exam.core.resolveToObj
import io.github.adven27.concordion.extensions.exam.files.FilesLoader
import io.github.adven27.concordion.extensions.exam.files.FilesResultRenderer
import org.concordion.api.CommandCall
import org.concordion.api.Element
import org.concordion.api.Evaluator
import org.concordion.api.Fixture
import org.concordion.api.Result
import org.concordion.api.ResultRecorder
import org.concordion.api.listener.AssertEqualsListener
import org.concordion.api.listener.AssertFailureEvent
import org.concordion.api.listener.AssertSuccessEvent
import org.concordion.internal.util.Announcer
import org.slf4j.LoggerFactory
import java.io.File

class FilesCheckCommand(name: String?, tag: String?, filesLoader: FilesLoader) : BaseCommand(name, tag) {
    private val listeners = Announcer.to(AssertEqualsListener::class.java)
    private val filesLoader: FilesLoader
    override fun verify(
        commandCall: CommandCall,
        evaluator: Evaluator,
        resultRecorder: ResultRecorder,
        fixture: Fixture
    ) {
        val root = commandCall.html().css("table-responsive")
        val table = table()
        root.moveChildrenTo(table)
        root.invoke(table)
        val path = root.takeAwayAttr("dir", evaluator)
        if (path != null) {
            val evalPath = evaluator.evaluate(path).toString()
            val names = filesLoader.getFileNames(evalPath)
            val surplusFiles: MutableList<String> = if (names.isEmpty()) ArrayList() else ArrayList(listOf(*names))
            table.invoke(flCaption(evalPath))
            addHeader(table, HEADER, FILE_CONTENT)
            var empty = true
            for (f in table.childs()) {
                if ("file" == f.localName()) {
                    val (name1, content1, autoFormat, lineNumbers) = filesLoader.readFileTag(f, evaluator)
                    val resolvedName = resolveToObj(name1, evaluator)
                    val expectedName = resolvedName?.toString() ?: name1!!
                    val fileNameTD = td(expectedName)
                    var pre = codeXml("")
                    if (!filesLoader.fileExists(evalPath + File.separator + expectedName)) {
                        resultRecorder.record(Result.FAILURE)
                        announceFailure(fileNameTD.el(), "", null)
                    } else {
                        resultRecorder.record(Result.SUCCESS)
                        announceSuccess(fileNameTD.el())
                        surplusFiles.remove(expectedName)
                        if (content1 == null) {
                            val id = generateId()
                            val content = filesLoader.readFile(evalPath, expectedName)
                            if (!content.isEmpty()) {
                                pre = div().style("position: relative").invoke(
                                    divCollapse("", id),
                                    div("id".to(id)).css("collapse show").invoke(
                                        pre.text(content)
                                    )
                                )
                            }
                        } else {
                            checkContent(
                                evalPath + File.separator + expectedName,
                                content1,
                                resultRecorder,
                                pre.el()
                            )
                        }
                    }
                    table.invoke(
                        tr().invoke(
                            fileNameTD,
                            td().invoke(
                                pre.attrs(
                                    "autoFormat".to(autoFormat.toString()),
                                    "lineNumbers".to(lineNumbers.toString())
                                )
                            )
                        )
                    ).remove(f)
                    empty = false
                }
            }
            for (file in surplusFiles) {
                resultRecorder.record(Result.FAILURE)
                val td = td()
                val tr = tr().invoke(
                    td,
                    td().invoke(
                        codeXml(filesLoader.readFile(evalPath, file))
                    )
                )
                table.invoke(tr)
                announceFailure(td.el(), null, file)
            }
            if (empty) {
                addRow(table, EMPTY, "")
            }
        }
    }

    private fun checkContent(path: String, expected: String?, resultRecorder: ResultRecorder, element: Element) {
        if (!filesLoader.fileExists(path)) {
            xmlDoesNotEqual(resultRecorder, element, "(not set)", expected)
            return
        }
        val prettyActual = filesLoader.documentFrom(path).prettyXml()
        try {
            XmlVerifier().verify(expected!!, prettyActual)
                .onFailure { f ->
                    when (f) {
                        is ContentVerifier.Fail -> xmlDoesNotEqual(resultRecorder, element, f.actual, f.expected)
                        else -> throw f
                    }
                }
                .onSuccess {
                    element.appendText(prettyActual)
                    xmlEquals(resultRecorder, element)
                }
        } catch (e: Exception) {
            LOG.debug("Got exception on xml checking: {}", e.message)
            xmlDoesNotEqual(resultRecorder, element, prettyActual, expected)
        }
    }

    private fun xmlEquals(resultRecorder: ResultRecorder, element: Element) {
        resultRecorder.record(Result.SUCCESS)
        announceSuccess(element)
    }

    private fun xmlDoesNotEqual(resultRecorder: ResultRecorder, element: Element, actual: String, expected: String?) {
        resultRecorder.record(Result.FAILURE)
        announceFailure(element, expected, actual)
    }

    private fun announceSuccess(element: Element) {
        listeners.announce().successReported(AssertSuccessEvent(element))
    }

    private fun announceFailure(element: Element, expected: String?, actual: Any?) {
        listeners.announce().failureReported(AssertFailureEvent(element, expected, actual))
    }

    companion object {
        private val LOG = LoggerFactory.getLogger(FilesCheckCommand::class.java)
    }

    init {
        listeners.addListener(FilesResultRenderer())
        this.filesLoader = filesLoader
    }
}
