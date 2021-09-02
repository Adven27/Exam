package io.github.adven27.concordion.extensions.exam.ws

import io.github.adven27.concordion.extensions.exam.core.ContentTypeConfig
import io.github.adven27.concordion.extensions.exam.core.ExamExtension.Companion.contentTypeConfig
import io.github.adven27.concordion.extensions.exam.core.commands.ExamCommand
import io.github.adven27.concordion.extensions.exam.core.commands.ExamVerifyCommand
import io.github.adven27.concordion.extensions.exam.core.content
import io.github.adven27.concordion.extensions.exam.core.errorMessage
import io.github.adven27.concordion.extensions.exam.core.handlebars.PLACEHOLDER_TYPE
import io.github.adven27.concordion.extensions.exam.core.html.Html
import io.github.adven27.concordion.extensions.exam.core.html.NAME
import io.github.adven27.concordion.extensions.exam.core.html.RowParserEval
import io.github.adven27.concordion.extensions.exam.core.html.badge
import io.github.adven27.concordion.extensions.exam.core.html.code
import io.github.adven27.concordion.extensions.exam.core.html.codeHighlight
import io.github.adven27.concordion.extensions.exam.core.html.div
import io.github.adven27.concordion.extensions.exam.core.html.html
import io.github.adven27.concordion.extensions.exam.core.html.li
import io.github.adven27.concordion.extensions.exam.core.html.pill
import io.github.adven27.concordion.extensions.exam.core.html.span
import io.github.adven27.concordion.extensions.exam.core.html.table
import io.github.adven27.concordion.extensions.exam.core.html.tag
import io.github.adven27.concordion.extensions.exam.core.html.td
import io.github.adven27.concordion.extensions.exam.core.html.th
import io.github.adven27.concordion.extensions.exam.core.html.thead
import io.github.adven27.concordion.extensions.exam.core.html.tr
import io.github.adven27.concordion.extensions.exam.core.html.ul
import io.github.adven27.concordion.extensions.exam.core.prettyJson
import io.github.adven27.concordion.extensions.exam.core.prettyXml
import io.github.adven27.concordion.extensions.exam.core.resolveForContentType
import io.github.adven27.concordion.extensions.exam.core.resolveJson
import io.github.adven27.concordion.extensions.exam.core.resolveNoType
import io.github.adven27.concordion.extensions.exam.core.resolveValues
import io.github.adven27.concordion.extensions.exam.core.resolveXml
import io.github.adven27.concordion.extensions.exam.core.sameSizeWith
import io.github.adven27.concordion.extensions.exam.core.toHtml
import io.github.adven27.concordion.extensions.exam.core.toMap
import io.github.adven27.concordion.extensions.exam.ws.RequestExecutor.Companion.fromEvaluator
import io.restassured.http.ContentType
import io.restassured.http.Method
import org.concordion.api.CommandCall
import org.concordion.api.Element
import org.concordion.api.Evaluator
import org.concordion.api.Fixture
import org.concordion.api.ResultRecorder
import java.nio.charset.Charset
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
private const val VERIFY_AS = "verifyAs"
private const val PROTOCOL = "protocol"
private const val STATUS_CODE = "statusCode"
private const val REASON_PHRASE = "reasonPhrase"
private const val FROM = "from"
private const val ENDPOINT_HEADER_TMPL = //language=xml
    """
    <div class="input-group input-group-sm">
        <span class="input-group-text">%s</span>
        <span id='%s' class="form-control bg-light text-dark font-weight-light"/>
    </div>
    """
private const val ENDPOINT_TMPL = //language=xml
    """
    <div class="input-group mb-1 mt-1">
        <span class="input-group-text %s text-white">%s</span>
        <span class="form-control bg-light text-primary font-weight-light" id='%s'/>
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

        startTable(root).prependChild(
            addRequestDescTo(url, type, cookies, headers)
        )
        executor.type(type).url(url).headers(headers).cookies(cookies)
    }

    private fun startTable(html: Html): Html = table("class" to "ws-cases")(
        thead()(
            th(
                "Use cases:",
                "colspan" to "2",
                "style" to "text-align:center;",
                "class" to "text-secondary"
            )
        )
    ).apply { html.dropAllTo(this) }

    private fun cookies(eval: Evaluator?, html: Html): String? =
        html.takeAwayAttr(COOKIES, eval).apply { eval!!.setVariable("#cookies", this) }

    @Suppress("SpreadOperator")
    private fun addRequestDescTo(url: String, type: String, cookies: String?, headers: Map<String, String>) =
        div()(
            endpoint(url, method),
            contentType(type),
            if (cookies != null) cookies(cookies) else null,
            *headers.map { header(it) }.toTypedArray()
        )

    private fun attr(html: Html, attrName: String, defaultValue: String, eval: Evaluator?): String =
        html.takeAwayAttr(attrName, defaultValue, eval!!).apply { eval.setVariable("#$attrName", this) }
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
    private val contentTypeConfigs: Map<ContentType, ContentTypeConfig>,
    private val contentTypeResolver: WsPlugin.ContentTypeResolver
) : RestVerifyCommand(CASE, tag) {
    private lateinit var contentTypeConfig: ContentTypeConfig

    private val cases: MutableMap<String, Map<String, Any?>> = LinkedHashMap()
    private var number = 0

    @Suppress("SpreadOperator", "ComplexMethod")
    override fun setUp(cmd: CommandCall, eval: Evaluator, resultRecorder: ResultRecorder, fixture: Fixture) {
        val caseRoot = cmd.html()
        eval.setVariable("#$PLACEHOLDER_TYPE", if (fromEvaluator(eval).xml()) "xml" else "json")
        cases.clear()
        caseRoot.firstOptional(WHERE).map { where ->
            val vars = where.takeAwayAttr(VARIABLES, "", eval).split(",").map { it.trim() }
            val vals = RowParserEval(where, VALUES, eval).parse()
            caseRoot.remove(where)
            cases.putAll(
                vals.map {
                    it.key to vars.sameSizeWith(it.value).mapIndexed { i, name -> "#$name" to it.value[i] }.toMap()
                }.toMap()
            )
        }.orElseGet { cases["single"] = HashMap() }

        val body = caseRoot.first(BODY)
        val multiPart = caseRoot.first(MULTI_PART)
        val expected = caseRoot.firstOrThrow(EXPECTED)
        val contentType = fromEvaluator(eval).contentType()
        val resolvedType = contentTypeResolver.resolve(contentType)

        contentTypeConfig = expected.attr(VERIFY_AS)?.let { contentTypeConfig(it) }
            ?: byContentType(resolvedType)

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

    private fun byContentType(resolvedType: ContentType): ContentTypeConfig = contentTypeConfigs[resolvedType]
        ?: throw IllegalStateException("Content type config for type $resolvedType not found. Provide one through WsPlugin constructor.")

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
            aCase.value.forEach { (key, value) -> evaluator.setVariable(key, value) }

            cookies?.let { executor.cookies(evaluator.resolveNoType(it)) }
            headers?.let { executor.headers(headers.toMap().resolveValues(evaluator)) }

            executor.urlParams(if (urlParams == null) null else evaluator.resolveNoType(urlParams))

            val caseTR = tr().insteadOf(root.firstOrThrow(CASE))
            val body = caseTR.first(BODY)
            if (body != null) {
                val content = body.content(evaluator)
                val bodyStr = contentTypeConfig.resolver.resolve(content, evaluator)
                td().insteadOf(body).css(contentTypeConfig.printer.style() + " exp-body")
                    .style("min-width: 20%; width: 50%;")
                    .removeChildren()
                    .text(contentTypeConfig.printer.print(bodyStr))
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
                executor.contentType(),
                aCase.key
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
        val wheres = rt.el.getChildElements("tr")
        if (wheres.size > 2) {
            rt.below(
                tr()(
                    td("colspan" to "2")(
                        whereCaseTemplate(
                            wheres.withIndex().groupBy { it.index / 2 }
                                .map { entry -> tab(System.currentTimeMillis(), entry.value.map { it.value }) }
                        )
                    )
                )
            )
        }
        val caseDesc = caseDesc(rt.attr(DESC))
        rt.attrs("data-type" to CASE, "id" to caseDesc).prependChild(
            tr()(
                td(caseDesc, "colspan" to "2").muted().css("bg-light")
            )
        )
    }

    private fun caseDesc(desc: String?): String = "${++number}) " + (desc ?: "")

    @Suppress("LongParameterList")
    private fun check(
        root: Html,
        statusEl: Html,
        eval: Evaluator,
        resultRecorder: ResultRecorder,
        contentType: String,
        caseTitle: String
    ) {
        val executor = fromEvaluator(eval)
        check(
            executor.responseBody(),
            eval.resolveForContentType(root.content(eval), contentType),
            resultRecorder,
            root
        )
        val trBodies = root.parent().deepClone()
        val case = root.parent().parent()
        case.remove(root.parent())
        case()(
            trCaseDesc(caseTitle, statusEl, executor.hasRequestBody(), executor.responseTime(), executor.httpDesc()),
            trBodies
        )
    }

    private fun check(actual: String, expected: String, resultRecorder: ResultRecorder, root: Html) {
        (contentTypeConfig.verifier to contentTypeConfig.printer).let { (verifier, printer) ->
            verifier.verify(expected, actual).fail.map {
                val diff = div().css(printer.style())
                val (_, errorMsg) = errorMessage(message = it.details, html = diff, type = printer.style())
                root.removeChildren()(errorMsg)
                resultRecorder.failure(diff, printer.print(it.actual), printer.print(it.expected))
            }.orElseGet {
                root.removeChildren()(
                    tag("exp").text(printer.print(expected)) css printer.style(),
                    tag("act").text(printer.print(actual)) css printer.style()
                )
                resultRecorder.pass(root)
            }
        }
    }

    @Suppress("SpreadOperator", "MagicNumber")
    private fun trCaseDesc(caseTitle: String, statusEl: Html, hasReqBody: Boolean, responseTime: Long, desc: String) =
        tr("data-case-title" to caseTitle)(
            td().style("max-width: 1px; width: ${if (hasReqBody) 50 else 100}%;")(
                div().css("httpstyle")(
                    codeHighlight(desc, "http")
                )
            ),
            td("style" to "padding-left: 0;")(
                tag("small")(
                    statusEl,
                    pill("${responseTime}ms", "light")
                )
            )
        )
}

private fun endpoint(url: String, method: Method): Html = "endpoint-${Random().nextInt()}".let { id ->
    String.format(ENDPOINT_TMPL, method.background(), method.name, id).toHtml().apply { findBy(id)?.text(url) }
}

private fun Method.background() = when (this) {
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

private fun whereCaseTemplate(tabs: List<Pair<Html, Html>>): Html = tabs.let { list ->
    val failed = tabs.indexOfFirst { it.first.attr("class")?.contains("rest-failure") ?: false }
    val active = if (failed == -1) 0 else failed
    return div()(
        tag("nav")(
            ul("class" to "nav nav-tabs", "role" to "tablist")(
                list.mapIndexed { i, p ->
                    li().css("nav-item")(
                        p.first.apply { if (i == active) css("active show") }
                    )
                }
            )
        ),
        div()(
            div("class" to "tab-content")(
                list.mapIndexed { i, p -> p.second.apply { if (i == active) css("active show") } }
            )
        )
    )
}

private fun tab(id: Long, trs: List<Element>): Pair<Html, Html> {
    val cnt = trs.map { Html(it.deepClone()) }
    val parentElement = trs[0].parentElement
    parentElement.removeChild(trs[0])
    parentElement.removeChild(trs[1])
    val fail = cnt.any { it.descendants("fail").isNotEmpty() }
    val name = Random().nextInt()
    return Html(
        "button",
        cnt[0].attrOrFail("data-case-title"),
        "id" to "nav-$name-$id-tab",
        "class" to "nav-link small ${if (fail) "rest-failure" else "text-success"} ",
        "data-bs-toggle" to "tab",
        "data-bs-target" to "#nav-$name-$id",
        "role" to "tab",
        "aria-controls" to "nav-$name-$id",
        "aria-selected" to "false",
        "onclick" to "setTimeout(() => { window.dispatchEvent(new Event('resize')); }, 200)"
    ) to div(
        "class" to "tab-pane fade",
        "id" to "nav-$name-$id",
        "role" to "tabpanel",
        "aria-labelledby" to "nav-$name-$id-tab",
    )(table()(cnt))
}
