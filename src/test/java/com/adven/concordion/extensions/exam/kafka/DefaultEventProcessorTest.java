package com.adven.concordion.extensions.exam.kafka;

import com.adven.concordion.extensions.exam.kafka.check.DummyMock;
import com.adven.concordion.extensions.exam.kafka.check.WithReply;
import com.google.common.base.Optional;
import com.google.protobuf.Message;
import org.junit.Before;
import org.junit.Test;

import static com.adven.concordion.extensions.exam.RandomUtils.anyString;
import static com.adven.concordion.extensions.exam.kafka.EventUtils.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

/**
 * @author Ruslan Ustits
 */
public class DefaultEventProcessorTest {

    private DefaultEventProcessor processor;
    private DummyEventProducer eventProducer;

    @Before
    public void setUp() {
        eventProducer = DummyEventProducer.defaultInstance();
        processor = new DefaultEventProcessor(DummyEventConsumer.defaultInstance(), eventProducer);
    }

    @Test
    public void testMockWithReply() {
        final Event<String> replyEvent = goodEvent();
        final Event<String> failEvent = goodEvent();
        final Optional<WithReply> withReply = processor.mockWithReply(replyEvent, failEvent,
                goodClass().getName(), new DummyMock(true));
        assertThat(withReply).isNotEqualTo(Optional.absent());
    }

    @Test
    public void testMockWithReplyWithBadReplyEvent() {
        final Event<String> replyEvent = badEvent();
        final Event<String> failEvent = goodEvent();
        final Optional<WithReply> withReply = processor.mockWithReply(replyEvent, failEvent,
                goodClass().getName(), new DummyMock(true));
        assertThat(withReply).isEqualTo(Optional.absent());
    }

    @Test
    public void testMockWithReplyWithBadFailEvent() {
        final Event<String> replyEvent = goodEvent();
        final Event<String> failEvent = badEvent();
        final Optional<WithReply> withReply = processor.mockWithReply(replyEvent, failEvent,
                goodClass().getName(), new DummyMock(true));
        assertThat(withReply).isEqualTo(Optional.absent());
    }

    @Test
    public void testSendWithNullEvent() {
        final boolean result = processor.send(null, "event");
        assertThat(result).isFalse();
    }

    @Test
    public void testSendWithNullClassName() {
        final boolean result = processor.send(Event.<String>empty(), null);
        assertThat(result).isFalse();
    }

    @Test
    public void testSendWithBlankClassName() {
        final boolean result = processor.send(Event.<String>empty(), "");
        assertThat(result).isFalse();
    }

    @Test
    public void testSuccessSend() {
        eventProducer.mustReturnTrue();
        final boolean result = processor.send(anyString(), anyString(), mock(Message.class));
        assertThat(result).isTrue();
    }

    @Test
    public void testFailedSend() {
        eventProducer.mustReturnFalse();
        final boolean result = processor.send(anyString(), anyString(), mock(Message.class));
        assertThat(result).isFalse();
    }

    @Test
    public void testSendWithNullTopic() {
        final boolean result = processor.send(null, null, mock(Message.class));
        assertThat(result).isFalse();
    }

    @Test
    public void testSendWithEmptyTopic() {
        final boolean result = processor.send("", null, mock(Message.class));
        assertThat(result).isFalse();
    }

    @Test
    public void testSendWithNullMessage() {
        final boolean result = processor.send(anyString(), null, null);
        assertThat(result).isFalse();
    }

}