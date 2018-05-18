package com.adven.concordion.extensions.exam.kafka.commands;

import com.adven.concordion.extensions.exam.commands.ExamCommand;
import com.adven.concordion.extensions.exam.html.Html;

import static com.adven.concordion.extensions.exam.html.Html.table;
import static com.adven.concordion.extensions.exam.html.Html.th;
import static com.adven.concordion.extensions.exam.html.Html.thead;

public abstract class BaseEventCommand extends ExamCommand {

    public BaseEventCommand(String name, String tag) {
        super(name, tag);
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
