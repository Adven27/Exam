package com.adven.concordion.extensions.exam.kafka;

import com.google.protobuf.Message;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.utils.Bytes;

import java.util.Properties;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * @author Ruslan Ustits
 */
@Slf4j
@RequiredArgsConstructor
public final class DefaultEventProducer implements EventProducer {

    private final long produceTimeout;

    @Override
    public boolean produce(@NonNull final String topic, final String key, @NonNull final Message message,
                           @NonNull final Properties properties) {
        try (KafkaProducer<String, Bytes> producer = new KafkaProducer<>(properties)) {
            final ProducerRecord<String, Bytes> record =
                    new ProducerRecord<>(topic, key, Bytes.wrap(message.toByteArray()));
            producer.send(record).get(produceTimeout, TimeUnit.MILLISECONDS);
            return true;
        } catch (InterruptedException e) {
            log.warn("Thread was interrupted", e);
            Thread.currentThread().interrupt();
        } catch (ExecutionException e) {
            log.error("Failed to retrieve execution result after sending message={} with key={} to topic={}",
                    message, key, topic, e);
        } catch (TimeoutException e) {
            log.warn("Sending message={} with key={} to topic={} ended due to timeout", message, key, topic, e);
        }
        return false;
    }

}
