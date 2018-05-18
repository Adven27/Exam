package com.adven.concordion.extensions.exam.kafka.commands;

import com.adven.concordion.extensions.exam.commands.ExamCommand;
import com.adven.concordion.extensions.exam.html.Html;
import com.adven.concordion.extensions.exam.kafka.Event;
import com.adven.concordion.extensions.exam.kafka.EventProcessor;
import org.concordion.api.CommandCall;
import org.concordion.api.Evaluator;
import org.concordion.api.ResultRecorder;

import static com.adven.concordion.extensions.exam.html.Html.codeXml;
import static com.adven.concordion.extensions.exam.html.Html.span;
import static com.adven.concordion.extensions.exam.html.Html.trWithTDs;

public class EventReplyCommand extends BaseEventCommand {

    EventProcessor eventProcessor;

    public EventReplyCommand(String name, String tag, EventProcessor eventProcessor) {
        super(name, tag);
        this.eventProcessor = eventProcessor;
    }

    @Override
    public void setUp(CommandCall commandCall, Evaluator evaluator, ResultRecorder resultRecorder) {

        Html eventReplyRoot = Html.tableSlim(commandCall.getElement());

        final String protoClass = eventReplyRoot.takeAwayAttr("protobufClass");

        final String eventJson = eventReplyRoot.text();

        Event replyEvent = Event.builder().message(eventJson).build();

        eventProcessor.configureReply(replyEvent, protoClass);

        drawEventTable(eventReplyRoot, eventJson);
    }
}
