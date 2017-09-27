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
    public void setUp(CommandCall commandCall, Evaluator evaluator, ResultRecorder resultRecorder) {
        Html el = new Html(commandCall.getElement());
        String var = el.attr("var");
        evaluator.setVariable("#" + var, PlaceholdersResolver.resolveToObj(el.attr("value"), evaluator));

        el.parent().remove(el);
    }
}