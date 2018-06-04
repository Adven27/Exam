package com.adven.concordion.extensions.exam.kafka.commands;

import com.adven.concordion.extensions.exam.html.Html;
import com.adven.concordion.extensions.exam.kafka.Event;
import com.adven.concordion.extensions.exam.kafka.EventProcessor;
import com.adven.concordion.extensions.exam.kafka.protobuf.ProtoBlockParser;
import com.adven.concordion.extensions.exam.kafka.protobuf.ProtoEntity;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.concordion.api.CommandCall;
import org.concordion.api.Evaluator;
import org.concordion.api.Result;
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
        val root = new Html(commandCall.getElement());
        final String topicName = root.attr(TOPIC_NAME);
        final String key = root.attr(EVENT_KEY);
        val proto = new ProtoBlockParser().parse(root);
        root.removeAllChild();

        final boolean result;
        if (proto.isPresent()) {
            val message = proto.get();
            val info = buildProtoInfo(message, "Send message to", topicName);
            root.childs(info);
            val event = Event.<ProtoEntity>builder()
                    .topicName(topicName)
                    .key(key)
                    .message(message)
                    .build();
            result = getEventProcessor().send(event);
        } else {
            result = false;
        }

        if (!result) {
            root.parent().attr("class", "")
                    .css("rest-failure bd-callout bd-callout-danger");
            root.text("Failed to send message to kafka");
            resultRecorder.record(Result.EXCEPTION);
        }
    }

}
