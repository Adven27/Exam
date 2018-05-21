package com.adven.concordion.extensions.exam.kafka.commands;

import com.adven.concordion.extensions.exam.html.Html;
import com.adven.concordion.extensions.exam.kafka.Event;
import com.adven.concordion.extensions.exam.kafka.EventProcessor;
import com.adven.concordion.extensions.exam.rest.JsonPrettyPrinter;
import lombok.extern.slf4j.Slf4j;
import org.concordion.api.CommandCall;
import org.concordion.api.Evaluator;
import org.concordion.api.Result;
import org.concordion.api.ResultRecorder;

import static com.adven.concordion.extensions.exam.html.Html.*;

/**
 * @author Ruslan Ustits
 */
@Slf4j
public final class EventSendCommand extends BaseEventCommand {

    private static final String PROTO_CLASS = "protobufClass";
    private static final String TOPIC_NAME = "topicName";
    private static final String EVENT_KEY = "key";

    public EventSendCommand(final String name, final String tag, final EventProcessor eventProcessor) {
        super(name, tag, eventProcessor);
    }

    @Override
    public void setUp(final CommandCall commandCall, final Evaluator evaluator, final ResultRecorder resultRecorder) {
        final Html html = new Html(commandCall.getElement());
        final String protobufClass = html.attr(PROTO_CLASS);
        final String topicName = html.attr(TOPIC_NAME);
        final String key = html.attr(EVENT_KEY);
        final String message = html.text();

        html.removeAllChild();
        final Html eventInfo = eventInfo(topicName, protobufClass);
        final Html eventTable = tableResult(key, message);

        html.childs(eventInfo)
                .dropAllTo(eventTable);

        final Event<String> event = Event.<String>builder()
                .topicName(topicName)
                .key(key)
                .message(message)
                .build();
        //        final boolean result = getEventProcessor().send(event, protobufClass);
        final boolean result = true;
        if (!result) {
            html.parent().attr("class", "")
                    .css("rest-failure bd-callout bd-callout-danger");
            html.text("Failed to send message to kafka");
            resultRecorder.record(Result.EXCEPTION);
        }
    }

    private Html eventInfo(final String topicName, final String protobufClass) {
        return div().childs(
                h(4, "Send message to"),
                h(5, "").childs(
                        badge(topicName, "primary"),
                        badge(protobufClass, "secondary"),
                        code("protobuf")));
    }

    private Html tableResult(final String key, final String message) {
        final Html table = eventTable();
        final String header = "key=" + key;
        final JsonPrettyPrinter printer = new JsonPrettyPrinter();
        table.childs(
                tbody().childs(
                        td(code(header)),
                        td(printer.prettyPrint(message)).css("json")));
        return table;
    }

}
