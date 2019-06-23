package com.adven.concordion.extensions.exam.files

import com.adven.concordion.extensions.exam.core.html.Html
import nu.xom.Document
import org.concordion.api.Evaluator

interface FilesLoader {

    fun clearFolder(path: String)

    fun createFileWith(filePath: String, fileContent: String?)

    fun getFileNames(path: String): Array<String>

    fun fileExists(filePath: String): Boolean

    fun documentFrom(path: String): Document

    fun readFile(path: String, file: String): String

    fun readFileTag(f: Html, eval: Evaluator): FileTag

    data class FileTag
    @JvmOverloads constructor(
        var name: String? = null,
        var content: String? = null,
        var autoFormat: Boolean = false,
        var lineNumbers: Boolean = false
    )
}