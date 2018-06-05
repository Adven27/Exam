package com.adven.concordion.extensions.exam.kafka;

import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.BytesDeserializer;
import org.apache.kafka.common.serialization.BytesSerializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.utils.Bytes;
import org.junit.ClassRule;
import org.springframework.kafka.test.rule.KafkaEmbedded;
import org.springframework.kafka.test.utils.KafkaTestUtils;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ExecutionException;

public abstract class KafkaAwareTest {

    protected static final String CONSUME_TOPIC = "test.consume.topic";
    protected static final String PRODUCE_TOPIC = "test.produce.topic";
    protected static final long DEFAULT_CONSUME_TIMEOUT = 1000L;
    protected static final long DEFAULT_PRODUCE_TIMEOUT = 1000L;

    @ClassRule
    public static final KafkaEmbedded kafka = new KafkaEmbedded(1, false, CONSUME_TOPIC);

    protected final void produceEvent(final Bytes bytes) throws ExecutionException, InterruptedException {
        final Map<String, Object> props = KafkaTestUtils.producerProps(kafka);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, BytesSerializer.class);
        try (Producer<String, Bytes> producer = new KafkaProducer<>(props)) {
            final ProducerRecord<String, Bytes> record = new ProducerRecord<>(CONSUME_TOPIC, bytes);
            producer.send(record).get();
        }
    }

    protected final ConsumerRecord<String, Bytes> consumeSingleEvent() {
        return consumeSingleEvent(PRODUCE_TOPIC, DEFAULT_CONSUME_TIMEOUT);
    }

    protected final ConsumerRecord<String, Bytes> consumeSingleEvent(final String fromTopic, final long timeout) {
        final Map<String, Object> props = KafkaTestUtils.consumerProps("consumer.group", "false", kafka);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, BytesDeserializer.class);
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        final Consumer<String, Bytes> consumer = new KafkaConsumer<>(props);
        consumer.subscribe(Collections.singleton(fromTopic));
        final ConsumerRecord<String, Bytes> record = KafkaTestUtils.getSingleRecord(consumer, fromTopic, 1000L);
        consumer.commitSync();
        consumer.unsubscribe();
        return record;
    }

    protected final EventConsumer eventConsumer() {
        return new DefaultEventConsumer(DEFAULT_CONSUME_TIMEOUT, kafka.getBrokersAsString());
    }

    protected final EventProducer eventProducer() {
        return new DefaultEventProducer(DEFAULT_PRODUCE_TIMEOUT, kafka.getBrokersAsString());
    }

}
