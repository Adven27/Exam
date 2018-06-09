package com.adven.concordion.extensions.exam.kafka.commands;

import com.adven.concordion.extensions.exam.html.Html;
import com.adven.concordion.extensions.exam.kafka.EventBlockParser;
import com.adven.concordion.extensions.exam.kafka.EventProcessor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.concordion.api.CommandCall;
import org.concordion.api.Evaluator;
import org.concordion.api.Result;
import org.concordion.api.ResultRecorder;

@Slf4j
public final class EventSendCommand extends BaseEventCommand {

    public EventSendCommand(final String name, final String tag, final EventProcessor eventProcessor) {
        super(name, tag, eventProcessor);
    }

    @Override
    public void setUp(final CommandCall commandCall, final Evaluator evaluator, final ResultRecorder resultRecorder) {
        val root = new Html(commandCall.getElement());
        val parsedEvent = new EventBlockParser().parse(root);
        root.removeAllChild();

        final boolean result;
        if (parsedEvent.isPresent()) {
            val event = parsedEvent.get();
            val info = buildInfo(event, "Send message to");
            root.childs(info);
            result = getEventProcessor().send(event);
        } else {
            result = false;
        }
        if (!result) {
            root.parent().attr("class", "").css("rest-failure bd-callout bd-callout-danger");
            root.text("Failed to send message to kafka");
            resultRecorder.record(Result.EXCEPTION);
        }
    }

}
