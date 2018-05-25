package com.adven.concordion.extensions.exam.kafka.check;

import com.adven.concordion.extensions.exam.kafka.DummyEventProducer;
import com.adven.concordion.extensions.exam.kafka.Event;
import com.adven.concordion.extensions.exam.kafka.protobuf.TestEntity;
import com.google.protobuf.Message;
import org.junit.Before;
import org.junit.Test;

import static com.adven.concordion.extensions.exam.RandomUtils.*;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Ruslan Ustits
 */
public class WithReplyTest {

    private DummyEventProducer eventProducer;
    private Event<Message> successEvent;
    private Event<Message> failEvent;

    @Before
    public void setUp() {
        eventProducer = new DummyEventProducer();
        successEvent = Event.<Message>builder()
                .message(TestEntity.Entity.getDefaultInstance())
                .topicName(anyString())
                .build();
        failEvent = Event.<Message>builder()
                .message(TestEntity.Entity.getDefaultInstance())
                .topicName(anyString())
                .build();
    }

    @Test
    public void testVerify() {
        assertThat(testVerify(true, true)).isTrue();
        assertThat(eventProducer.popProducedMessages()).containsOnly(successEvent.getMessage());
    }

    @Test
    public void testVerifyWhenFailedToProduceEvent() {
        assertThat(testVerify(true, false)).isFalse();
    }

    @Test
    public void testVerifyWhenInnerMockFailed() {
        assertThat(testVerify(false, true)).isTrue();
        assertThat(eventProducer.popProducedMessages()).containsOnly(failEvent.getMessage());
    }

    private boolean testVerify(final boolean isEventVerified, final boolean isEventProduced) {
        if (isEventProduced) {
            eventProducer.mustReturnTrue();
        } else {
            eventProducer.mustReturnFalse();
        }
        final WithReply reply = new WithReply(successEvent, failEvent,
                eventProducer, new DummyMock(isEventVerified));
        return reply.verify();
    }

}