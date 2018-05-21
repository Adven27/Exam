package com.adven.concordion.extensions.exam.kafka.commands;

import com.adven.concordion.extensions.exam.commands.ExamCommand;
import com.adven.concordion.extensions.exam.html.Html;
import com.adven.concordion.extensions.exam.kafka.EventProcessor;
import lombok.AccessLevel;
import lombok.Getter;

import static com.adven.concordion.extensions.exam.html.Html.*;

abstract class BaseEventCommand extends ExamCommand {

    @Getter(AccessLevel.PROTECTED)
    private final EventProcessor eventProcessor;

    public BaseEventCommand(final String name, final String tag, final EventProcessor eventProcessor) {
        super(name, tag);
        this.eventProcessor = eventProcessor;
    }

    protected final Html eventTable() {
        final Html table = table();
        final Html header = thead();
        final Html tr = Html.tr();
        tr.childs(
                th("Event header"),
                th("Event body")
        );
        return table.childs(header.childs(tr));
    }

}
