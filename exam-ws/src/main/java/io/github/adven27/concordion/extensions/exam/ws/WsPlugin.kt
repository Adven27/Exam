package io.github.adven27.concordion.extensions.exam.ws

import com.github.jknack.handlebars.Options
import io.github.adven27.concordion.extensions.exam.core.ContentPrinter
import io.github.adven27.concordion.extensions.exam.core.ContentTypeConfig
import io.github.adven27.concordion.extensions.exam.core.ContentVerifier
import io.github.adven27.concordion.extensions.exam.core.ExamPlugin
import io.github.adven27.concordion.extensions.exam.core.JsonPrinter
import io.github.adven27.concordion.extensions.exam.core.JsonResolver
import io.github.adven27.concordion.extensions.exam.core.JsonVerifier
import io.github.adven27.concordion.extensions.exam.core.XmlPrinter
import io.github.adven27.concordion.extensions.exam.core.XmlResolver
import io.github.adven27.concordion.extensions.exam.core.XmlVerifier
import io.github.adven27.concordion.extensions.exam.core.commands.ExamCommand
import io.github.adven27.concordion.extensions.exam.core.handlebars.ExamHelper
import io.github.adven27.concordion.extensions.exam.core.handlebars.HANDLEBARS
import io.github.adven27.concordion.extensions.exam.core.handlebars.evaluator
import io.restassured.RestAssured
import io.restassured.http.ContentType
import io.restassured.http.Header
import io.restassured.internal.RestAssuredResponseImpl
import io.restassured.response.Response

@Suppress("LongParameterList")
class WsPlugin @JvmOverloads constructor(
    uri: String = "http://localhost",
    basePath: String = "",
    port: Int = 8080,
    additionalContentTypeConfigs: Map<ContentType, ContentTypeConfig> = emptyMap(),
    private val contentTypeResolver: ContentTypeResolver = MultiPartAware()
) : ExamPlugin.NoSetUp() {

    constructor(withPort: Int) : this(port = withPort)
    constructor(withBasePath: String, withPort: Int) : this(basePath = withBasePath, port = withPort)

    private val contentTypeConfigs: MutableMap<ContentType, ContentTypeConfig> = mutableMapOf(
        ContentType.JSON to ContentTypeConfig(JsonResolver(), JsonVerifier(), JsonPrinter()),
        ContentType.XML to ContentTypeConfig(XmlResolver(), XmlVerifier(), XmlPrinter()),
        ContentType.TEXT to ContentTypeConfig(JsonResolver(), ContentVerifier.Default("text"), ContentPrinter.AsIs()),
    )

    init {
        RestAssured.baseURI = uri
        RestAssured.basePath = basePath
        RestAssured.port = port
        contentTypeConfigs += additionalContentTypeConfigs
        HANDLEBARS.registerHelpers(WsHelperSource::class.java)
    }

    override fun commands(): List<ExamCommand> = listOf(
        SoapCommand("soap", "div"),
        PostCommand("post", "div"),
        GetCommand("get", "div"),
        PutCommand("put", "div"),
        DeleteCommand("delete", "div"),
        CaseCommand("tr", contentTypeConfigs, contentTypeResolver),
        CaseCheckCommand("check", "div")
    )

    interface ContentTypeResolver {
        open class Simple : ContentTypeResolver {
            override fun resolve(contentType: String) = ContentType.fromContentType(contentType) ?: ContentType.TEXT
        }

        fun resolve(contentType: String): ContentType
    }

    open class MultiPartAware : ContentTypeResolver.Simple() {
        override fun resolve(contentType: String) = if (contentType == "multipart/form-data") {
            ContentType.JSON
        } else super.resolve(contentType)
    }
}

@Suppress("EnumNaming")
enum class WsHelperSource(
    override val example: String,
    override val context: Map<String, Any?> = emptyMap(),
    override val expected: Any? = "",
    override val options: Map<String, String> = emptyMap()
) : ExamHelper {
    responseBody(
        "{{responseBody 'name'}}",
        mapOf("exam_response" to "{\"name\" : \"adam\"}".response()),
        "adam"
    ) {
        override fun apply(context: Any?, options: Options): Any? = resp(options).let {
            if (context is String) it.jsonPath().getString("$context")
            else it.body.print()
        }
    },
    responseStatusCode(
        "{{responseStatusCode}}",
        mapOf("exam_response" to "{}".response()),
        "200"
    ) {
        override fun apply(context: Any?, options: Options): Any =
            resp(options).statusCode()
    },
    responseHeaders(
        "{{responseHeaders}}",
        mapOf("exam_response" to "{}".response()),
        listOf(Header("h1", "1"), Header("h2", "2"))
    ) {
        override fun apply(context: Any?, options: Options): Any =
            resp(options).headers()
    },
    responseHeader(
        "{{responseHeader 'headerName'}}",
        mapOf("exam_response" to "{}".response()),
        "header value"
    ) {
        override fun apply(context: Any?, options: Options): Any? =
            resp(options).getHeader("$context")
    },
    response(
        "{{response}}",
        mapOf("exam_response" to "{\"name\" : \"adam\"}".response()),
        "adam"
    ) {
        override fun apply(context: Any?, options: Options): Any = resp(options)
    };

    protected fun resp(options: Options) = (options.evaluator().getVariable("#exam_response") as Response)
    override fun toString() = describe()
}

private fun String.response() = TestResponse(
    RestAssuredResponseImpl().apply {
        groovyResponse = io.restassured.internal.RestAssuredResponseOptionsGroovyImpl().apply {
            config = io.restassured.config.RestAssuredConfig.config()
            content = this@response
        }
    }
)

private class TestResponse(private val delegate: RestAssuredResponseImpl) : Response by delegate {
    override fun toString(): String = asString()
}
