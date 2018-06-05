package com.adven.concordion.extensions.exam.kafka.commands;

import com.adven.concordion.extensions.exam.html.Html;
import com.adven.concordion.extensions.exam.kafka.Event;
import com.adven.concordion.extensions.exam.kafka.EventProcessor;
import com.adven.concordion.extensions.exam.kafka.protobuf.ProtoBlockParser;
import com.adven.concordion.extensions.exam.kafka.protobuf.ProtoEntity;
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
        val expectedBlock = root.firstOrThrow("expected");
        val expectedTopic = expectedBlock.takeAwayAttr(TOPIC_NAME);
        val expectedProto = new ProtoBlockParser().parse(expectedBlock);
        final boolean result;
        if (expectedProto.isPresent()) {
            result = check(root, expectedProto.get(), expectedTopic);
        } else {
            result = false;
        }
        if (!result) {
            root.parent().attr("class", "").css("rest-failure bd-callout bd-callout-danger");
            root.text("Failed to start kafka listener mock");
            resultRecorder.record(Result.EXCEPTION);
        }
    }

    private boolean check(final Html root, final ProtoEntity expected, final String topic) {
        val checkEvent = Event.<ProtoEntity>builder()
            .topicName(topic)
            .message(expected)
            .build();
        val reply = root.first("reply");
        root.removeAllChild();

        val eventCheckInfo = buildProtoInfo(expected, "Expected event", topic);
        root.childs(eventCheckInfo);

        final boolean result;
        if (reply != null) {
            result = withReply(reply, checkEvent);
        } else {
            result = getEventProcessor().check(checkEvent, false);
        }
        return result;
    }

    private boolean withReply(final Html reply, final Event<ProtoEntity> checkEvent) {
        val protoParser = new ProtoBlockParser();
        val successProto = protoParser.parse(reply.firstOrThrow("success"));
        val failProto = protoParser.parse(reply.firstOrThrow("fail"));
        final boolean result;
        if (successProto.isPresent() && failProto.isPresent()) {
            val successReplyEvent = Event.<ProtoEntity>builder().message(successProto.get()).build();
            val failReplyEvent = Event.<ProtoEntity>builder().message(failProto.get()).build();
            val successEventInfo = buildProtoInfo(successProto.get(), "Success reply", "");
            val failEventInfo = buildProtoInfo(failProto.get(), "Fail reply", "");
            reply.parent().childs(successEventInfo, failEventInfo);

            result = getEventProcessor().checkWithReply(checkEvent, successReplyEvent, failReplyEvent, true);
        } else {
            result = false;
        }
        return result;
    }

}
