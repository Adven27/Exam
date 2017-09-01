package com.adven.concordion.extensions.exam;

import com.jayway.restassured.RestAssured;

public class RestAssuredCfg {
    private final ExamExtension extension;
    private String uri;
    private String context;
    private int port;

    RestAssuredCfg(ExamExtension extension) {
        this.extension = extension;
    }

    public RestAssuredCfg baseUri(String uri) {
        this.uri = uri;
        return this;
    }

    public RestAssuredCfg port(int port) {
        this.port = port;
        return this;
    }

    public RestAssuredCfg basePath(String context) {
        this.context = context;
        return this;
    }

    public ExamExtension end() {
        setUpRestAssured(uri, context, port);
        return extension;
    }

    private static void setUpRestAssured(String uri, String context, int port) {
        RestAssured.baseURI = uri;
        RestAssured.basePath = context;
        RestAssured.port = port;
    }
}
