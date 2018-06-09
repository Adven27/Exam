package com.adven.concordion.extensions.exam.kafka.commands;

import com.adven.concordion.extensions.exam.html.Html;
import com.adven.concordion.extensions.exam.kafka.Entity;
import com.adven.concordion.extensions.exam.kafka.Event;
import com.adven.concordion.extensions.exam.kafka.EventBlockParser;
import com.adven.concordion.extensions.exam.kafka.EventProcessor;
import lombok.val;
import org.concordion.api.CommandCall;
import org.concordion.api.Evaluator;
import org.concordion.api.Result;
import org.concordion.api.ResultRecorder;

import static com.adven.concordion.extensions.exam.html.HtmlBuilder.tableSlim;

public final class EventCheckReplyCommand extends BaseEventCommand {

    public EventCheckReplyCommand(final String name, final String tag, final EventProcessor eventProcessor) {
        super(name, tag, eventProcessor);
    }

    @Override
    public void verify(CommandCall commandCall, Evaluator evaluator, ResultRecorder resultRecorder) {
        val root = tableSlim(commandCall.getElement());
        val expectedProto = new EventBlockParser("expected").parse(root);
        final boolean result;
        if (expectedProto.isPresent()) {
            result = check(root, expectedProto.get());
        } else {
            result = false;
        }
        if (!result) {
            root.parent().attr("class", "").css("rest-failure bd-callout bd-callout-danger");
            root.text("Failed to start kafka listener mock");
            resultRecorder.record(Result.EXCEPTION);
        }
    }

    private boolean check(final Html root, final Event<Entity> expected) {
        val reply = root.first("reply");
        root.removeAllChild();

        val eventCheckInfo = buildInfo(expected, "Expected event");
        root.childs(eventCheckInfo);

        final boolean result;
        if (reply != null) {
            result = withReply(reply, expected);
        } else {
            result = getEventProcessor().check(expected, false);
        }
        return result;
    }

    private boolean withReply(final Html reply, final Event<Entity> checkEvent) {
        val successEvent = new EventBlockParser("success").parse(reply);
        val failEvent = new EventBlockParser("fail").parse(reply);
        final boolean result;
        if (successEvent.isPresent() && failEvent.isPresent()) {
            val successEventInfo = buildInfo(successEvent.get(), "Success reply");
            val failEventInfo = buildInfo(failEvent.get(), "Fail reply");
            reply.parent().childs(successEventInfo, failEventInfo);
            result = getEventProcessor().checkWithReply(checkEvent, successEvent.get(), failEvent.get(), true);
        } else {
            result = false;
        }
        return result;
    }

}
