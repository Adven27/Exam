package com.adven.concordion.extensions.exam.kafka.check;

import com.adven.concordion.extensions.exam.kafka.DummyEventProducer;
import com.adven.concordion.extensions.exam.kafka.Event;
import com.adven.concordion.extensions.exam.kafka.EventUtils;
import com.adven.concordion.extensions.exam.kafka.protobuf.ProtoEntity;
import org.junit.Before;
import org.junit.Test;

import static com.adven.concordion.extensions.exam.RandomUtils.anyString;
import static org.assertj.core.api.Assertions.assertThat;

public class WithReplyTest {

    private DummyEventProducer eventProducer;
    private Event<ProtoEntity> successEvent;
    private Event<ProtoEntity> failEvent;

    @Before
    public void setUp() {
        eventProducer = new DummyEventProducer();
        successEvent = EventUtils.goodEvent()
            .toBuilder()
            .topicName(anyString())
            .build();
        failEvent = EventUtils.goodEvent()
            .toBuilder()
            .topicName(anyString())
            .build();
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

    @Test
    public void testVerify() {
        assertThat(testVerify(true, true)).isTrue();
        assertThat(eventProducer.popProducedMessages()).containsOnly(successEvent.getMessage());
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