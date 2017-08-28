package com.adven.concordion.extensions.exam.rest.commands;

public class GetCommand extends RequestCommand {
    @Override
    protected String method() {
        return "GET";
    }
}