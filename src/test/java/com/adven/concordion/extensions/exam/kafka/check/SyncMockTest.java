package com.adven.concordion.extensions.exam.kafka.check;

import com.adven.concordion.extensions.exam.kafka.DummyEventConsumer;
import com.adven.concordion.extensions.exam.kafka.Event;
import com.adven.concordion.extensions.exam.kafka.check.verify.MockVerifier;
import org.apache.kafka.common.utils.Bytes;
import org.junit.Before;
import org.junit.Test;

import static com.adven.concordion.extensions.exam.RandomUtils.anyString;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Ruslan Ustits
 */
public class SyncMockTest {

    private DummyEventConsumer eventConsumer;

    @Before
    public void setUp() {
        eventConsumer = DummyEventConsumer.defaultInstance();
    }

    @Test
    public void testVerify() {
        eventConsumer.addEventToReturn(Event.<Bytes>empty());
        final Event<String> event = Event.<String>builder()
                .topicName(anyString())
                .build();
        final SyncMock syncMock = new SyncMock(event, eventConsumer, MockVerifier.returningTrue());
        assertThat(syncMock.verify()).isTrue();
    }

    @Test
    public void testVerifyWithNullMessage() {
        final Event<String> event = Event.empty();
        eventConsumer.addStringEventToReturn(event);
        final SyncMock syncMock = new SyncMock(event, eventConsumer, MockVerifier.returningTrue());
        assertThat(syncMock.verify()).isFalse();
    }

    @Test
    public void testVerifyWhenNoMessageWasConsumed() {
        final SyncMock syncMock = new SyncMock(Event.<String>empty(), eventConsumer, MockVerifier.returningTrue());
        assertThat(syncMock.verify()).isFalse();
    }

    @Test
    public void testFailedVerify() {
        eventConsumer.addEventToReturn(Event.<Bytes>empty());
        final Event<String> event = Event.<String>builder()
                .topicName(anyString())
                .build();
        final SyncMock syncMock = new SyncMock(event, eventConsumer, MockVerifier.returningFalse());
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

}