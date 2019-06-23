package com.adven.concordion.extensions.exam.files

import com.adven.concordion.extensions.exam.core.html.Html
import com.adven.concordion.extensions.exam.core.resolveXml
import com.google.common.base.Strings.isNullOrEmpty
import com.google.common.io.Files
import com.google.common.io.Resources.getResource
import nu.xom.Builder
import nu.xom.Document
import nu.xom.ParsingException
import org.concordion.api.Evaluator
import java.io.File
import java.io.File.separator
import java.io.IOException
import java.lang.Boolean.parseBoolean
import java.nio.charset.Charset
import java.util.*

class DefaultFilesLoader : FilesLoader {

    override fun clearFolder(path: String) {
        File(path).listFiles()?.forEach { file ->
            if (!file.delete()) {
                throw RuntimeException("could not delete file " + file.path)
            }
        }
    }

    override fun createFileWith(filePath: String, fileContent: String?) {
        try {
            val to = File(filePath)
            Files.createParentDirs(to)
            if (to.createNewFile() && !isNullOrEmpty(fileContent)) {
                Files.append(fileContent, to, CHARSET)
            }
        } catch (e: IOException) {
            throw RuntimeException(e)
        }

    }

    override fun getFileNames(path: String): Array<String> = getFileNamesForDir(File(path), "").toTypedArray()

    private fun getFileNamesForDir(dir: File, s: String): List<String> {
        val fileNames = ArrayList<String>()
        val files = dir.listFiles() ?: return fileNames
        files.forEach { file ->
            if (file.isFile) {
                val path = if ("" == s) "" else s + separator
                fileNames.add(path + file.name)
            }
            if (file.isDirectory) {
                val path = if ("" == s) file.name else s + separator + file.name
                fileNames.addAll(getFileNamesForDir(file, path))
            }
        }
        return fileNames
    }

    override fun fileExists(filePath: String): Boolean = File(filePath).exists()

    override fun documentFrom(path: String): Document = try {
        Builder().build(File(path))
    } catch (e: ParsingException) {
        throw RuntimeException("invalid xml", e)
    } catch (e: IOException) {
        throw RuntimeException("invalid xml", e)
    }

    override fun readFile(path: String, file: String): String = readFile(File("$path$separator$file"))

    protected fun readFile(file: File): String = try {
        file.readText(CHARSET)
    } catch (e: IOException) {
        "ERROR WHILE FILE READING"
    }

    override fun readFileTag(f: Html, eval: Evaluator): FilesLoader.FileTag {
        val content = getContentFor(f)
        return FilesLoader.FileTag(
            f.attr("name"),
            if (content == null) null else resolveXml(content, eval).trim(),
            parseBoolean(f.attr("autoFormat")),
            parseBoolean(f.attr("lineNumbers"))
        )
    }

    private fun getContentFor(f: Html): String? {
        val from = f.attr("from")
        return if (from == null)
            if (f.hasChildren()) f.text() else null
        else
            readFile(File(getResource(from).file))
    }

    companion object {
        private val CHARSET = Charset.forName("UTF-8")
    }
}