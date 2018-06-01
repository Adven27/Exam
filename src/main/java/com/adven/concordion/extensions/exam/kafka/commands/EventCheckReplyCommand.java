package com.adven.concordion.extensions.exam.kafka.commands;

import com.adven.concordion.extensions.exam.html.Html;
import com.adven.concordion.extensions.exam.kafka.Event;
import com.adven.concordion.extensions.exam.kafka.EventProcessor;
import org.concordion.api.CommandCall;
import org.concordion.api.Evaluator;
import org.concordion.api.Result;
import org.concordion.api.ResultRecorder;

import static com.adven.concordion.extensions.exam.html.Html.tableSlim;

public final class EventCheckReplyCommand extends BaseEventCommand {

    public EventCheckReplyCommand(final String name, final String tag, final EventProcessor eventProcessor) {
        super(name, tag, eventProcessor);
    }

    /**
     * {@inheritDoc}.
     */
    public void verify(CommandCall commandCall, Evaluator evaluator, ResultRecorder resultRecorder) {
        Html eventCheckReplyRoot = tableSlim(commandCall.getElement());
        // получаю событие и класс, требующее проверки
        final Html expected = eventCheckReplyRoot.firstOrThrow("expected");
        final String expectedProtoClass = expected.takeAwayAttr(PROTO_CLASS);
        final String expectedTopicName = expected.takeAwayAttr(TOPIC_NAME);
        final String expectedEventJson = expected.text();
        final Event<String> checkEvent = Event.<String>builder()
            .topicName(expectedTopicName)
            .message(expectedEventJson)
            .build();

        final Html reply = eventCheckReplyRoot.firstOrThrow("reply");
        // получаю класс события-ответа
        //FIXME WHAT IF NULL?
        final String replyProtoClass = reply.takeAwayAttr(PROTO_CLASS, "WHAT IF NULL?");

        // получаю событие успешного ответа
        final Html replySuccess = reply.firstOrThrow("success");
        final String successReplyEventJson = replySuccess.text();
        final Event<String> successReplyEvent = Event.<String>builder().message(successReplyEventJson).build();

        // получаю событие провального ответа
        final Html replyFail = reply.firstOrThrow("fail");
        final String failReplyEventJson = replyFail.text();
        final Event<String> failReplyEvent = Event.<String>builder().message(failReplyEventJson).build();

        eventCheckReplyRoot.removeAllChild();

        // рисую результирующую таблицу
        final Html eventCheckInfo = eventInfo("Expected event", expectedTopicName, expectedProtoClass);
        final Html expEventTable = tableResult(expectedEventJson);
        eventCheckInfo.dropAllTo(expEventTable);

        final Html eventSuccessInfo = eventInfo("Success reply", "", replyProtoClass);
        final Html successEventTable = tableResult(successReplyEventJson);
        eventSuccessInfo.dropAllTo(successEventTable);

        final Html failSuccessInfo = eventInfo("Fail reply", "", replyProtoClass);
        final Html failEventTable = tableResult(failReplyEventJson);
        failSuccessInfo.dropAllTo(failEventTable);

        eventCheckReplyRoot.childs(eventCheckInfo, eventSuccessInfo, failSuccessInfo);

        // произвожу проверку и ответ
        final boolean result = getEventProcessor().checkWithReply(
            checkEvent, expectedProtoClass, successReplyEvent, failReplyEvent, replyProtoClass, true);

        if (!result) {
            eventCheckReplyRoot.parent().attr("class", "").css("rest-failure bd-callout bd-callout-danger");
            eventCheckReplyRoot.text("Failed to start kafka listener mock");
            resultRecorder.record(Result.EXCEPTION);
        }
    }
}
