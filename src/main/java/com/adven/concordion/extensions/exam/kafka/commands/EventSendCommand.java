package com.adven.concordion.extensions.exam.kafka.commands;

import com.adven.concordion.extensions.exam.html.Html;
import com.adven.concordion.extensions.exam.kafka.Event;
import com.adven.concordion.extensions.exam.kafka.EventProcessor;
import lombok.extern.slf4j.Slf4j;
import org.concordion.api.CommandCall;
import org.concordion.api.Evaluator;
import org.concordion.api.ResultRecorder;

/**
 * @author Ruslan Ustits
 */
@Slf4j
public final class EventSendCommand extends BaseEventCommand {

    public EventSendCommand(final String name, final String tag, final EventProcessor eventProcessor) {
        super(name, tag, eventProcessor);
    }

    @Override
    public void setUp(final CommandCall commandCall, final Evaluator evaluator, final ResultRecorder resultRecorder) {
        final Html html = new Html(commandCall.getElement());
        final String protobufClass = html.attr("protobufClass");
        final String topicName = html.attr("topicName");
        final String key = html.attr("key");
        final String message = html.text();
        final Event<String> event = Event.<String>builder()
                .topicName(topicName)
                .key(key)
                .message(message)
                .build();
    }

}
