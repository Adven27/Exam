package com.adven.concordion.extensions.exam.kafka;

import com.adven.concordion.extensions.exam.kafka.protobuf.TestEntity;
import com.google.common.base.Optional;
import com.google.protobuf.Message;
import org.junit.Before;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Ruslan Ustits
 */
public class DefaultEventProcessorTest {

    private DefaultEventProcessor eventProcessor;

    @Before
    public void setUp() throws Exception {
        eventProcessor = new DefaultEventProcessor();
    }

    @Test
    public void testConfigureReply() {
        final String json = "{ \"name\": \"test\", \"number\": 123 }";
        final Event event = new Event("someTopic", json);
        final boolean result = eventProcessor.configureReply(event, goodClass().getName());
        assertThat(result).isTrue();
    }

    @Test
    public void testConfigureWithNullClass() {
        final boolean result = eventProcessor.configureReply(Event.builder().build(), null);
        assertThat(result).isFalse();
    }

    @Test
    public void testConfigureReplyWithNullEvent() {
        final boolean result = eventProcessor.configureReply(null, goodClass().getName());
        assertThat(result).isFalse();
    }

    @Test
    public void testConvertToProto() {
        final Class<?> expectedClass = goodClass();
        final String name = "test";
        final int number = 123;
        final Event event = new Event("someTopic", goodMessage(name, number));
        final Optional<Message> result = eventProcessor.convertToProto(event, expectedClass.getName());
        final Message message = result.get();
        assertThat(message).isNotNull().isInstanceOf(expectedClass);
        assertThat(((TestEntity.Entity) message).getName()).isEqualTo(name);
        assertThat(((TestEntity.Entity) message).getNumber()).isEqualTo(number);
    }

    @Test
    public void testConvertToProtoWithBadMessage() {
        final String json = "123";
        final Event event = new Event("someTopic", json);
        final Optional<Message> message = eventProcessor.convertToProto(event, goodClass().getName());
        assertThat(message).isEqualTo(Optional.absent());
    }

    @Test
    public void testConvertToProtoWithBadClassName() {
        final String json = goodMessage();
        final Event event = new Event("someTopic", json);
        final Optional<Message> message = eventProcessor.convertToProto(event, Object.class.getName());
        assertThat(message).isEqualTo(Optional.absent());
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