package com.adven.concordion.extensions.exam;

import com.jayway.restassured.RestAssured;

public class RestAssuredCfg {
    private final ExamExtension extension;

    RestAssuredCfg(ExamExtension extension) {
        this.extension = extension;
    }

    public RestAssuredCfg baseUri(String uri) {
        RestAssured.baseURI = uri;
        return this;
    }

    public RestAssuredCfg port(int port) {
        RestAssured.port = port;
        return this;
    }

    public RestAssuredCfg basePath(String context) {
        RestAssured.basePath = context;
        return this;
    }

    public ExamExtension end() {
        return extension;
    }
}
