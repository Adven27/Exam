package com.adven.concordion.extensions.exam.kafka;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.utils.Bytes;
import org.junit.Test;

import static com.adven.concordion.extensions.exam.RandomUtils.anyInt;
import static com.adven.concordion.extensions.exam.RandomUtils.anyString;
import static com.adven.concordion.extensions.exam.kafka.protobuf.TestEntity.Entity;
import static org.assertj.core.api.Assertions.assertThat;


public class DefaultEventProducerTestIT extends KafkaAwareTest {

    @Test
    public void testProduce() {
        final EventProducer eventProducer = new DefaultEventProducer(DEFAULT_PRODUCE_TIMEOUT,
                kafka.getBrokersAsString());

        final String name = anyString();
        final int number = anyInt();
        final Entity message = Entity.newBuilder()
                .setName(name)
                .setNumber(number)
                .build();
        final boolean result = eventProducer.produce(PRODUCE_TOPIC, anyString(), message);
        assertThat(result).isTrue();

        final ConsumerRecord<String, Bytes> record = consumeSingleEvent();
        assertThat(record.value()).isEqualTo(Bytes.wrap(message.toByteArray()));
    }

}
