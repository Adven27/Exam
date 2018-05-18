package com.adven.concordion.extensions.exam.kafka.commands;

import com.adven.concordion.extensions.exam.commands.ExamCommand;
import com.adven.concordion.extensions.exam.html.Html;
import com.adven.concordion.extensions.exam.kafka.Event;
import com.adven.concordion.extensions.exam.kafka.EventProcessor;
import com.adven.concordion.extensions.exam.kafka.EventVerifier;
import org.concordion.api.CommandCall;
import org.concordion.api.Evaluator;
import org.concordion.api.Result;
import org.concordion.api.ResultRecorder;

public class EventCheckCommand extends ExamCommand {

    EventProcessor eventProcessor;
    EventVerifier eventVerifier;

    public EventCheckCommand(String name, String tag, EventProcessor eventProcessor) {
        super(name, tag);
        this.eventProcessor = eventProcessor;
    }


    /**
     * {@inheritDoc}.
     */
    public void verify(CommandCall commandCall, Evaluator evaluator, ResultRecorder resultRecorder) {

        Html eventReplyRoot = Html.tableSlim(commandCall.getElement());

        final String eventJson = eventReplyRoot.text();

        Event checkEvent = Event.builder().message(eventJson).build();

        final String topic = eventReplyRoot.takeAwayAttr("topic");

        // получаю ивент из очереди
        Event eventToCheck = eventProcessor.consume(topic);

        if (eventVerifier.verify(checkEvent, eventToCheck)) {

            if (eventProcessor.hasReply()) {
                // отправляю
                eventProcessor.reply();

            }

            resultRecorder.record(Result.SUCCESS);

        } else {

            resultRecorder.record(Result.FAILURE);
        }


    }
}
