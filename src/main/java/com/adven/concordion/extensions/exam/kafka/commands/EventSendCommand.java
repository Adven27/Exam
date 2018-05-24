package com.adven.concordion.extensions.exam.kafka.commands;

import com.adven.concordion.extensions.exam.html.Html;
import com.adven.concordion.extensions.exam.kafka.Event;
import com.adven.concordion.extensions.exam.kafka.EventProcessor;
import lombok.extern.slf4j.Slf4j;
import org.concordion.api.CommandCall;
import org.concordion.api.Evaluator;
import org.concordion.api.Result;
import org.concordion.api.ResultRecorder;

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
        final Html eventInfo = eventInfo("Send message to", topicName, protobufClass);
        final Html eventTable = tableResult(key, message);

        html.childs(eventInfo)
                .dropAllTo(eventTable);

        final Event<String> event = Event.<String>builder()
                .topicName(topicName)
                .key(key)
                .message(message)
                .build();
        final boolean result = getEventProcessor().send(event, protobufClass);
        if (!result) {
            html.parent().attr("class", "")
                    .css("rest-failure bd-callout bd-callout-danger");
            html.text("Failed to send message to kafka");
            resultRecorder.record(Result.EXCEPTION);
        }
    }
}
