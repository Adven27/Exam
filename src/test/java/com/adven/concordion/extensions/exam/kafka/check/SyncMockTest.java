package com.adven.concordion.extensions.exam.kafka.check;

import com.adven.concordion.extensions.exam.kafka.DummyEventConsumer;
import com.adven.concordion.extensions.exam.kafka.Event;
import org.junit.Before;
import org.junit.Test;

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
        final Event<String> first = Event.<String>builder()
                .topicName("test1")
                .message("message")
                .build();
        final Event<String> second = Event.empty();
        eventConsumer.addEventToReturn(first)
                .addEventToReturn(second);
        final SyncMock syncMock = new SyncMock(first, eventConsumer);
        assertThat(syncMock.verify()).isTrue();
    }

    @Test
    public void testVerifyWithNullMessage() {
        final Event<String> event = Event.empty();
        eventConsumer.addEventToReturn(event);
        final SyncMock syncMock = new SyncMock(event, eventConsumer);
        assertThat(syncMock.verify()).isFalse();
    }

    @Test
    public void testVerifyWhenNoMessageWasConsumed() {
        final SyncMock syncMock = new SyncMock(Event.<String>empty(), eventConsumer);
        assertThat(syncMock.verify()).isFalse();
    }

    @Test
    public void testVerifyWhenMessagesAreNotEqual() {
        final Event<String> one = Event.<String>builder()
                .message("first")
                .build();
        final Event<String> another = Event.<String>builder()
                .message("second")
                .build();
        eventConsumer.addEventToReturn(one);
        final SyncMock syncMock = new SyncMock(another, eventConsumer);
        assertThat(syncMock.verify()).isFalse();
    }

    @Test
    public void testConsume() {
        final Event<String> first = Event.<String>builder()
                .topicName("test1")
                .build();
        final Event<String> second = Event.<String>builder()
                .topicName("test2")
                .build();
        eventConsumer.addEventToReturn(first)
                .addEventToReturn(second);
        final SyncMock syncMock = new SyncMock(first, eventConsumer);
        final Event event = syncMock.consume("test");
        assertThat(event).isEqualTo(first);
    }

    @Test
    public void testConsumeIfThereAreNoEvents() {
        final SyncMock syncMock = new SyncMock(Event.<String>empty(), eventConsumer);
        final Event event = syncMock.consume("test");
        assertThat(event).isNull();
    }

    @Test
    public void testConsumeWithNullTopic() {
        final SyncMock syncMock = new SyncMock(Event.<String>empty(), eventConsumer);
        final Event event = syncMock.consume(null);
        assertThat(event).isNull();
    }

    @Test
    public void testConsumeWithBlankTopic() {
        final SyncMock syncMock = new SyncMock(Event.<String>empty(), eventConsumer);
        final Event event = syncMock.consume("");
        assertThat(event).isNull();
    }

}