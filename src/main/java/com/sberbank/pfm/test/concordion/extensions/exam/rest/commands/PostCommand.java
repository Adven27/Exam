package com.sberbank.pfm.test.concordion.extensions.exam.rest.commands;

public class PostCommand extends RequestCommand {
    @Override
    protected String method() {
        return "POST";
    }
}