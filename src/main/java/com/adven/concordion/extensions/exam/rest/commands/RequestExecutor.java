package com.adven.concordion.extensions.exam.rest.commands;

import com.google.common.base.Splitter;
import com.jayway.restassured.response.Response;
import com.jayway.restassured.specification.RequestSpecification;
import org.concordion.api.Evaluator;

import java.util.HashMap;
import java.util.Map;

import static com.jayway.restassured.RestAssured.given;

public class RequestExecutor {
    private static final String REQUEST_EXECUTOR_VARIABLE = "#request";

    private Response response;
    private String method;
    private String url;
    private String body;
    private String type;
    private Map<String, String> headers = new HashMap();
    private String urlParams;
    private String cookies;

    private RequestExecutor() {
    }

    static RequestExecutor fromEvaluator(Evaluator evaluator) {
        return (RequestExecutor) evaluator.getVariable(REQUEST_EXECUTOR_VARIABLE);
    }

    static RequestExecutor newExecutor(Evaluator evaluator) {
        RequestExecutor variable = new RequestExecutor();
        evaluator.setVariable(REQUEST_EXECUTOR_VARIABLE, variable);
        return variable;
    }

    RequestExecutor method(String method) {
        this.method = method;
        return this;
    }

    public RequestExecutor url(String url) {
        this.url = url;
        return this;
    }

    public RequestExecutor urlParams(String params) {
        this.urlParams = params;
        return this;
    }

    public RequestExecutor type(String type) {
        this.type = type;
        return this;
    }

    Response execute() {
        RequestSpecification request = given();
        request.headers(headers);

        if (body != null) {
            request.body(body);
        }

        if (type != null) {
            request.contentType(type);
        }

        if (cookies != null) {
            cookies = cookies.trim();
            if (cookies.startsWith("{")) {
                cookies = cookies.substring(1, cookies.length() - 1);
            }
            request.cookies(Splitter.on(",").trimResults().withKeyValueSeparator("=").split(cookies));
        }

        switch (method) {
            case "GET":
                response = request.get(requestUrlWithParams());
                break;
            case "POST":
                response = request.post(url);
                break;
            case "PUT":
                response = request.put(url);
                break;
            case "DELETE":
                response = request.delete(url);
                break;
            case "PATCH":
                response = request.patch(url);
                break;
            default:
                throw new UnsupportedOperationException(method);
        }
        return response;
    }

    public RequestExecutor header(String headerName, String headerValue) {
        headers.put(headerName, headerValue);
        return this;
    }

    public RequestExecutor header(Map<String, String> headersMap) {
        headers.clear();
        headers.putAll(headersMap);
        return this;
    }

    public RequestExecutor body(String body) {
        this.body = body;
        return this;
    }

    public String responseHeader(String attributeValue) {
        return response.getHeader(attributeValue);
    }

    public String statusLine() {
        return response.statusLine();
    }

    public int statusCode() {
        return response.getStatusCode();
    }

    public String responseBody() {
        return response.body().asString();
    }

    public String requestUrlWithParams() {
        return url + (urlParams != null ? "?" + urlParams : "");
    }

    public boolean hasRequestBody() {
        return method.equals("POST") || method.equals("PUT");
    }

    public String requestMethod() {
        return method;
    }

    public boolean isGET(){
        return "GET".equals(method);
    }

    public String requestHeader(String header) {
        return headers.get(header);
    }

    public RequestExecutor cookies(String cookies) {
        this.cookies = cookies;
        return this;
    }

    public String cookies() {
        return cookies;
    }
}