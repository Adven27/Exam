package com.adven.concordion.extensions.exam.kafka.commands;

import com.adven.concordion.extensions.exam.commands.ExamCommand;
import com.adven.concordion.extensions.exam.html.Html;
import com.adven.concordion.extensions.exam.kafka.EventProcessor;
import com.adven.concordion.extensions.exam.rest.JsonPrettyPrinter;
import lombok.AccessLevel;
import lombok.Getter;

import static com.adven.concordion.extensions.exam.html.Html.*;

abstract class BaseEventCommand extends ExamCommand {

    protected static final String PROTO_CLASS = "protobufClass";
    protected static final String TOPIC_NAME = "topicName";
    protected static final String EVENT_KEY = "key";

    @Getter(AccessLevel.PROTECTED)
    private final EventProcessor eventProcessor;

    public BaseEventCommand(final String name, final String tag, final EventProcessor eventProcessor) {
        super(name, tag);
        this.eventProcessor = eventProcessor;
    }

    protected final Html eventTable() {
        final Html table = Html.Companion.table();
        final Html header = Companion.thead();
        final Html tr = Html.Companion.tr();
        tr.childs(
                Companion.th("Event header"),
                Companion.th("Event body")
        );
        return table.childs(header.childs(tr));
    }

    protected Html eventInfo(String text, final String topicName, final String protobufClass) {
        return Companion.div().childs(
                Companion.h(4, text),
                Companion.h(5, "").childs(
                        Companion.badge(topicName, "primary"),
                        Companion.badge(protobufClass, "secondary"),
                        Companion.code("protobuf")));
    }

    protected Html tableResult(final String message) {
        return tableResult("", message);
    }


    protected Html tableResult(final String header, final String message) {
        final Html table = eventTable();
        final JsonPrettyPrinter printer = new JsonPrettyPrinter();
        table.childs(
                Companion.tbody().childs(
                        Companion.td(Companion.code(header)),
                        Companion.td(printer.prettyPrint(message)).css("json")));
        return table;
    }
}
