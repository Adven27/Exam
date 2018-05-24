package com.adven.concordion.extensions.exam.kafka;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.utils.Bytes;
import org.junit.Before;
import org.junit.Test;

import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.Properties;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Ruslan Ustits
 */
public class DefaultEventConsumerTest {

    private DefaultEventConsumer consumer;

    @Before
    public void setUp() {
        consumer = new DefaultEventConsumer(1000L, new Properties());
    }

    @Test
    public void toEvent() throws UnsupportedEncodingException {
        final String topic = "topic";
        final String key = "key";
        final String message = "test";
        final Bytes messageInBytes = new Bytes("test".getBytes(Charset.forName("UTF-8")));
        final ConsumerRecord<String, Bytes> record = new ConsumerRecord<>(topic, 1, 1L, key, messageInBytes);
        final Event<Bytes> event = consumer.toEvent(record);
        assertThat(event.getKey()).isEqualTo(key);
        assertThat(event.getTopicName()).isEqualTo(topic);
        assertThat(event.getMessage()).isEqualTo(Bytes.wrap(message.getBytes("UTF-8")));
    }

    @Test(expected = NullPointerException.class)
    public void testToEventWithNullRecord() {
        consumer.toEvent(null);
    }

    @Test(expected = NullPointerException.class)
    public void testConsumeWithNullTopic() {
        consumer.consume(null);
    }

    @Test(expected = NullPointerException.class)
    public void testCreateInstanceWithNullProperty() {
        new DefaultEventConsumer(10L, (Properties) null);
    }
}