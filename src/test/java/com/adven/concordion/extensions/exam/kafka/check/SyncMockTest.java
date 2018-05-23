package com.adven.concordion.extensions.exam.kafka.check;

import com.adven.concordion.extensions.exam.kafka.DummyEventConsumer;
import com.adven.concordion.extensions.exam.kafka.Event;
import com.adven.concordion.extensions.exam.kafka.protobuf.TestEntity;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.RandomUtils;
import org.apache.kafka.common.utils.Bytes;
import org.junit.Before;
import org.junit.Test;

import static com.adven.concordion.extensions.exam.kafka.protobuf.TestEntity.*;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Ruslan Ustits
 */
public class SyncMockTest {

    private DummyEventConsumer eventConsumer;

    @Before
    public void setUp() throws Exception {
        eventConsumer = DummyEventConsumer.defaultInstance();
    }

    @Test
    public void testVerify() {
        final String name = anyString();
        final int number = RandomUtils.nextInt();
        final Entity entity = Entity.newBuilder()
                .setName(name)
                .setNumber(number)
                .build();
        final Bytes bytes = Bytes.wrap(entity.toByteArray());
        final String expected = "{\n" +
                "  \"name\": \"" + name + "\",\n" +
                "  \"number\": " + number + "\n" +
                "}";

        final String topicName = anyString();
        final Event<Bytes> bytesEvent = Event.<Bytes>builder()
                .topicName(topicName)
                .message(bytes)
                .build();
        final Event<String> stringEvent = Event.<String>builder()
                .topicName(topicName)
                .message(expected)
                .build();

        eventConsumer.addEventToReturn(bytesEvent);
        final SyncMock syncMock = new SyncMock(stringEvent, TestEntity.Entity.class.getName(), eventConsumer);
        assertThat(syncMock.verify()).isTrue();
    }

    @Test
    public void testVerifyWithNullMessage() {
        final Event<String> event = Event.empty();
        eventConsumer.addStringEventToReturn(event);
        final SyncMock syncMock = new SyncMock(event, anyString(), eventConsumer);
        assertThat(syncMock.verify()).isFalse();
    }

    @Test
    public void testVerifyWhenNoMessageWasConsumed() {
        final SyncMock syncMock = new SyncMock(Event.<String>empty(), anyString(), eventConsumer);
        assertThat(syncMock.verify()).isFalse();
    }

    @Test
    public void testVerifyWhenMessagesAreNotEqual() {
        final Event<String> one = Event.<String>builder()
                .message(anyString())
                .build();
        final Event<String> another = Event.<String>builder()
                .message(anyString())
                .build();
        eventConsumer.addStringEventToReturn(one);
        final SyncMock syncMock = new SyncMock(another, anyString(), eventConsumer);
        assertThat(syncMock.verify()).isFalse();
    }

    @Test
    public void testConsume() {
        final Event<Bytes> first = Event.<Bytes>builder()
                .topicName(anyString())
                .build();
        final Event<Bytes> second = Event.<Bytes>builder()
                .topicName(anyString())
                .build();
        eventConsumer.addEventToReturn(first)
                .addEventToReturn(second);
        final SyncMock syncMock = new SyncMock(Event.<String>empty(), anyString(), eventConsumer);
        final Event event = syncMock.consume(anyString());
        assertThat(event).isEqualTo(first);
    }

    @Test
    public void testConsumeIfThereAreNoEvents() {
        final SyncMock syncMock = new SyncMock(Event.<String>empty(), anyString(), eventConsumer);
        final Event event = syncMock.consume(anyString());
        assertThat(event).isNull();
    }

    @Test
    public void testConsumeWithNullTopic() {
        final SyncMock syncMock = new SyncMock(Event.<String>empty(), anyString(), eventConsumer);
        final Event event = syncMock.consume(null);
        assertThat(event).isNull();
    }

    @Test
    public void testConsumeWithBlankTopic() {
        final SyncMock syncMock = new SyncMock(Event.<String>empty(), anyString(), eventConsumer);
        final Event event = syncMock.consume("");
        assertThat(event).isNull();
    }

    private String anyString() {
        return RandomStringUtils.random(10);
    }

}