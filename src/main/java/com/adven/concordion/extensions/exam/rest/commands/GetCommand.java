package com.adven.concordion.extensions.exam.rest.commands;

import com.adven.concordion.extensions.exam.rest.Method;

import static com.adven.concordion.extensions.exam.rest.Method.GET;

public class GetCommand extends RequestCommand {
    public GetCommand(String name, String tag) {
        super(name, tag);
    }

    @Override
    protected Method method() {
        return GET;
    }
}