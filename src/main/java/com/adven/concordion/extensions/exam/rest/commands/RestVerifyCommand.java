package com.adven.concordion.extensions.exam.rest.commands;

import com.adven.concordion.extensions.exam.commands.ExamVerifyCommand;
import com.adven.concordion.extensions.exam.rest.JsonPrettyPrinter;
import com.adven.concordion.extensions.exam.rest.RestResultRenderer;

public class RestVerifyCommand extends ExamVerifyCommand {
    protected final JsonPrettyPrinter printer = new JsonPrettyPrinter();

    public RestVerifyCommand(String name, String tag) {
        super(name, tag, new RestResultRenderer());
    }
}