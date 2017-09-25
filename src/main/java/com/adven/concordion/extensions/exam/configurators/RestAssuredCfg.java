package com.adven.concordion.extensions.exam.configurators;

import com.adven.concordion.extensions.exam.ExamExtension;
import com.jayway.restassured.RestAssured;

public class RestAssuredCfg {
    private final ExamExtension extension;
    private String uri;
    private String context;
    private Integer port;

    public RestAssuredCfg(ExamExtension extension) {
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

    private static void setUpRestAssured(String uri, String context, Integer port) {
        if (uri != null) {
            RestAssured.baseURI = uri;
        }
        if (context != null) {
            RestAssured.basePath = context;
        }
        if (port != null) {
            RestAssured.port = port;
        }
    }
}