package io.github.adven27.concordion.extensions.exam.core.logger

import org.slf4j.MDC
import java.io.File
import java.util.Stack

interface LoggingAdaptor {
    fun startLogFile(testPath: String?)
    fun startSpecificationLogFile(resourcePath: String)
    fun startExampleLogFile(resourcePath: String, exampleName: String)
    fun stopLogFile()
    fun logFileExists(): Boolean
    val logFile: File
}

@Suppress("MagicNumber")
class LogbackAdaptor : LoggingAdaptor {
    override fun startLogFile(testPath: String?) {
        testStack.push(testPath)
        MDC.put("testname", testPath)
    }

    override fun startSpecificationLogFile(resourcePath: String) {
        val path = baseFolder + getPath(resourcePath)
        testStack.push(path)
        MDC.put("testname", path)
    }

    override fun startExampleLogFile(resourcePath: String, exampleName: String) {
        val path = baseFolder + getPath(resourcePath) + "[" + shortenFileName(exampleName, 40) + "]"
        testStack.push(path)
        MDC.put("testname", path)
    }

    override fun stopLogFile() {
        testStack.pop()
        if (testStack.isEmpty()) {
            MDC.remove("testname")
        } else {
            MDC.put("testname", testStack.peek())
        }
    }

    override fun logFileExists() = logFile.exists()

    override val logFile: File
        get() {
            val currentTest = MDC.get("testname")
            return if (currentTest != null && currentTest.isNotEmpty()) {
                var logFile = File(currentTest + "Log.html")
                if (logFile.exists()) {
                    logFile
                } else {
                    logFile = File("$currentTest.log")
                    if (logFile.exists()) logFile else File("")
                }
            } else {
                File("")
            }
        }

    private fun getPath(path: String): String {
        var resourcePath = path
        if (resourcePath.lastIndexOf(".") > 0) {
            resourcePath = resourcePath.substring(0, resourcePath.lastIndexOf("."))
        }
        if (resourcePath.startsWith("/") || resourcePath.startsWith("\\")) {
            resourcePath = resourcePath.substring(1)
        }
        var pos = resourcePath.lastIndexOf("/") + 1
        val pos2 = resourcePath.lastIndexOf("\\") + 1
        if (pos2 > pos) {
            pos = pos2
        }
        return resourcePath.substring(0, pos) + shortenFileName(resourcePath.substring(pos), 60)
    }

    private fun shortenFileName(fileName: String, maxLength: Int): String {
        return if (fileName.length <= maxLength) {
            fileName
        } else {
            var sb = StringBuilder()
            var addNextChar = false
            var index = fileName.length - 1
            while (index > 0) {
                val chr = fileName[index]
                if (addNextChar) {
                    sb.append(fileName[index].toString().uppercase())
                    addNextChar = false
                }
                if (chr == ' ' || chr == '-') {
                    addNextChar = true
                }
                if (index + sb.length <= maxLength) {
                    break
                }
                --index
            }
            sb = sb.reverse()
            if (index > 0) {
                sb.insert(0, fileName.substring(0, index))
            }
            sb.toString()
        }
    }

    companion object {
        private val testStack: Stack<String?> = Stack()
        private val baseFolder = concordionBaseOutputDir

        private val concordionBaseOutputDir: String
            get() {
                var outputPath = System.getProperty("concordion.output.dir")
                if (outputPath == null) {
                    outputPath = File(System.getProperty("java.io.tmpdir"), "concordion").absolutePath
                }
                outputPath = outputPath!!.replace("\\\\".toRegex(), "/")
                if (!outputPath.endsWith("/")) {
                    outputPath = "$outputPath/"
                }
                return outputPath
            }
    }
}
