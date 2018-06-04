package com.adven.concordion.extensions.exam.kafka;

import com.adven.concordion.extensions.exam.kafka.protobuf.ProtoEntity;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.BytesSerializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.apache.kafka.common.utils.Bytes;

import java.util.Properties;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static com.adven.concordion.extensions.exam.kafka.EventHeader.CORRELATION_ID;
import static com.adven.concordion.extensions.exam.kafka.EventHeader.REPLY_TOPIC;
import static org.apache.kafka.clients.consumer.ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG;
import static org.apache.kafka.clients.consumer.ConsumerConfig.CLIENT_ID_CONFIG;
import static org.apache.kafka.clients.producer.ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG;
import static org.apache.kafka.clients.producer.ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG;

/**
 * @author Ruslan Ustits
 */
@Slf4j
@RequiredArgsConstructor
public final class DefaultEventProducer implements EventProducer {

    private final long produceTimeout;

    @NonNull
    private final Properties properties;

    public DefaultEventProducer(final long produceTimeout, final String kafkaBrokers) {
        this.produceTimeout = produceTimeout;
        properties = new Properties();
        properties.put(BOOTSTRAP_SERVERS_CONFIG, kafkaBrokers);
        properties.put(CLIENT_ID_CONFIG, "exam-test-producer");
        properties.put(KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        properties.put(VALUE_SERIALIZER_CLASS_CONFIG, BytesSerializer.class.getName());
    }

    public DefaultEventProducer withProperty(final Object key, final Object value) {
        properties.put(key, value);
        return this;
    }

    @Override
    public boolean produce(@NonNull final String topic, final String key, @NonNull final ProtoEntity message) {
        final ProducerRecord<String, Bytes> record =
                new ProducerRecord<>(topic, key, Bytes.wrap(message.toBytes()));
        return produce(record);
    }

    @Override
    public boolean produce(@NonNull final String topic, final String key, final EventHeader eventHeader,
                           @NonNull final ProtoEntity message) {
        if (eventHeader == null) {
            return produce(topic, key, message);
        } else {
            final ProducerRecord<String, Bytes> record =
                    new ProducerRecord<>(topic, key, Bytes.wrap(message.toBytes()));
            addHeaders(record, eventHeader);
            return produce(record);
        }
    }

    protected void addHeaders(final ProducerRecord<String, Bytes> record, final EventHeader eventHeader) {
        record.headers().add(REPLY_TOPIC, eventHeader.getReplyToTopic());
        record.headers().add(CORRELATION_ID, eventHeader.getCorrelationId());
    }

    protected boolean produce(final ProducerRecord<String, Bytes> record) {
        try (KafkaProducer<String, Bytes> producer = new KafkaProducer<>(properties)) {
            producer.send(record).get(produceTimeout, TimeUnit.MILLISECONDS);
            return true;
        } catch (InterruptedException e) {
            log.warn("Thread was interrupted", e);
            Thread.currentThread().interrupt();
        } catch (ExecutionException e) {
            log.error("Failed to retrieve execution result after sending record={}", record, e);
        } catch (TimeoutException e) {
            log.warn("Sending record={} ended due to timeout", record, e);
        }
        return false;
    }

}
