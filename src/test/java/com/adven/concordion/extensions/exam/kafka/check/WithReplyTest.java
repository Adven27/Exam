package com.adven.concordion.extensions.exam.kafka.check;

import com.adven.concordion.extensions.exam.kafka.DummyEventProducer;
import com.adven.concordion.extensions.exam.kafka.Event;
import com.adven.concordion.extensions.exam.kafka.protobuf.TestEntity;
import com.google.protobuf.Message;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Ruslan Ustits
 */
public class WithReplyTest {

    @Test
    public void testVerify() {
        assertThat(testVerify(true, true)).isTrue();
    }

    @Test
    public void testVerifyWhenFailedToProduceEvent() {
        assertThat(testVerify(true, false)).isFalse();
    }

    @Test
    public void testVerifyWhenInnerMockFailed() {
        assertThat(testVerify(false, true)).isFalse();
    }

    private boolean testVerify(final boolean isEventVerified, final boolean isEventProduced) {
        final Event<Message> event = Event.<Message>builder()
                .message(TestEntity.Entity.getDefaultInstance())
                .topicName("topic")
                .build();
        final DummyEventProducer eventProducer = DummyEventProducer.defaultInstance();
        if (isEventProduced) {
            eventProducer.mustReturnTrue();
        } else {
            eventProducer.mustReturnFalse();
        }
        final WithReply reply = new WithReply(event, eventProducer, new DummyMock(isEventVerified));
        return reply.verify();
    }
}