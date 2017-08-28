package com.adven.concordion.extensions.exam.rest.commands;

public class PostCommand extends RequestCommand {
    @Override
    protected String method() {
        return "POST";
    }
}