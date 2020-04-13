package com.adven.concordion.extensions.exam.ws

import com.adven.concordion.extensions.exam.core.commands.ExamCommand
import com.adven.concordion.extensions.exam.core.commands.ExamVerifyCommand
import com.adven.concordion.extensions.exam.core.html.*
import com.adven.concordion.extensions.exam.core.resolveJson
import com.adven.concordion.extensions.exam.core.resolveXml
import com.adven.concordion.extensions.exam.core.utils.*
import com.adven.concordion.extensions.exam.ws.RequestExecutor.Companion.fromEvaluator
import io.restassured.http.Method
import net.javacrumbs.jsonunit.JsonAssert.assertJsonEquals
import net.javacrumbs.jsonunit.core.Configuration
import net.javacrumbs.jsonunit.core.Option
import net.javacrumbs.jsonunit.core.internal.Options
import org.concordion.api.CommandCall
import org.concordion.api.Evaluator
import org.concordion.api.ResultRecorder
import org.concordion.internal.util.Check
import org.xmlunit.diff.NodeMatcher
import java.nio.charset.Charset
import java.util.*

private const val HEADERS = "headers"
private const val TYPE = "contentType"
private const val URL = "url"
private const val DESC = "desc"
private const val URL_PARAMS = "urlParams"
private const val COOKIES = "cookies"
private const val VARIABLES = "vars"
private const val VALUES = "vals"
private const val BODY = "body"
private const val MULTI_PART = "multiPart"
private const val PART = "part"
private const val PART_NAME = "name"
private const val FILE_NAME = "fileName"
private const val EXPECTED = "expected"
private const val WHERE = "where"
private const val CASE = "case"
private const val IGNORED_PATHS = "ignoredPaths"
private const val JSON_UNIT_OPTIONS = "jsonUnitOptions"
private const val PROTOCOL = "protocol"
private const val STATUS_CODE = "statusCode"
private const val REASON_PHRASE = "reasonPhrase"
private const val FROM = "from"

class PutCommand(name: String, tag: String) : RequestCommand(name, tag, Method.PUT)
class GetCommand(name: String, tag: String) : RequestCommand(name, tag, Method.GET)
class PostCommand(name: String, tag: String) : RequestCommand(name, tag, Method.POST)
class DeleteCommand(name: String, tag: String) : RequestCommand(name, tag, Method.DELETE)
class SoapCommand(name: String, tag: String) :
    RequestCommand(name, tag, Method.POST, "application/soap+xml; charset=UTF-8;")

sealed class RequestCommand(
    name: String,
    tag: String,
    private val method: Method,
    private val contentType: String = "application/json"
) : ExamCommand(name, tag) {

    override fun setUp(commandCall: CommandCall?, evaluator: Evaluator?, resultRecorder: ResultRecorder?) {
        val executor = RequestExecutor.newExecutor(evaluator!!).method(method)
        val root = Html(commandCall!!.element).success()

        val url = attr(root, URL, "/", evaluator)
        val type = attr(root, TYPE, contentType, evaluator)
        val cookies = cookies(evaluator, root)
        val headersMap = headers(root, evaluator)

        addRequestDescTo(root, url, type, cookies)
        startTable(root, executor.hasRequestBody())

        executor.type(type).url(url).header(headersMap).cookies(cookies)
    }

    private fun startTable(html: Html, hasRequestBody: Boolean) {
        val table = table()
        val header = thead()
        val tr = thead()
        if (hasRequestBody) {
            tr(
                th("Request")
            )
        }
        tr(
            th("Expected response"),
            th("Status code")
        )
        table(header(tr))
        html.dropAllTo(table)
    }

    private fun headers(html: Html, eval: Evaluator?): Map<String, String> {
        val headers = html.takeAwayAttr(HEADERS, eval)
        val headersMap = HashMap<String, String>()
        if (headers != null) {
            val headersArray = headers.split(",").dropLastWhile { it.isEmpty() }.toTypedArray()
            for (i in headersArray.indices) {
                if ((i - 1) % 2 == 0) {
                    headersMap[headersArray[i - 1]] = headersArray[i]
                }
            }
        }
        return headersMap
    }

    private fun cookies(eval: Evaluator?, html: Html): String? {
        val cookies = html.takeAwayAttr(COOKIES, eval)
        eval!!.setVariable("#cookies", cookies)
        return cookies
    }

    private fun addRequestDescTo(root: Html, url: String, type: String, cookies: String?) {
        val div = div()(
            h(4, "")(
                badge(method.name, "success"),
                badge(type, "info"),
                code(url)
            )
        )
        if (cookies != null) {
            div(
                h(6, "")(
                    badge("Cookies", "info"),
                    code(cookies)
                )
            )
        }
        root(div)
    }

    private fun attr(html: Html, attrName: String, defaultValue: String, evaluator: Evaluator?): String {
        val attr = html.takeAwayAttr(attrName, defaultValue, evaluator!!)
        evaluator.setVariable("#$attrName", attr)
        return attr
    }
}

open class RestVerifyCommand(name: String, tag: String) : ExamVerifyCommand(name, tag, RestResultRenderer())

class ExpectedStatusCommand(name: String, tag: String) : RestVerifyCommand(name, tag) {
    override fun verify(cmd: CommandCall?, evaluator: Evaluator?, resultRecorder: ResultRecorder) {
        Check.isFalse(
            cmd!!.hasChildCommands(),
            "Nesting commands inside an 'expectedStatus' is not supported"
        )

        val element = cmd.html()
        val expected = element.text()
        val actual = evaluator!!.evaluate(cmd.expression).toString()

        if (expected == actual) {
            success(resultRecorder, element)
        } else {
            failure(resultRecorder, element, actual, expected)
        }
    }
}

class CaseCheckCommand(name: String, tag: String) : ExamCommand(name, tag) {
    override fun setUp(cmd: CommandCall?, evaluator: Evaluator?, resultRecorder: ResultRecorder?) {
        val checkTag = cmd.html()
        val td = td("colspan" to "3")
        checkTag.moveChildrenTo(td)
        checkTag.parent().below(tr()(td))
    }
}

class CaseCommand(tag: String, private var originalCfg: Configuration, private val nodeMatcher: NodeMatcher) :
    RestVerifyCommand(CASE, tag) {
    private var usedCfg = originalCfg
    private val cases = ArrayList<Map<String, Any?>>()
    private var number = 0

    override fun setUp(cmd: CommandCall, eval: Evaluator, resultRecorder: ResultRecorder) {
        val caseRoot = cmd.html()
        eval.setVariable("#$PLACEHOLDER_TYPE", if (fromEvaluator(eval).xml()) "xml" else "json")
        cases.clear()
        val where = caseRoot.first(WHERE)
        if (where != null) {
            val vars = where.takeAwayAttr(VARIABLES, "", eval).split(",").map { it.trim() }
            val vals = RowParser(where, VALUES, eval).parse()
            cases += vals.map { vars.mapIndexed { i, name -> "#$name" to it[i] }.toMap() }
        } else {
            cases.add(HashMap())
        }

        val body = caseRoot.first(BODY)
        val multiPart = caseRoot.first(MULTI_PART)
        val expected = caseRoot.firstOrThrow(EXPECTED)
        setUpJsonUnitConfiguration(expected)
        caseRoot.remove(body, expected, multiPart)(
            cases.map {
                val expectedToAdd = tag(EXPECTED).text(expected.text())
                expected.attr(PROTOCOL)?.let { expectedToAdd.attrs(PROTOCOL to it) }
                expected.attr(STATUS_CODE)?.let { expectedToAdd.attrs(STATUS_CODE to it) }
                expected.attr(REASON_PHRASE)?.let { expectedToAdd.attrs(REASON_PHRASE to it) }
                expected.attr(FROM)?.let { expectedToAdd.attrs(FROM to it) }
                tag(CASE)(
                    if (body == null)
                        null
                    else tag(BODY).text(body.text()).apply {
                        body.attr(FROM)?.let { this.attrs(FROM to it) }
                    },
                    if (multiPart == null)
                        null
                    else {
                        val multiPartArray = multiPart.all(PART).map {
                            tag(PART).text(it.text()).apply {
                                it.attr(NAME)?.let { this.attrs(NAME to it) }
                                it.attr(TYPE)?.let { this.attrs(TYPE to it) }
                                it.attr(FILE_NAME)?.let { this.attrs(FILE_NAME to it) }
                                it.attr(FROM)?.let { this.attrs(FROM to it) }
                            }
                        }.toTypedArray()
                        tag(MULTI_PART)(*multiPartArray)
                    }
                    ,
                    expectedToAdd
                )
            })
    }

    private fun setUpJsonUnitConfiguration(expected: Html) {
        usedCfg = originalCfg;
        expected.attr(IGNORED_PATHS)?.let { attr ->
            usedCfg = usedCfg.whenIgnoringPaths(*attr.split(";").filter { it.isNotEmpty() }.toTypedArray())
        }
        expected.attr(JSON_UNIT_OPTIONS)?.let { attr -> overrideJsonUnitOption(attr) }
    }

    private fun overrideJsonUnitOption(attr: String) {
        val first = usedCfg.options.values().first()
        val other = usedCfg.options.values();
        other.remove(first);
        other.addAll(attr.split(";").filter { it.isNotEmpty() }.map { Option.valueOf(it) }.toSet())
        usedCfg = usedCfg.withOptions(Options(first, *other.toTypedArray()))
    }

    override fun execute(commandCall: CommandCall, evaluator: Evaluator, resultRecorder: ResultRecorder) {
        val childCommands = commandCall.children
        val root = commandCall.html()

        val executor = fromEvaluator(evaluator)
        val isJson = !executor.xml()
        val urlParams = root.takeAwayAttr(URL_PARAMS)
        val cookies = root.takeAwayAttr(COOKIES)

        for (aCase in cases) {
            aCase.forEach { (key, value) -> evaluator.setVariable(key, value) }

            cookies?.let { executor.cookies(evaluator.resolveJson(it)) }

            executor.urlParams(if (urlParams == null) null else evaluator.resolveJson(urlParams))

            val caseTR = tr().insteadOf(root.firstOrThrow(CASE))
            val body = caseTR.first(BODY)
            if (body != null) {
                val content = body.content(evaluator)
                var bodyStr: String
                if (isJson) {
                    bodyStr = evaluator.resolveJson(content)
                    td().insteadOf(body).css("json").style(MAX_WIDTH).removeChildren().text(bodyStr.prettyJson())
                } else {
                    bodyStr = evaluator.resolveXml(content)

                    td().insteadOf(body).css("xml").style(MAX_WIDTH).removeChildren()
                        .text(bodyStr.prettyXml())
                }
                executor.body(bodyStr)
            }
            processMultipart(caseTR, evaluator, executor)

            val expected = caseTR.firstOrThrow(EXPECTED)
            val expectedStatus = expectedStatus(expected)
            val statusTd = td(expectedStatus)
            caseTR(statusTd)

            childCommands.setUp(evaluator, resultRecorder)
            evaluator.setVariable("#exam_response", executor.execute())
            childCommands.execute(evaluator, resultRecorder)
            childCommands.verify(evaluator, resultRecorder)

            check(td().insteadOf(expected), evaluator, resultRecorder, isJson)
            resultRecorder.check(statusTd, executor.statusLine(), expectedStatus) { a, e ->
                a.trim() == e.trim()
            }
        }
    }

    private fun processMultipart(caseTR: Html, evaluator: Evaluator, executor: RequestExecutor) {
        val multiPart = caseTR.first(MULTI_PART)
        if (multiPart != null) {
            val table = table()
            multiPart.all(PART).forEach {
                val mpType = it.takeAwayAttr(TYPE)
                val name = it.takeAwayAttr(PART_NAME)
                val fileName = it.takeAwayAttr(FILE_NAME)
                val content = it.content(evaluator)

                table(tr()(td()(badge("Part", "light")), td()(
                        name?.let { badge(name.toString(), "warning") },
                        mpType?.let { badge(mpType.toString(), "info") },
                        fileName?.let { code(fileName.toString()) })))
                val mpStr: String
                if (executor.xml(mpType.toString())) {
                    mpStr = evaluator.resolveXml(content)
                    table(tr()(td()(badge("Content", "dark")), td(mpStr.prettyXml()).css("xml").style(MAX_WIDTH)))
                } else {
                    mpStr = evaluator.resolveJson(content)
                    table(tr()(td()(badge("Content", "dark")), td(mpStr.prettyJson()).css("json").style(MAX_WIDTH)))
                }
                if (mpType == null)
                    executor.multiPart(name.toString(), fileName.toString(), mpStr.toByteArray(Charset.forName("UTF-8")))
                else
                    executor.multiPart(name.toString(), mpType.toString(), mpStr)

            }
            multiPart.removeChildren()
            td().insteadOf(multiPart)(table)
        }
    }

    private fun checkJsonContent(actual: String, expected: String, resultRecorder: ResultRecorder, root: Html) {
        val prettyActual = actual.prettyJson()
        try {
            assertJsonEquals(expected, prettyActual, usedCfg)
            resultRecorder.pass(root)
        } catch (e: Throwable) {
            if (e is AssertionError || e is Exception) {
                resultRecorder.failure(root, prettyActual, expected)
                root.below(
                    span(e.message, CLASS to "exceptionMessage")
                )
            } else throw e
        }
    }

    private fun checkXmlContent(actual: String, expected: String, resultRecorder: ResultRecorder, root: Html) {
        val prettyActual = actual.prettyXml()
        try {
            resultRecorder.check(root, prettyActual, expected) { a, e ->
                a.equalToXml(e, nodeMatcher, usedCfg)
            }
        } catch (e: Exception) {
            resultRecorder.failure(root, prettyActual, expected)
            root.below(
                span(e.message, CLASS to "exceptionMessage")
            )
        }
    }

    private fun expectedStatus(expected: Html) = listOf(
        expected.takeAwayAttr(PROTOCOL, "HTTP/1.1").trim(),
        expected.takeAwayAttr(STATUS_CODE, "200").trim(),
        expected.takeAwayAttr(REASON_PHRASE, "OK").trim()
    ).joinToString(" ")

    override fun verify(cmd: CommandCall, evaluator: Evaluator, resultRecorder: ResultRecorder) {
        val executor = fromEvaluator(evaluator)
        val colspan = if (executor.hasRequestBody()) "3" else "2"
        val rt = cmd.html()
        val caseDesc = caseDesc(rt.attr(DESC), evaluator)
        rt.attrs("data-type" to CASE, "id" to caseDesc).above(
            tr()(
                td(caseDesc, "colspan" to colspan).muted()
            )
        )
    }

    private fun caseDesc(desc: String?, eval: Evaluator): String =
        "${++number}) " + if (desc == null) "" else eval.resolveJson(desc)

    private fun check(root: Html, eval: Evaluator, resultRecorder: ResultRecorder, json: Boolean) {
        val expected = resolve(json, root.content(eval), eval)

        root.removeChildren().text(expected).css(if (json) "json" else "xml").style(MAX_WIDTH)

        val executor = fromEvaluator(eval)
        fillCaseContext(root, executor)
        val actual = executor.responseBody()
        when {
            expected.isNotEmpty() && actual.isEmpty() -> resultRecorder.failure(root, "(empty)", expected)
            expected.isEmpty() && actual.isEmpty() -> resultRecorder.pass(root)
            else -> check(json, actual, expected, resultRecorder, root)
        }
    }

    private fun check(json: Boolean, actual: String, expected: String, resultRecorder: ResultRecorder, root: Html) =
        if (json) {
            checkJsonContent(actual, expected, resultRecorder, root)
        } else {
            checkXmlContent(actual, expected, resultRecorder, root)
        }

    private fun resolve(json: Boolean, content: String, eval: Evaluator): String = if (json) {
        eval.resolveJson(content).prettyJson()
    } else {
        eval.resolveXml(content).prettyXml()
    }

    private fun fillCaseContext(root: Html, executor: RequestExecutor) {
        val cookies = executor.cookies
        root.parent().above(
            tr()(
                td("colspan" to if (executor.hasRequestBody()) "3" else "2")(
                    div()(
                        italic("${executor.requestMethod()} "),
                        code(executor.requestUrlWithParams()),
                        *cookiesTags(cookies)
                    )
                )
            )
        )
    }

    private fun cookiesTags(cookies: String?): Array<Html> {
        return (if (cookies != null && cookies.isNotEmpty()) {
            listOf(
                italic(" Cookies "),
                code(cookies)
            )
        } else listOf(span(""))).toTypedArray()
    }

    companion object {
        private const val MAX_WIDTH = "max-width:550px"
    }
}