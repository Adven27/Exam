package com.adven.concordion.extensions.exam.kafka;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.header.Header;
import org.apache.kafka.common.header.Headers;
import org.apache.kafka.common.serialization.BytesDeserializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.utils.Bytes;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

import static com.adven.concordion.extensions.exam.kafka.EventHeader.CORRELATION_ID;
import static com.adven.concordion.extensions.exam.kafka.EventHeader.REPLY_TOPIC;
import static org.apache.kafka.clients.consumer.ConsumerConfig.*;

@Slf4j
@RequiredArgsConstructor
public final class DefaultEventConsumer implements EventConsumer {
    private static final long POLL_TIMEOUT = 100L;

    private final long consumeTimeout;
    @NonNull
    private final Properties properties;

    public DefaultEventConsumer(final long consumeTimeout, final String kafkaBrokers) {
        this.consumeTimeout = consumeTimeout;
        properties = new Properties();
        properties.put(BOOTSTRAP_SERVERS_CONFIG, kafkaBrokers);
        properties.put(GROUP_ID_CONFIG, "exam-test-consumer-group");
        properties.put(CLIENT_ID_CONFIG, "exam-test-consumer");
        properties.put(AUTO_OFFSET_RESET_CONFIG, "earliest");
        properties.put(KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        properties.put(VALUE_DESERIALIZER_CLASS_CONFIG, BytesDeserializer.class.getName());
    }

    public DefaultEventConsumer withProperty(final Object key, final Object value) {
        properties.put(key, value);
        return this;
    }

    @Override
    public List<Event<Bytes>> consume(@NonNull final String fromTopic) {
        try (KafkaConsumer<String, Bytes> consumer = new KafkaConsumer<>(properties)) {
            consumer.subscribe(Collections.singleton(fromTopic));
            return toEvents(consumeBy(consumer));
        }
    }

    protected ConsumerRecords<String, Bytes> consumeBy(final KafkaConsumer<String, Bytes> consumer) {
        ConsumerRecords<String, Bytes> records = null;
        for (int i = 0; i < consumeTimeout; i += POLL_TIMEOUT) {
            records = consumer.poll(POLL_TIMEOUT);
            if (!records.isEmpty()) {
                break;
            }
        }
        return records;
    }

    private List<Event<Bytes>> toEvents(final ConsumerRecords<String, Bytes> records) {
        final List<Event<Bytes>> events = new ArrayList<>();
        for (ConsumerRecord<String, Bytes> record : records) {
            events.add(toEvent(record));
        }
        return events;
    }

    protected Event<Bytes> toEvent(@NonNull final ConsumerRecord<String, Bytes> record) {
        final String key = record.key();
        final Bytes value = record.value();
        final String topic = record.topic();
        return Event.<Bytes>builder()
            .topicName(topic)
            .key(key)
            .message(value)
            .header(eventHeader(record))
            .build();
    }

    protected EventHeader eventHeader(@NonNull final ConsumerRecord<String, Bytes> record) {
        final Headers headers = record.headers();
        return new EventHeader(
            headerToString(headers.lastHeader(REPLY_TOPIC)),
            headerToString(headers.lastHeader(CORRELATION_ID))
        );
    }

    protected byte[] headerToString(final Header header) {
        return header == null ? new byte[]{} : header.value();
    }
}
