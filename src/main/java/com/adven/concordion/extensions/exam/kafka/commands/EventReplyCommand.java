package com.adven.concordion.extensions.exam.kafka.commands;

import com.adven.concordion.extensions.exam.html.Html;
import com.adven.concordion.extensions.exam.kafka.Event;
import com.adven.concordion.extensions.exam.kafka.EventProcessor;
import org.concordion.api.CommandCall;
import org.concordion.api.Evaluator;
import org.concordion.api.ResultRecorder;

public final class EventReplyCommand extends BaseEventCommand {

    public EventReplyCommand(final String name, final String tag, final EventProcessor eventProcessor) {
        super(name, tag, eventProcessor);
    }

    @Override
    public void setUp(final CommandCall commandCall, final Evaluator evaluator, final ResultRecorder resultRecorder) {
        Html eventReplyRoot = Html.tableSlim(commandCall.getElement());
        final String protoClass = eventReplyRoot.takeAwayAttr("protobufClass");
        final String eventJson = eventReplyRoot.text();
        final Event<String> replyEvent = Event.<String>builder()
                .message(eventJson)
                .build();
        getEventProcessor().configureReply(replyEvent, protoClass);
    }

}
