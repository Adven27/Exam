package com.adven.concordion.extensions.exam.commands;

import com.adven.concordion.extensions.exam.html.Html;
import org.concordion.api.CommandCall;
import org.concordion.api.Evaluator;
import org.concordion.api.ResultRecorder;

import static com.adven.concordion.extensions.exam.html.Html.button;
import static com.adven.concordion.extensions.exam.html.Html.italic;

public class ScrollToTopCommand extends ExamCommand {
    public ScrollToTopCommand(String name, String tag) {
        super(name, tag);
    }

    @Override
    public void setUp(CommandCall cmd, Evaluator evaluator, ResultRecorder resultRecorder) {
        Html el = new Html(cmd.getElement());
        el.childs(button("").attr("id", "btnToTop").attr("onclick", "topFunction()").childs(
                italic("").css("fa fa-arrow-up fa-3x")
        ));
    }
}
