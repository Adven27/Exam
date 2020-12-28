package com.adven.concordion.extensions.exam.ws

import com.adven.concordion.extensions.exam.core.toMap
import io.restassured.RestAssured.given
import io.restassured.http.Method
import io.restassured.http.Method.DELETE
import io.restassured.http.Method.GET
import io.restassured.http.Method.HEAD
import io.restassured.http.Method.OPTIONS
import io.restassured.http.Method.PATCH
import io.restassured.http.Method.POST
import io.restassured.http.Method.PUT
import io.restassured.response.Response
import org.concordion.api.Evaluator

@Suppress("TooManyFunctions")
class RequestExecutor private constructor() {
    private lateinit var method: Method
    private lateinit var url: String
    private lateinit var response: Response
    private var multiPart: MutableList<MultiPart> = ArrayList()
    private var body: String? = null
    private lateinit var type: String
    private var urlParams: String? = null
    internal val headers: MutableMap<String, String> = HashMap()
    internal var cookies: String? = null
        private set

    internal fun method(method: Method): RequestExecutor {
        this.method = method
        return this
    }

    fun url(url: String): RequestExecutor {
        this.url = url
        return this
    }

    fun urlParams(params: String?): RequestExecutor {
        this.urlParams = params
        return this
    }

    fun type(type: String): RequestExecutor {
        this.type = type
        return this
    }

    fun header(headerName: String, headerValue: String): RequestExecutor {
        headers[headerName] = headerValue
        return this
    }

    fun headers(headersMap: Map<String, String>): RequestExecutor {
        headers.clear()
        headers.putAll(headersMap)
        return this
    }

    fun body(body: String): RequestExecutor {
        this.body = body
        return this
    }

    fun multiPart(name: String, fileName: String, value: Any): RequestExecutor {
        this.multiPart.add(MultiPart(name, value, fileName))
        return this
    }

    fun cookies(cookies: String?): RequestExecutor {
        this.cookies = cookies
        return this
    }

    fun contentType() = type

    fun xml() = type.contains("xml", true)
    fun xml(type: String) = type.contains("xml", true)

    fun responseHeader(attributeValue: String) = response.getHeader(attributeValue)

    fun statusLine() = response.statusLine()

    fun statusCode() = response.statusCode

    fun responseTime() = response.time

    fun responseBody() = response.body().asString()

    fun requestUrlWithParams() = url + if (urlParams != null) "?$urlParams" else ""

    fun hasRequestBody() = method == POST || method == PUT

    fun requestMethod() = method

    internal fun execute(): Response {
        val request = given()
        request.headers(headers)

        body?.let { request.body(it) }
        multiPart.forEach {
            if (it.value is ByteArray) {
                request.multiPart(it.name, it.type, it.value)
            } else {
                request.multiPart(it.name, it.value, it.type)
            }
        }
        type.let { request.contentType(it) }
        cookies?.let {
            request.cookies(it.toMap())
        }
        response = when (method) {
            PUT -> request.put(requestUrlWithParams())
            GET -> request.get(requestUrlWithParams())
            POST -> request.post(requestUrlWithParams())
            PATCH -> request.patch(url)
            DELETE -> request.delete(requestUrlWithParams())
            OPTIONS -> request.options(requestUrlWithParams())
            HEAD -> request.head(requestUrlWithParams())
            else -> throw UnsupportedOperationException(method.name)
        }
        return response
    }

    companion object {
        private const val REQUEST_EXECUTOR_VARIABLE = "#request"

        internal fun fromEvaluator(evaluator: Evaluator): RequestExecutor =
            evaluator.getVariable(REQUEST_EXECUTOR_VARIABLE) as RequestExecutor

        internal fun newExecutor(evaluator: Evaluator): RequestExecutor {
            val variable = RequestExecutor()
            evaluator.setVariable(REQUEST_EXECUTOR_VARIABLE, variable)
            return variable
        }
    }
}

class MultiPart(
    val name: String,
    val value: Any,
    val type: String? = null // for file used as name
)

fun RequestExecutor.httpDesc() = "${requestMethod()} ${requestUrlWithParams()} HTTP/1.1\n" +
    (if (!cookies.isNullOrEmpty()) "Cookies: ${cookies}\n" else "") +
    headers.map { "${it.key}: ${it.value}" }.joinToString("\n")
