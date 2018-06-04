package com.adven.concordion.extensions.exam.kafka;

import com.adven.concordion.extensions.exam.kafka.protobuf.ProtoEntity;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.utils.Bytes;
import org.junit.Test;

import static com.adven.concordion.extensions.exam.RandomUtils.anyInt;
import static com.adven.concordion.extensions.exam.RandomUtils.anyString;
import static com.adven.concordion.extensions.exam.kafka.EventUtils.goodClass;
import static com.adven.concordion.extensions.exam.kafka.EventUtils.goodMessage;
import static org.assertj.core.api.Assertions.assertThat;


public class DefaultEventProducerTestIT extends KafkaAwareTest {

    @Test
    public void testProduce() {
        final EventProducer eventProducer = new DefaultEventProducer(DEFAULT_PRODUCE_TIMEOUT,
                kafka.getBrokersAsString());

        final String name = anyString();
        final int number = anyInt();
        final ProtoEntity protoEntity = new ProtoEntity(goodMessage(name, number), goodClass().getName());
        final boolean result = eventProducer.produce(PRODUCE_TOPIC, anyString(), protoEntity);
        assertThat(result).isTrue();

        final ConsumerRecord<String, Bytes> record = consumeSingleEvent();
        assertThat(record.value()).isEqualTo(Bytes.wrap(protoEntity.toBytes()));
    }

}
