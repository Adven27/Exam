package com.adven.concordion.extensions.exam.rest.commands;

import com.jayway.restassured.http.Method;

import static com.jayway.restassured.http.Method.GET;

public class GetCommand extends RequestCommand {
    public GetCommand(String name, String tag) {
        super(name, tag);
    }

    @Override
    protected Method method() {
        return GET;
    }
}