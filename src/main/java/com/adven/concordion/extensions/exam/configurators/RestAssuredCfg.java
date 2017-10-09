package com.adven.concordion.extensions.exam.configurators;

import com.adven.concordion.extensions.exam.ExamExtension;
import com.jayway.restassured.RestAssured;

import java.net.URI;
import java.net.URISyntaxException;

public class RestAssuredCfg {
    private final ExamExtension extension;
    private String uri;
    private String context;
    private Integer port;

    public RestAssuredCfg(ExamExtension extension) {
        this(extension, null);
    }

    public RestAssuredCfg(ExamExtension extension, String url) {
        if (url != null) {
            URI uri = fromUri(url);
            this.uri = "http://" + uri.getHost();
            this.port = uri.getPort();
            this.context = uri.getPath();
        }
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

    private URI fromUri(String url) {
        try {
            return new URI(url);
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }
}
