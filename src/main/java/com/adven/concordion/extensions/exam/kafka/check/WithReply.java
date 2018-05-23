package com.adven.concordion.extensions.exam.kafka.check;

import com.adven.concordion.extensions.exam.kafka.Event;
import com.adven.concordion.extensions.exam.kafka.EventProducer;
import com.google.protobuf.Message;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Ruslan Ustits
 */
@Slf4j
@RequiredArgsConstructor
public final class WithReply implements CheckMessageMock {

    private final Event<Message> replyEvent;
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

    private boolean send(final Event<Message> event) {
        return eventProducer.produce(event.getTopicName(), event.getKey(), event.getMessage());
    }

}
