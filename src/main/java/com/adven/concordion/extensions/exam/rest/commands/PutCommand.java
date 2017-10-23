package com.adven.concordion.extensions.exam.rest.commands;

import com.jayway.restassured.http.Method;

import static com.jayway.restassured.http.Method.PUT;

public class PutCommand extends RequestCommand {
    public PutCommand(String name, String tag) {
        super(name, tag);
    }

    @Override
    protected Method method() {
        return PUT;
    }
}