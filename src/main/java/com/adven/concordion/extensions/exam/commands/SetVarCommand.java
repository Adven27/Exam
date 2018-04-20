package com.adven.concordion.extensions.exam.commands;

import com.adven.concordion.extensions.exam.PlaceholdersResolver;
import com.adven.concordion.extensions.exam.html.Html;
import org.concordion.api.CommandCall;
import org.concordion.api.Evaluator;
import org.concordion.api.ResultRecorder;

public class SetVarCommand extends ExamCommand {
    public SetVarCommand(String tag) {
        super("set", tag);
    }

    @Override
    public void setUp(CommandCall commandCall, Evaluator eval, ResultRecorder resultRecorder) {
        Html el = new Html(commandCall.getElement());
        eval.setVariable("#" + el.attr("var"), PlaceholdersResolver.INSTANCE.resolveToObj(el.attr("value"), eval));
    }
}