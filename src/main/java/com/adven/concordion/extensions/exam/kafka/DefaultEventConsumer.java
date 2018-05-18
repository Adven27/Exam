package com.adven.concordion.extensions.exam.kafka;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.utils.Bytes;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

/**
 * @author Ruslan Ustits
 */
@Slf4j
@RequiredArgsConstructor
public final class DefaultEventConsumer implements EventConsumer {

    private final long consumeTimeout;

    @Override
    public List<Event> consume(@NonNull final String fromTopic, @NonNull final Properties properties) {
        try (KafkaConsumer<String, Bytes> consumer = new KafkaConsumer<>(properties)) {
            consumer.subscribe(Collections.singleton(fromTopic));
            final ConsumerRecords<String, Bytes> records = consumer.poll(consumeTimeout);
            return toEvents(records);
        }
    }

    private List<Event> toEvents(final ConsumerRecords<String, Bytes> records) {
        final List<Event> events = new ArrayList<>();
        for (ConsumerRecord<String, Bytes> record : records) {
            final Event event = toEvent(record);
            events.add(event);
        }
        return events;
    }

    protected Event toEvent(@NonNull final ConsumerRecord<String, Bytes> record) {
        final String key = record.key();
        final byte[] value = record.value().get();
        final String topic = record.topic();
        return Event.builder()
                .topicName(topic)
                .key(key)
                .message(new String(value))
                .build();
    }

}
