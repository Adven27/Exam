package com.adven.concordion.extensions.exam.ws

import com.adven.concordion.extensions.exam.core.ExamExtension
import com.adven.concordion.extensions.exam.core.commands.ExamCommand
import com.adven.concordion.extensions.exam.core.commands.ExamVerifyCommand
import com.adven.concordion.extensions.exam.core.html.Html
import com.adven.concordion.extensions.exam.core.html.NAME
import com.adven.concordion.extensions.exam.core.html.RowParserEval
import com.adven.concordion.extensions.exam.core.html.badge
import com.adven.concordion.extensions.exam.core.html.code
import com.adven.concordion.extensions.exam.core.html.div
import com.adven.concordion.extensions.exam.core.html.html
import com.adven.concordion.extensions.exam.core.html.pill
import com.adven.concordion.extensions.exam.core.html.span
import com.adven.concordion.extensions.exam.core.html.table
import com.adven.concordion.extensions.exam.core.html.tag
import com.adven.concordion.extensions.exam.core.html.td
import com.adven.concordion.extensions.exam.core.html.th
import com.adven.concordion.extensions.exam.core.html.thead
import com.adven.concordion.extensions.exam.core.html.tr
import com.adven.concordion.extensions.exam.core.resolveForContentType
import com.adven.concordion.extensions.exam.core.resolveJson
import com.adven.concordion.extensions.exam.core.resolveNoType
import com.adven.concordion.extensions.exam.core.resolveValues
import com.adven.concordion.extensions.exam.core.resolveXml
import com.adven.concordion.extensions.exam.core.toHtml
import com.adven.concordion.extensions.exam.core.toMap
import com.adven.concordion.extensions.exam.core.utils.PLACEHOLDER_TYPE
import com.adven.concordion.extensions.exam.core.utils.content
import com.adven.concordion.extensions.exam.core.utils.prettyJson
import com.adven.concordion.extensions.exam.core.utils.prettyXml
import com.adven.concordion.extensions.exam.ws.RequestExecutor.Companion.fromEvaluator
import io.restassured.http.ContentType
import io.restassured.http.Method
import net.javacrumbs.jsonunit.core.Configuration
import net.javacrumbs.jsonunit.core.Option
import net.javacrumbs.jsonunit.core.internal.Options
import org.concordion.api.CommandCall
import org.concordion.api.Evaluator
import org.concordion.api.Fixture
import org.concordion.api.ResultRecorder
import java.nio.charset.Charset
import java.util.ArrayList
import java.util.HashMap
import java.util.Random

private const val HEADERS = "headers"
private const val TYPE = "contentType"
private const val URL = "url"
private const val DESC = "desc"
private const val URL_PARAMS = "urlParams"
private const val COOKIES = "cookies"
private const val HEADER_MAX_LENGTH = 200
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
private const val RESPONSE_CHECK_FAIL_TMPL = //language=xml
    """
    <div class="card border-danger bg-warning">
      <div class="card-body mb-1 mt-1">
        <pre id='%s' class="card-text" style='white-space: pre-wrap;'/>
      </div>
    </div>
    """
private const val ENDPOINT_HEADER_TMPL = //language=xml
    """
    <div class="input-group input-group-sm">
      <div class="input-group-prepend">
        <span class="input-group-text" style='border:none;'>%s</span>
      </div>
      <div class="form-control bg-light text-dark font-weight-light" style='border:none;'>
        <span id='%s'></span>
      </div>
    </div>
    """
private const val ENDPOINT_TMPL = //language=xml
    """
    <div class="input-group mb-1 mt-1">
      <div class="input-group-prepend">
        <span class="input-group-text %s text-white">%s</span>
      </div>
      <div class="form-control bg-light text-primary font-weight-light">
        <span id='%s'></span>
      </div>
    </div>
    """

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

    override fun setUp(
        commandCall: CommandCall?,
        evaluator: Evaluator?,
        resultRecorder: ResultRecorder?,
        fixture: Fixture
    ) {
        val executor = RequestExecutor.newExecutor(evaluator!!).method(method)
        val root = commandCall.html()

        val url = attr(root, URL, "/", evaluator)
        val type = attr(root, TYPE, contentType, evaluator)
        val cookies = cookies(evaluator, root)
        val headers = headers(evaluator, root)

        startTable(root, executor.hasRequestBody()).above(
            addRequestDescTo(url, type, cookies, headers)
        )
        executor.type(type).url(url).headers(headers).cookies(cookies)
    }

    private fun startTable(html: Html, hasRequestBody: Boolean): Html {
        val table = table()(
            thead()(
                if (hasRequestBody) th("Request", "style" to "text-align:center;", "class" to "bg-light") else null,
                th(
                    "Expected response",
                    "colspan" to (if (hasRequestBody) "1" else "2"),
                    "style" to "text-align:center;",
                    "class" to "bg-light"
                )
            )
        )
        html.dropAllTo(table)
        return table
    }

    private fun cookies(eval: Evaluator?, html: Html): String? {
        val cookies = html.takeAwayAttr(COOKIES, eval)
        eval!!.setVariable("#cookies", cookies)
        return cookies
    }

    @Suppress("SpreadOperator")
    private fun addRequestDescTo(url: String, type: String, cookies: String?, headers: Map<String, String>) =
        div()(
            endpoint(url, method),
            contentType(type),
            if (cookies != null) cookies(cookies) else null,
            *headers.map { header(it) }.toTypedArray()
        )

    private fun attr(html: Html, attrName: String, defaultValue: String, evaluator: Evaluator?): String {
        val attr = html.takeAwayAttr(attrName, defaultValue, evaluator!!)
        evaluator.setVariable("#$attrName", attr)
        return attr
    }
}

private fun headers(eval: Evaluator, html: Html): Map<String, String> =
    html.takeAwayAttr(HEADERS)?.toMap()?.resolveValues(eval) ?: emptyMap()

open class RestVerifyCommand(name: String, tag: String) : ExamVerifyCommand(name, tag, RestResultRenderer())

class CaseCheckCommand(name: String, tag: String) : ExamCommand(name, tag) {
    override fun setUp(cmd: CommandCall?, evaluator: Evaluator?, resultRecorder: ResultRecorder?, fixture: Fixture) {
        val checkTag = cmd.html()
        val td = td("colspan" to "2")
        checkTag.moveChildrenTo(td)
        checkTag.parent().below(tr()(td))
    }
}

@Suppress("TooManyFunctions")
class CaseCommand(
    tag: String,
    private val contentResolvers: Map<ContentType, WsPlugin.ContentResolver>,
    private val contentVerifiers: Map<ContentType, WsPlugin.ContentVerifier>,
    private val contentPrinters: Map<ContentType, WsPlugin.ContentPrinter>,
    private val contentTypeResolver: WsPlugin.ContentTypeResolver
) : RestVerifyCommand(CASE, tag) {
    private lateinit var contentResolver: WsPlugin.ContentResolver
    private lateinit var contentVerifier: WsPlugin.ContentVerifier
    private lateinit var contentPrinter: WsPlugin.ContentPrinter

    private val cases = ArrayList<Map<String, Any?>>()
    private var number = 0

    @Suppress("SpreadOperator", "ComplexMethod")
    override fun setUp(cmd: CommandCall, eval: Evaluator, resultRecorder: ResultRecorder, fixture: Fixture) {
        val caseRoot = cmd.html()
        eval.setVariable("#$PLACEHOLDER_TYPE", if (fromEvaluator(eval).xml()) "xml" else "json")
        cases.clear()
        val where = caseRoot.first(WHERE)
        if (where != null) {
            val vars = where.takeAwayAttr(VARIABLES, "", eval).split(",").map { it.trim() }
            val vals = RowParserEval(where, VALUES, eval).parse()
            cases += vals.map { vars.mapIndexed { i, name -> "#$name" to it[i] }.toMap() }
        } else {
            cases.add(HashMap())
        }

        val body = caseRoot.first(BODY)
        val multiPart = caseRoot.first(MULTI_PART)
        val expected = caseRoot.firstOrThrow(EXPECTED)
        val contentType = fromEvaluator(eval).contentType()
        val resolvedType = contentTypeResolver.resolve(contentType)
        contentVerifier = contentVerifiers[resolvedType]
            ?: throw IllegalStateException("Content verifier for type $resolvedType not found. Provide one through WsPlugin constructor.")
        contentResolver = contentResolvers[resolvedType]
            ?: throw IllegalStateException("Content resolver for type $resolvedType not found. Provide one through WsPlugin constructor.")
        contentPrinter = contentPrinters[resolvedType]
            ?: throw IllegalStateException("Content printer for type $resolvedType not found. Provide one through WsPlugin constructor.")

        setUpJsonUnitConfiguration(expected.attr(IGNORED_PATHS), expected.attr(JSON_UNIT_OPTIONS))
        caseRoot.remove(body, expected, multiPart)(
            cases.map {
                val expectedToAdd = tag(EXPECTED).text(expected.text())
                expected.attr(PROTOCOL)?.let { expectedToAdd.attrs(PROTOCOL to it) }
                expected.attr(STATUS_CODE)?.let { expectedToAdd.attrs(STATUS_CODE to it) }
                expected.attr(REASON_PHRASE)?.let { expectedToAdd.attrs(REASON_PHRASE to it) }
                expected.attr(FROM)?.let { expectedToAdd.attrs(FROM to it) }
                tag(CASE)(
                    if (body == null) {
                        null
                    } else tag(BODY).text(body.text()).apply {
                        body.attr(FROM)?.let { this.attrs(FROM to it) }
                    },
                    if (multiPart == null) {
                        null
                    } else {
                        val multiPartArray = multiPart.all(PART).map { html ->
                            tag(PART).text(html.text()).apply {
                                html.attr(NAME)?.let { this.attrs(NAME to it) }
                                html.attr(TYPE)?.let { this.attrs(TYPE to it) }
                                html.attr(FILE_NAME)?.let { this.attrs(FILE_NAME to it) }
                                html.attr(FROM)?.let { this.attrs(FROM to it) }
                            }
                        }.toTypedArray()
                        tag(MULTI_PART)(*multiPartArray)
                    },
                    expectedToAdd
                )
            }
        )
    }

    @Suppress("SpreadOperator")
    private fun setUpJsonUnitConfiguration(ignorePath: String?, jsonUnitOptions: String?) {
        var usedCfg = ExamExtension.DEFAULT_JSON_UNIT_CFG
        ignorePath?.let {
            usedCfg = usedCfg.whenIgnoringPaths(*it.split(";").filter { it.isNotEmpty() }.toTypedArray())
        }
        jsonUnitOptions?.let { usedCfg = overrideJsonUnitOption(it, usedCfg) }
        contentVerifier.setConfiguration(usedCfg)
    }

    @Suppress("SpreadOperator")
    private fun overrideJsonUnitOption(attr: String, usedCfg: Configuration): Configuration {
        val first = usedCfg.options.values().first()
        val other = usedCfg.options.values()
        other.remove(first)
        other.addAll(attr.split(";").filter { it.isNotEmpty() }.map { Option.valueOf(it) }.toSet())
        return usedCfg.withOptions(Options(first, *other.toTypedArray()))
    }

    override fun execute(
        commandCall: CommandCall,
        evaluator: Evaluator,
        resultRecorder: ResultRecorder,
        fixture: Fixture
    ) {
        val childCommands = commandCall.children
        val root = commandCall.html()

        val executor = fromEvaluator(evaluator)
        val urlParams = root.takeAwayAttr(URL_PARAMS)
        val cookies = root.takeAwayAttr(COOKIES)
        val headers = root.takeAwayAttr(HEADERS)

        for (aCase in cases) {
            aCase.forEach { (key, value) -> evaluator.setVariable(key, value) }

            cookies?.let { executor.cookies(evaluator.resolveNoType(it)) }
            headers?.let { executor.headers(headers.toMap().resolveValues(evaluator)) }

            executor.urlParams(if (urlParams == null) null else evaluator.resolveNoType(urlParams))

            val caseTR = tr().insteadOf(root.firstOrThrow(CASE))
            val body = caseTR.first(BODY)
            if (body != null) {
                val content = body.content(evaluator)
                val bodyStr = contentResolver.resolve(content, evaluator)
                td().insteadOf(body).css(contentPrinter.style() + " exp-body").style("min-width: 20%; width: 50%;")
                    .removeChildren()
                    .text(contentPrinter.print(bodyStr))
                executor.body(bodyStr)
            }
            processMultipart(caseTR, evaluator, executor)

            childCommands.setUp(evaluator, resultRecorder, fixture)
            evaluator.setVariable("#exam_response", executor.execute())
            childCommands.execute(evaluator, resultRecorder, fixture)
            childCommands.verify(evaluator, resultRecorder, fixture)

            val expected = caseTR.firstOrThrow(EXPECTED)
            val expectedStatus = expectedStatus(expected)
            val statusEl = span().css("exp-status")
            check(
                td("colspan" to (if (body == null) "2" else "1")).css("exp-body").insteadOf(expected),
                statusEl,
                evaluator,
                resultRecorder,
                executor.contentType()
            )
            if (checkStatusLine(expectedStatus)) {
                resultRecorder.check(statusEl, executor.statusLine(), statusLine(expectedStatus)) { a, e ->
                    a.trim() == e.trim()
                }
            } else {
                resultRecorder.check(statusEl, executor.statusCode().toString(), expectedStatus.second) { a, e ->
                    a.trim() == e.trim()
                }
            }
        }
    }

    private fun statusLine(status: Triple<String?, String, String?>) =
        "${status.first} ${status.second} ${status.third}"

    private fun checkStatusLine(status: Triple<String?, String, String?>) = status.third != null

    private fun processMultipart(caseTR: Html, evaluator: Evaluator, executor: RequestExecutor) {
        val multiPart = caseTR.first(MULTI_PART)
        if (multiPart != null) {
            val table = table()
            multiPart.all(PART).forEach {
                val mpType = it.takeAwayAttr(TYPE)
                val name = it.takeAwayAttr(PART_NAME)
                val fileName = it.takeAwayAttr(FILE_NAME)
                val content = it.content(evaluator)

                table(
                    tr()(
                        td()(badge("Part", "light")),
                        td()(
                            name?.let { badge(name.toString(), "warning") },
                            mpType?.let { badge(mpType.toString(), "info") },
                            fileName?.let { code(fileName.toString()) }
                        )
                    )
                )
                val mpStr: String
                if (executor.xml(mpType.toString())) {
                    mpStr = evaluator.resolveXml(content)
                    table(
                        tr()(
                            td()(badge("Content", "dark")),
                            td(mpStr.prettyXml()).css("xml")
                        )
                    )
                } else {
                    mpStr = evaluator.resolveJson(content)
                    table(
                        tr()(
                            td()(badge("Content", "dark")),
                            td(mpStr.prettyJson()).css("json")
                        )
                    )
                }
                if (mpType == null) {
                    executor.multiPart(
                        name.toString(),
                        fileName.toString(),
                        mpStr.toByteArray(Charset.forName("UTF-8"))
                    )
                } else {
                    executor.multiPart(name.toString(), mpType.toString(), mpStr)
                }
            }
            multiPart.removeChildren()
            td().insteadOf(multiPart)(table)
        }
    }

    private fun expectedStatus(expected: Html) = Triple(
        expected.takeAwayAttr(PROTOCOL, "HTTP/1.1").trim(),
        expected.takeAwayAttr(STATUS_CODE, "200").trim(),
        expected.takeAwayAttr(REASON_PHRASE)?.trim()
    )

    override fun verify(cmd: CommandCall, evaluator: Evaluator, resultRecorder: ResultRecorder, fixture: Fixture) {
        val rt = cmd.html()
        val caseDesc = caseDesc(rt.attr(DESC), evaluator)
        rt.attrs("data-type" to CASE, "id" to caseDesc).above(
            tr()(
                td(caseDesc, "colspan" to "2").muted().css("bg-light")
            )
        )
    }

    private fun caseDesc(desc: String?, eval: Evaluator): String =
        "${++number}) " + if (desc == null) "" else contentResolver.resolve(desc, eval)

    private fun check(
        root: Html,
        statusEl: Html,
        eval: Evaluator,
        resultRecorder: ResultRecorder,
        contentType: String
    ) {
        val executor = fromEvaluator(eval)
        fillCaseContext(root, statusEl, executor)
        check(
            executor.responseBody(),
            eval.resolveForContentType(root.content(eval), contentType),
            resultRecorder,
            root
        )
    }

    private fun check(actual: String, expected: String, resultRecorder: ResultRecorder, root: Html) {
        val fail = contentVerifier.verify(expected, actual).fail
        if (fail.isPresent) {
            val it = fail.get()
            val diff = div().css(contentPrinter.style())
            val errorMsg = errorMessage(it.details, diff)
            root.removeChildren()(errorMsg)
            resultRecorder.failure(diff, contentPrinter.print(it.actual), contentPrinter.print(it.expected))
        } else {
            root.removeChildren()(
                tag("exp").text(contentPrinter.print(expected)) css contentPrinter.style(),
                tag("act").text(contentPrinter.print(actual)) css contentPrinter.style()
            )
            resultRecorder.pass(root)
        }
    }

    private fun errorMessage(txt: String, diff: Html): Html = "error-${Random().nextInt()}".let { id ->
        String.format(RESPONSE_CHECK_FAIL_TMPL, id).toHtml().apply { findBy(id)!!.text(txt).below(diff) }
    }

    @Suppress("SpreadOperator", "MagicNumber")
    private fun fillCaseContext(root: Html, statusEl: Html, executor: RequestExecutor) {
        root.parent().above(
            tr()(
                td().style("max-width: 1px; width: ${if (executor.hasRequestBody()) 50 else 100}%;")(
                    div().css("httpstyle")(
                        tag("textarea").css("http").text(
                            "${executor.requestMethod()} ${executor.requestUrlWithParams()} HTTP/1.1" +
                                (if (!executor.cookies.isNullOrEmpty()) "\nCookies: ${executor.cookies}" else "") +
                                executor.headers.map { "\n${it.key}: ${it.value}" }.joinToString()
                        )
                    ),
                    td("style" to "padding-left: 0;")(
                        tag("small")(
                            statusEl,
                            pill("${executor.responseTime()}ms", "light")
                        )
                    )
                )
            )
        )
    }
}

private fun endpoint(url: String, method: Method): Html = "endpoint-${Random().nextInt()}".let { id ->
    String.format(ENDPOINT_TMPL, bgByMethod(method), method.name, id).toHtml().apply { findBy(id)?.text(url) }
}

private fun bgByMethod(method: Method) = when (method) {
    Method.GET -> "bg-primary"
    Method.POST -> "bg-success"
    Method.PUT -> "bg-warning"
    Method.PATCH -> "bg-warning"
    Method.DELETE -> "bg-danger"
    else -> "bg-dark"
}

private fun header(it: Map.Entry<String, String>) = "header-${Random().nextInt()}".let { id ->
    String.format(ENDPOINT_HEADER_TMPL, it.key, id).toHtml().apply {
        findBy(id)?.text(it.value.cutString(HEADER_MAX_LENGTH))
    }
}

private fun cookies(cookies: String) = "header-${Random().nextInt()}".let { id ->
    String.format(ENDPOINT_HEADER_TMPL, "Cookies", id).toHtml().apply {
        findBy(id)?.text(cookies.cutString(HEADER_MAX_LENGTH))
    }
}

private fun contentType(type: String) = "header-${Random().nextInt()}".let { id ->
    String.format(ENDPOINT_HEADER_TMPL, "Content-Type", id).toHtml().apply {
        findBy(id)?.text(type.cutString(HEADER_MAX_LENGTH))
    }
}

private fun String.cutString(max: Int) = if (length > max) take(max) + "..." else this
