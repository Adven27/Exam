package com.adven.concordion.extensions.exam.rest.commands;

import com.jayway.restassured.http.Method;

import static com.jayway.restassured.http.Method.POST;

public class PostCommand extends RequestCommand {
    public PostCommand(String name, String tag) {
        super(name, tag);
    }

    @Override
    protected Method method() {
        return POST;
    }
}