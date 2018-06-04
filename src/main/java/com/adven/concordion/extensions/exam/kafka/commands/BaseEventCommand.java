package com.adven.concordion.extensions.exam.kafka.commands;

import com.adven.concordion.extensions.exam.commands.ExamCommand;
import com.adven.concordion.extensions.exam.html.Html;
import com.adven.concordion.extensions.exam.kafka.EventProcessor;
import com.adven.concordion.extensions.exam.kafka.protobuf.ProtoEntity;
import com.adven.concordion.extensions.exam.rest.JsonPrettyPrinter;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.val;

import static com.adven.concordion.extensions.exam.html.HtmlBuilder.*;

abstract class BaseEventCommand extends ExamCommand {

    protected static final String TOPIC_NAME = "topicName";
    protected static final String EVENT_KEY = "key";

    @Getter(AccessLevel.PROTECTED)
    private final EventProcessor eventProcessor;

    public BaseEventCommand(final String name, final String tag, final EventProcessor eventProcessor) {
        super(name, tag);
        this.eventProcessor = eventProcessor;
    }

    protected final Html eventTable() {
        final Html table = table();
        final Html header = thead();
        final Html tr = tr();
        tr.childs(
            th("Event header"),
            th("Event body")
        );
        return table.childs(header.childs(tr));
    }

    protected Html eventInfo(String text, final String topicName, final String protobufClass) {
        return div().childs(
            h(4, text),
            h(5, "").childs(
                badge(topicName, "primary"),
                badge(protobufClass, "secondary"),
                code("protobuf")));
    }

    protected Html tableResult(final String message) {
        return tableResult("", message);
    }


    protected Html tableResult(final String header, final String message) {
        final Html table = eventTable();
        final JsonPrettyPrinter printer = new JsonPrettyPrinter();
        table.childs(
            tbody().childs(
                td().childs(code(header)),
                td(printer.prettyPrint(message)).css("json")));
        return table;
    }

    protected Html buildProtoInfo(final ProtoEntity proto, final String header, final String topicName) {
        val info = eventInfo(header, topicName, proto.getClassName());
        val table = tableResult(proto.getJsonValue());
        info.dropAllTo(table);
        return info;
    }

}
