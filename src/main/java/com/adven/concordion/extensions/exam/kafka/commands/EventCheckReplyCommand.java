package com.adven.concordion.extensions.exam.kafka.commands;

import com.adven.concordion.extensions.exam.html.Html;
import com.adven.concordion.extensions.exam.kafka.Event;
import com.adven.concordion.extensions.exam.kafka.EventProcessor;
import com.adven.concordion.extensions.exam.kafka.EventVerifier;
import org.concordion.api.CommandCall;
import org.concordion.api.Evaluator;
import org.concordion.api.ResultRecorder;

public final class EventCheckReplyCommand extends BaseEventCommand {

    private EventVerifier eventVerifier;

    public EventCheckReplyCommand(final String name, final String tag, final EventProcessor eventProcessor) {
        super(name, tag, eventProcessor);
    }

    @Override
    public void setUp(final CommandCall commandCall, final Evaluator evaluator, final ResultRecorder resultRecorder) {
        Html eventReplyRoot = Html.tableSlim(commandCall.getElement());
        final String eventJson = eventReplyRoot.text();
        final Event<String> replyEvent = Event.<String>builder()
                .message(eventJson)
                .build();
    }

    /**
     * {@inheritDoc}.
     */
    public void verify(CommandCall commandCall, Evaluator evaluator, ResultRecorder resultRecorder) {
        Html eventCheckReplyRoot = Html.tableSlim(commandCall.getElement());
        // получаю событие и класс, требующее проверки
        final Html expected = eventCheckReplyRoot.first("expected");
        final String expectedProtoClass = expected.takeAwayAttr("protobufClass");
        final String expectedEventJson = expected.text();
        Event<String> checkEvent = Event.<String>builder()
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
        // произвожу проверку и ответ
        final boolean result = getEventProcessor().checkWithReply(checkEvent, expectedProtoClass, successReplyEvent, failReplyEvent, replyProtoClass, true);
        // рисую результирующую таблицу
    }

}
