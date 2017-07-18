package com.sberbank.pfm.test.concordion.extensions.exam.commands;

import com.sberbank.pfm.test.concordion.extensions.exam.html.Html;
import org.concordion.api.*;

import static com.google.common.base.Strings.isNullOrEmpty;
import static com.sberbank.pfm.test.concordion.extensions.exam.html.Html.h4;

public class ExamplesSummaryCommand extends AbstractCommand {
    @Override
    public void setUp(CommandCall cmd, Evaluator evaluator, ResultRecorder resultRecorder) {
        Html el = new Html(cmd.getElement()).attr("id", "summary");
        final String title = el.takeAwayAttr("title");
        if (!isNullOrEmpty(title)) {
            el.childs(h4(title));
        }
    }
}