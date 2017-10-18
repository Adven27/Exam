package com.adven.concordion.extensions.exam.rest.commands;

import com.adven.concordion.extensions.exam.rest.Method;

import static com.adven.concordion.extensions.exam.rest.Method.PUT;

public class PutCommand extends RequestCommand {
    public PutCommand(String name, String tag) {
        super(name, tag);
    }

    @Override
    protected Method method() {
        return PUT;
    }
}
