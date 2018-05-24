package com.adven.concordion.extensions.exam.kafka.commands;

import com.adven.concordion.extensions.exam.html.Html;
import com.adven.concordion.extensions.exam.kafka.Event;
import com.adven.concordion.extensions.exam.kafka.EventProcessor;
import org.concordion.api.CommandCall;
import org.concordion.api.Evaluator;
import org.concordion.api.Result;
import org.concordion.api.ResultRecorder;

public final class EventCheckReplyCommand extends BaseEventCommand {

    public EventCheckReplyCommand(final String name, final String tag, final EventProcessor eventProcessor) {
        super(name, tag, eventProcessor);
    }

    /**
     * {@inheritDoc}.
     */
    public void verify(CommandCall commandCall, Evaluator evaluator, ResultRecorder resultRecorder) {
        Html eventCheckReplyRoot = Html.tableSlim(commandCall.getElement());

        // получаю событие и класс, требующее проверки
        final Html expected = eventCheckReplyRoot.first("expected");
        final String expectedProtoClass = expected.takeAwayAttr("protobufClass");
        final String expectedTopicName = expected.takeAwayAttr("topicName");
        final String expectedEventJson = expected.text();
        Event<String> checkEvent = Event.<String>builder()
                .topicName(expectedTopicName)
                .message(expectedEventJson)
                .build();

        //final String topic = eventCheckReplyRoot.takeAwayAttr("topic");
        final Html reply = eventCheckReplyRoot.first("reply");
        // получаю класс события-ответа
        final String replyProtoClass = reply.takeAwayAttr("protobufClass");

        // получаю событие успешного ответа
        final Html replySuccess = reply.first("success");
        final String successReplyEventJson = replySuccess.text();
        Event<String> successReplyEvent = Event.<String>builder()
                .message(successReplyEventJson)
                .build();

        // получаю событие провального ответа
        final Html replyFail = reply.first("fail");
        final String failReplyEventJson = replyFail.text();
        Event<String> failReplyEvent = Event.<String>builder()
                .message(failReplyEventJson)
                .build();

        eventCheckReplyRoot.removeAllChild();

        // рисую результирующую таблицу
        final Html eventCheckInfo = eventInfo("Message expected", expectedTopicName, expectedProtoClass);
        final Html expEventTable = tableResult(expectedEventJson);

        eventCheckReplyRoot.childs(eventCheckInfo)
                .dropAllTo(expEventTable);

        final Html eventSuccessInfo = eventInfo("Success reply", "", replyProtoClass);
        final Html successEventTable = tableResult(successReplyEventJson);
        eventCheckReplyRoot.childs(eventSuccessInfo)
                .dropAllTo(successEventTable);

        final Html failSuccessInfo = eventInfo("Fails reply", "", replyProtoClass);
        final Html failEventTable = tableResult(failReplyEventJson);
        eventCheckReplyRoot.childs(failSuccessInfo)
                .dropAllTo(failEventTable);

        // произвожу проверку и ответ
        final boolean result = getEventProcessor().checkWithReply(checkEvent, expectedProtoClass, successReplyEvent, failReplyEvent, replyProtoClass, true);

        if (!result) {
            eventCheckReplyRoot.parent().attr("class", "")
                    .css("rest-failure bd-callout bd-callout-danger");
            eventCheckReplyRoot.text("Failed to send message to kafka");
            resultRecorder.record(Result.EXCEPTION);
        }
    }
}
