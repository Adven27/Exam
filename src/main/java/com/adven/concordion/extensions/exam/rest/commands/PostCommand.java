package com.adven.concordion.extensions.exam.rest.commands;

public class PostCommand extends RequestCommand {
    public PostCommand(String name, String tag) {
        super(name, tag);
    }

    @Override
    protected String method() {
        return "POST";
    }
}