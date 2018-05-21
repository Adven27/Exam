package com.adven.concordion.extensions.exam.kafka.commands;

import com.adven.concordion.extensions.exam.commands.ExamCommand;
import com.adven.concordion.extensions.exam.html.Html;
import com.adven.concordion.extensions.exam.kafka.EventProcessor;
import lombok.AccessLevel;
import lombok.Getter;

import static com.adven.concordion.extensions.exam.html.Html.table;
import static com.adven.concordion.extensions.exam.html.Html.th;
import static com.adven.concordion.extensions.exam.html.Html.thead;

abstract class BaseEventCommand extends ExamCommand {

    @Getter(AccessLevel.PROTECTED)
    private final EventProcessor eventProcessor;

    public BaseEventCommand(final String name, final String tag, final EventProcessor eventProcessor) {
        super(name, tag);
        this.eventProcessor = eventProcessor;
    }

    protected void drawEventTable(Html html, String eventJson) {
        Html table = table();
        Html header = thead();
        Html tr = Html.tr().text(eventJson);

        tr.childs(
                th("Event header"),
                th("Event json")
        );
        table.childs(header.childs(tr));
        html.dropAllTo(table);
    }
}
