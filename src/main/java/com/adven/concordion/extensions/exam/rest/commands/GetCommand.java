package com.adven.concordion.extensions.exam.rest.commands;

public class GetCommand extends RequestCommand {
    public GetCommand(String name, String tag) {
        super(name, tag);
    }

    @Override
    protected String method() {
        return "GET";
    }
}