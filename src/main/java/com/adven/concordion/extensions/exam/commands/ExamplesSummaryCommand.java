package com.adven.concordion.extensions.exam.commands;

import com.adven.concordion.extensions.exam.html.Html;
import org.concordion.api.*;

import static com.google.common.base.Strings.isNullOrEmpty;

public class ExamplesSummaryCommand extends AbstractCommand {
    @Override
    public void setUp(CommandCall cmd, Evaluator evaluator, ResultRecorder resultRecorder) {
        Html el = new Html(cmd.getElement()).attr("id", "summary");
        final String title = el.takeAwayAttr("title", evaluator);
        if (!isNullOrEmpty(title)) {
            el.childs(Html.h4(title));
        }
    }
}