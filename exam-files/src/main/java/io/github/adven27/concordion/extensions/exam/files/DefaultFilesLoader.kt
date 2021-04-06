package io.github.adven27.concordion.extensions.exam.files

import io.github.adven27.concordion.extensions.exam.core.html.Html
import io.github.adven27.concordion.extensions.exam.core.resolveXml
import io.github.adven27.concordion.extensions.exam.core.utils.InvalidXml
import io.github.adven27.concordion.extensions.exam.core.utils.content
import nu.xom.Builder
import nu.xom.Document
import nu.xom.ParsingException
import org.concordion.api.Evaluator
import java.io.File
import java.io.File.separator
import java.io.IOException
import java.lang.Boolean.parseBoolean
import java.util.ArrayList

open class DefaultFilesLoader : FilesLoader {

    override fun clearFolder(path: String) {
        File(path).listFiles()?.forEach {
            if (!it.delete()) {
                throw CantDeleteFile(it)
            }
        }
    }

    class CantDeleteFile(file: File) : RuntimeException("could not delete file " + file.path)

    override fun createFileWith(filePath: String, fileContent: String?) {
        File(filePath).also {
            createParentDirs(it)
            if (it.createNewFile() && !fileContent.isNullOrEmpty()) {
                it.appendText(fileContent)
            }
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
        throw InvalidXml(e)
    } catch (e: IOException) {
        throw InvalidXml(e)
    }

    override fun readFile(path: String, file: String): String = readFile(File("$path$separator$file"))

    protected fun readFile(file: File): String = try {
        file.readText()
    } catch (e: IOException) {
        "ERROR WHILE FILE READING"
    }

    override fun readFileTag(f: Html, eval: Evaluator): FilesLoader.FileTag {
        val content = getContentFor(f)
        return FilesLoader.FileTag(
            f.attr("name"),
            if (content == null) null else eval.resolveXml(content).trim(),
            parseBoolean(f.attr("autoFormat")),
            parseBoolean(f.attr("lineNumbers"))
        )
    }

    private fun getContentFor(f: Html) = if (f.attr("from") == null) {
        if (f.hasChildren()) f.text() else null
    } else {
        f.content()
    }

    companion object {
        private fun createParentDirs(file: File) {
            file.canonicalFile.parentFile?.also {
                it.mkdirs()
                if (!it.isDirectory) {
                    throw IOException("Unable to create parent directories of $file")
                }
            }
        }
    }
}
