package com.adven.concordion.extensions.exam.rest

import com.adven.concordion.extensions.exam.commands.ExamCommand
import com.adven.concordion.extensions.exam.commands.ExamVerifyCommand
import com.adven.concordion.extensions.exam.html.*
import com.adven.concordion.extensions.exam.resolveJson
import com.adven.concordion.extensions.exam.resolveXml
import com.adven.concordion.extensions.exam.rest.RequestExecutor.Companion.fromEvaluator
import com.adven.concordion.extensions.exam.utils.content
import com.adven.concordion.extensions.exam.utils.equalToXml
import com.adven.concordion.extensions.exam.utils.prettyPrintJson
import com.adven.concordion.extensions.exam.utils.prettyPrintXml
import com.jayway.restassured.http.Method
import net.javacrumbs.jsonunit.JsonAssert.assertJsonEquals
import net.javacrumbs.jsonunit.core.Configuration
import org.concordion.api.CommandCall
import org.concordion.api.Evaluator
import org.concordion.api.ResultRecorder
import org.concordion.internal.util.Check
import org.xmlunit.diff.NodeMatcher
import java.util.*

private const val HEADERS = "headers"
private const val TYPE = "type"
private const val URL = "url"
private const val DESC = "desc"
private const val URL_PARAMS = "urlParams"
private const val COOKIES = "cookies"
private const val VARIABLES = "vars"
private const val VALUES = "vals"
private const val BODY = "body"
private const val EXPECTED = "expected"
private const val WHERE = "where"
private const val CASE = "case"
private const val PROTOCOL = "protocol"
private const val STATUS_CODE = "statusCode"
private const val REASON_PHRASE = "reasonPhrase"
private const val FROM = "from"

class PutCommand(name: String, tag: String) : RequestCommand(name, tag, Method.PUT)
class GetCommand(name: String, tag: String) : RequestCommand(name, tag, Method.GET)
class PostCommand(name: String, tag: String) : RequestCommand(name, tag, Method.POST)
class DeleteCommand(name: String, tag: String) : RequestCommand(name, tag, Method.DELETE)
class SoapCommand(name: String, tag: String) : RequestCommand(name, tag, Method.POST, "application/soap+xml; charset=UTF-8;")

sealed class RequestCommand(name: String, tag: String, val method: Method, private val contentType: String = "application/json") : ExamCommand(name, tag) {

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
            val headersArray = headers.split(",".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            for (i in headersArray.indices) {
                if (i - 1 % 2 == 0) {
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
        Check.isFalse(cmd!!.hasChildCommands(),
                "Nesting commands inside an 'expectedStatus' is not supported")

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

class CaseCommand(tag: String, private val cfg: Configuration, private val nodeMatcher: NodeMatcher) : RestVerifyCommand(CASE, tag) {
    private val cases = ArrayList<Map<String, Any?>>()
    private var number = 0

    override fun setUp(cmd: CommandCall, eval: Evaluator, resultRecorder: ResultRecorder) {
        val caseRoot = cmd.html()
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
        val expected = caseRoot.firstOrThrow(EXPECTED)
        caseRoot.remove(body, expected)(
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
                            expectedToAdd
                    )
                })
    }

    override fun execute(cmd: CommandCall, eval: Evaluator, resultRecorder: ResultRecorder) {
        val childCommands = cmd.children
        val root = cmd.html()

        val executor = fromEvaluator(eval)
        val isJson = !executor.xml()
        val urlParams = root.takeAwayAttr(URL_PARAMS)
        val cookies = root.takeAwayAttr(COOKIES)

        for (aCase in cases) {
            aCase.forEach { (key, value) -> eval.setVariable(key, value) }

            cookies?.let { executor.cookies(resolveJson(it, eval)) }

            executor.urlParams(if (urlParams == null) null else resolveJson(urlParams, eval))

            val caseTR = tr().insteadOf(root.firstOrThrow(CASE))
            val body = caseTR.first(BODY)
            if (body != null) {
                val content = body.content(eval)
                if (isJson) {
                    val bodyStr = eval.resolveJson(content)
                    td().insteadOf(body).css("json").removeAllChild().text(bodyStr.prettyPrintJson())
                    executor.body(bodyStr)
                } else {
                    val bodyStr = eval.resolveXml(content)

                    td().insteadOf(body).css("xml").removeAllChild().text(bodyStr.prettyPrintXml())
                    executor.body(bodyStr)
                }
            }

            val expected = caseTR.firstOrThrow(EXPECTED)
            val expectedStatus = expectedStatus(expected)
            val statusTd = td(expectedStatus)
            caseTR(statusTd)

            childCommands.setUp(eval, resultRecorder)
            val response = executor.execute()
            eval.setVariable("#exam_response", response)
            childCommands.execute(eval, resultRecorder)
            childCommands.verify(eval, resultRecorder)

            check(td().insteadOf(expected), eval, resultRecorder, isJson)
            resultRecorder.check(statusTd, executor.statusLine(), expectedStatus) { a, e ->
                a.trim() == e.trim()
            }
        }
    }

    private fun checkJsonContent(actual: String, expected: String, resultRecorder: ResultRecorder, root: Html) {
        val prettyActual = actual.prettyPrintJson()
        try {
            assertJsonEquals(expected, prettyActual, cfg)
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
        val prettyActual = actual.prettyPrintXml()
        try {
            resultRecorder.check(root, prettyActual, expected) { a, e ->
                a.equalToXml(e, nodeMatcher, cfg)
            }
        } catch (e: Exception) {
            resultRecorder.failure(root, prettyActual, expected)
            root.below(
                    span(e.message, CLASS to "exceptionMessage")
            )
        }
    }

    private fun expectedStatus(expected: Html) = StatusBuilder(
            expected.takeAwayAttr(PROTOCOL),
            expected.takeAwayAttr(STATUS_CODE),
            expected.takeAwayAttr(REASON_PHRASE)).build()

    override fun verify(cmd: CommandCall, evaluator: Evaluator, resultRecorder: ResultRecorder) {
        val executor = fromEvaluator(evaluator)
        val colspan = if (executor.hasRequestBody()) "3" else "2"
        val rt = cmd.html()
        val caseDesc = caseDesc(rt.attr(DESC), evaluator)
        rt.attrs("data-type" to CASE, "id" to caseDesc).above(
                tr()(
                        td(caseDesc, "colspan" to colspan).muted()))
    }

    private fun caseDesc(desc: String?, eval: Evaluator): String =
            "${++number}) " + if (desc == null) "" else resolveJson(desc, eval)

    private fun check(root: Html, eval: Evaluator, resultRecorder: ResultRecorder, json: Boolean) {
        val expected = resolve(json, root.content(eval), eval)

        root.removeAllChild().text(expected).css(if (json) "json" else "xml")

        val executor = fromEvaluator(eval)
        fillCaseContext(root, executor)
        val actual = executor.responseBody()
        if (actual.isEmpty()) {
            resultRecorder.failure(root, "(not set)", expected)
            return
        }
        check(json, actual, expected, resultRecorder, root)
    }

    private fun check(json: Boolean, actual: String, expected: String, resultRecorder: ResultRecorder, root: Html) =
            if (json) {
                checkJsonContent(actual, expected, resultRecorder, root)
            } else {
                checkXmlContent(actual, expected, resultRecorder, root)
            }

    private fun resolve(json: Boolean, content: String, eval: Evaluator): String = if (json) {
        resolveJson(content, eval).prettyPrintJson()
    } else {
        resolveXml(content, eval).prettyPrintXml()
    }

    private fun fillCaseContext(root: Html, executor: RequestExecutor) {
        val cookies = executor.cookies
        root.parent().above(
                tr()(
                        td("colspan" to if (executor.hasRequestBody()) "3" else "2")(
                                div()(
                                        italic("${executor.requestMethod()} "),
                                        code(executor.requestUrlWithParams()),
                                        *cookiesTags(cookies)))))
    }

    private fun cookiesTags(cookies: String?): Array<Html> {
        return (if (cookies != null && !cookies.isEmpty()) {
            listOf(
                    italic(" Cookies "),
                    code(cookies)
            )
        } else listOf(span(""))).toTypedArray()
    }
}