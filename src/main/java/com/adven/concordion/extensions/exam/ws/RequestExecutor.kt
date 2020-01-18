package com.adven.concordion.extensions.exam.ws

import io.restassured.RestAssured.given
import io.restassured.http.Method
import io.restassured.http.Method.*
import io.restassured.response.Response
import org.concordion.api.Evaluator

class RequestExecutor private constructor() {
    private lateinit var method: Method
    private lateinit var url: String
    private lateinit var response: Response
    private var multiPart: MutableList<MultiPart> = ArrayList()
    private var body: String? = null
    private var type: String? = null
    private val headers = HashMap<String, String>()
    private var urlParams: String? = null
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

    fun header(headersMap: Map<String, String>): RequestExecutor {
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

    fun xml() = type?.contains("xml", true) ?: false
    fun xml(type: String) = type.contains("xml", true)

    fun responseHeader(attributeValue: String) = response.getHeader(attributeValue)

    fun statusLine() = response.statusLine()

    fun statusCode() = response.statusCode

    fun responseBody() = response.body().asString()

    fun requestUrlWithParams() = url + if (urlParams != null) "?$urlParams" else ""

    fun hasRequestBody() = method == POST || method == PUT

    fun requestMethod() = method.name

    fun requestHeader(header: String) = headers.get(header)

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
        type?.let { request.contentType(it) }
        cookies?.let {
            request.cookies(
                (if (it.trim().startsWith("{")) it.substring(1, it.lastIndex) else it)
                    .split(",")
                    .map {
                        val (n, v) = it.split("=")
                        Pair(n.trim(), v.trim())
                    }.toMap()
            )
        }

        response = when (method) {
            PUT -> request.put(url)
            GET -> request.get(requestUrlWithParams())
            POST -> request.post(url)
            PATCH -> request.patch(url)
            DELETE -> request.delete(requestUrlWithParams())
            else -> throw UnsupportedOperationException(method.name)
        }
        return response
    }

    companion object {
        private const val REQUEST_EXECUTOR_VARIABLE = "#request"

        internal fun fromEvaluator(evaluator: Evaluator): RequestExecutor {
            return evaluator.getVariable(REQUEST_EXECUTOR_VARIABLE) as RequestExecutor
        }

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
        val type: String? = null //for file used as name
)
