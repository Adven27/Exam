package com.adven.concordion.extensions.exam.kafka;

import com.adven.concordion.extensions.exam.kafka.protobuf.TestEntity;
import com.google.common.base.Optional;
import com.google.protobuf.Message;
import org.junit.Before;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

/**
 * @author Ruslan Ustits
 */
public class DefaultEventProcessorTest {

    private DefaultEventProcessor processor;
    private DummyEventConsumer eventConsumer;
    private DummyEventProducer eventProducer;

    @Before
    public void setUp() throws Exception {
        eventConsumer = DummyEventConsumer.defaultInstance();
        eventProducer = DummyEventProducer.defaultInstance();
        processor = new DefaultEventProcessor(eventConsumer, eventProducer);
    }

    @Test
    public void testConvertToProto() {
        final Class<?> expectedClass = goodClass();
        final String name = "test";
        final int number = 123;
        final String event = goodMessage(name, number);
        final Optional<Message> result = processor.convertToProto(event, expectedClass.getName());
        final Message message = result.get();
        assertThat(message).isNotNull().isInstanceOf(expectedClass);
        assertThat(((TestEntity.Entity) message).getName()).isEqualTo(name);
        assertThat(((TestEntity.Entity) message).getNumber()).isEqualTo(number);
    }

    @Test
    public void testConvertToProtoWithBadMessage() {
        final String bad = "123";
        final Optional<Message> message = processor.convertToProto(bad, goodClass().getName());
        assertThat(message).isEqualTo(Optional.absent());
    }

    @Test
    public void testConvertToProtoWithBadClassName() {
        final String json = goodMessage();
        final Optional<Message> message = processor.convertToProto(json, Object.class.getName());
        assertThat(message).isEqualTo(Optional.absent());
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
        final boolean result = processor.send("123", "321", mock(Message.class));
        assertThat(result).isTrue();
    }

    @Test
    public void testFailedSend() {
        eventProducer.mustReturnFalse();
        final boolean result = processor.send("123", "321", mock(Message.class));
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
        final boolean result = processor.send("123", null, null);
        assertThat(result).isFalse();
    }

    private String goodMessage() {
        return goodMessage("test", 312);
    }

    private String goodMessage(final String name, final int number) {
        return "{ \"name\": \"" + name + "\", \"number\":" + number + " }";
    }

    private Class<TestEntity.Entity> goodClass() {
        return TestEntity.Entity.class;
    }

}