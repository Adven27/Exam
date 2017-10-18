package com.adven.concordion.extensions.exam.rest.commands;

import com.adven.concordion.extensions.exam.rest.Method;

import static com.adven.concordion.extensions.exam.rest.Method.DELETE;

public class DeleteCommand extends RequestCommand {

    public DeleteCommand(String name, String tag) {
        super(name, tag);
    }

    @Override
    protected Method method() {
        return DELETE;
    }
}
