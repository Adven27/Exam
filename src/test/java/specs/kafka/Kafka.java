package specs.kafka;

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
import org.springframework.kafka.test.utils.KafkaTestUtils;
import specs.Specs;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class Kafka extends Specs {

    protected final void produceEvent(final ProducerRecord<String, Bytes> record) throws InterruptedException,
        ExecutionException, TimeoutException {
        final Map<String, Object> props = KafkaTestUtils.producerProps(kafka);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, BytesSerializer.class);
        try (Producer<String, Bytes> producer = new KafkaProducer<>(props)) {
            producer.send(record).get(5000, TimeUnit.MILLISECONDS);
        }
    }

    protected final void produceEvent(final Bytes bytes) throws ExecutionException, InterruptedException,
        TimeoutException {
        final ProducerRecord<String, Bytes> record = new ProducerRecord<>(CONSUME_TOPIC, bytes);
        produceEvent(record);
    }

    protected final ConsumerRecord<String, Bytes> consumeSingleEvent() {
        return consumeSingleEvent(PRODUCE_TOPIC, 1000L);
    }

    protected final ConsumerRecord<String, Bytes> consumeSingleEvent(final String fromTopic, final long timeout) {
        final Map<String, Object> props = KafkaTestUtils.consumerProps("consumer.group", "false", kafka);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, BytesDeserializer.class);
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        final Consumer<String, Bytes> consumer = new KafkaConsumer<>(props);
        consumer.subscribe(Collections.singleton(fromTopic));
        final ConsumerRecord<String, Bytes> record = KafkaTestUtils.getSingleRecord(consumer, fromTopic, timeout);
        consumer.commitSync();
        consumer.unsubscribe();
        return record;
    }

}
