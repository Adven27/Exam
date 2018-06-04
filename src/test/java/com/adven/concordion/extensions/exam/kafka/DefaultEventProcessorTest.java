package com.adven.concordion.extensions.exam.kafka;

import com.adven.concordion.extensions.exam.kafka.protobuf.ProtoEntity;
import org.junit.Before;
import org.junit.Test;

import static com.adven.concordion.extensions.exam.RandomUtils.anyString;
import static org.assertj.core.api.Assertions.assertThat;

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
    public void testSendWithNullEvent() {
        final boolean result = processor.send(null);
        assertThat(result).isFalse();
    }

    @Test
    public void testSuccessSend() {
        eventProducer.mustReturnTrue();
        final boolean result = processor.send(anyString(), anyString(), mockEntity());
        assertThat(result).isTrue();
    }

    @Test
    public void testFailedSend() {
        eventProducer.mustReturnFalse();
        final boolean result = processor.send(anyString(), anyString(), mockEntity());
        assertThat(result).isFalse();
    }

    @Test
    public void testSendWithNullTopic() {
        final boolean result = processor.send(null, null, mockEntity());
        assertThat(result).isFalse();
    }

    @Test
    public void testSendWithEmptyTopic() {
        final boolean result = processor.send("", null, mockEntity());
        assertThat(result).isFalse();
    }

    @Test
    public void testSendWithNullMessage() {
        final boolean result = processor.send(anyString(), null, null);
        assertThat(result).isFalse();
    }

    private ProtoEntity mockEntity() {
        return new ProtoEntity(anyString(), anyString());
    }

}