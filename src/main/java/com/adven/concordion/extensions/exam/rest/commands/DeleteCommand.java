package com.adven.concordion.extensions.exam.rest.commands;

import com.jayway.restassured.http.Method;

import static com.jayway.restassured.http.Method.DELETE;

public class DeleteCommand extends RequestCommand {

    public DeleteCommand(String name, String tag) {
        super(name, tag);
    }

    @Override
    protected Method method() {
        return DELETE;
    }
}
