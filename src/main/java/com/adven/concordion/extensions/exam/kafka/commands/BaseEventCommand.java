package com.adven.concordion.extensions.exam.kafka.commands;

import com.adven.concordion.extensions.exam.commands.ExamCommand;
import com.adven.concordion.extensions.exam.html.Html;
import com.adven.concordion.extensions.exam.html.HtmlBuilder;
import com.adven.concordion.extensions.exam.kafka.Event;
import com.adven.concordion.extensions.exam.kafka.EventProcessor;
import com.adven.concordion.extensions.exam.kafka.protobuf.ProtoEntity;
import com.adven.concordion.extensions.exam.rest.JsonPrettyPrinter;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import java.io.UnsupportedEncodingException;
import java.util.*;

import static com.adven.concordion.extensions.exam.html.HtmlBuilder.*;

@Slf4j
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

    protected Html tableResult(final String message, final String... headers) {
        final Html table = eventTable();
        final JsonPrettyPrinter printer = new JsonPrettyPrinter();
        val headerColumn = td();
        for (val header : headers) {
            headerColumn.childs(
                HtmlBuilder.tag("dd").childs(
                    code(header)));
        }
        table.childs(
            tbody().childs(
                headerColumn,
                td(printer.prettyPrint(message)).css("json")));
        return table;
    }

    protected Html buildProtoInfo(final Event<ProtoEntity> event, final String infoHeader) {
        final Map<String, String> headers = new HashMap<>();
        if (event.getKey() != null) {
            headers.put(EVENT_KEY, event.getKey());
        }
        if (event.getHeader() != null) {
            val eventHeader = event.getHeader();
            if (eventHeader.getReplyToTopic().length > 0) {
                headers.put("replyTopic", bytesToString(eventHeader.getReplyToTopic()));
            }
            if (eventHeader.getCorrelationId().length > 0) {
                headers.put("correlationId", bytesToString(eventHeader.getCorrelationId()));
            }
        }
        return buildProtoInfo(event.getMessage(), infoHeader, event.getTopicName(), headers);
    }

    private String bytesToString(final byte[] bytes) {
        try {
            return new String(bytes, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            log.error("Wrong encoding", e);
        }
        return "";
    }

    protected Html buildProtoInfo(final ProtoEntity proto, final String header, final String topicName) {
        return buildProtoInfo(proto, header, topicName, Collections.<String, String>emptyMap());
    }

    private Html buildProtoInfo(final ProtoEntity proto, final String header, final String topicName,
                                final Map<String, String> eventHeaders) {
        val info = eventInfo(header, topicName, proto.getClassName());
        final List<String> headers = new ArrayList<>();
        for (val entry : eventHeaders.entrySet()) {
            headers.add(entry.getKey() + "=" + entry.getValue());
        }
        val table = tableResult(proto.getJsonValue(), headers.toArray(new String[]{}));
        info.dropAllTo(table);
        return info;
    }

}
