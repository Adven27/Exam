package com.adven.concordion.extensions.exam.kafka.check;

import com.adven.concordion.extensions.exam.kafka.Event;
import com.adven.concordion.extensions.exam.kafka.EventProducer;
import com.google.protobuf.Message;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;


@Slf4j
@RequiredArgsConstructor
public final class WithReply implements CheckMessageMock {

    @Getter(AccessLevel.PROTECTED)
    private final Event<Message> replyEvent;
    @Getter(AccessLevel.PROTECTED)
    private final Event<Message> failEvent;
    private final EventProducer eventProducer;
    private final CheckMessageMock checkMessageMock;

    @Override
    public boolean verify() {
        if (checkMessageMock.verify()) {
            return send(replyEvent);
        } else {
            return send(failEvent);
        }
    }

    protected boolean send(final Event<Message> event) {
        return send(event.getTopicName(), event);
    }

    protected boolean send(final String topicName, final Event<Message> event) {
        return eventProducer.produce(topicName, event.getKey(), event.getHeader(), event.getMessage());
    }

}
