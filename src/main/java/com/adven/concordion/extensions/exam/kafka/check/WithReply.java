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
    private final EventProducer eventProducer;
    private final CheckMessageMock checkMessageMock;

    @Override
    public boolean verify() {
        return checkMessageMock.verify() &&
                eventProducer.produce(replyEvent.getTopicName(), replyEvent.getKey(), replyEvent.getMessage());
    }

}
