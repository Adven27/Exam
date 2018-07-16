package com.adven.concordion.extensions.exam.kafka;

import org.apache.kafka.common.utils.Bytes;
import org.junit.Test;

import java.util.List;
import java.util.concurrent.ExecutionException;

import static com.adven.concordion.extensions.exam.RandomUtils.anyInt;
import static com.adven.concordion.extensions.exam.RandomUtils.anyString;
import static com.adven.concordion.extensions.exam.utils.protobuf.TestEntity.Entity;
import static org.assertj.core.api.Assertions.assertThat;

public class DefaultEventConsumerTestIT extends KafkaAwareTest {

    @Test
    public void testConsume() throws ExecutionException, InterruptedException {
        final EventConsumer eventConsumer = new DefaultEventConsumer(DEFAULT_CONSUME_TIMEOUT,
            kafka.getBrokersAsString());
        final Entity entity = Entity.newBuilder()
            .setName(anyString())
            .setNumber(anyInt())
            .build();
        final Bytes bytes = Bytes.wrap(entity.toByteArray());
        produceEvent(bytes);

        final List<Event<Bytes>> events = eventConsumer.consume(CONSUME_TOPIC);

        final Event<Bytes> expected = Event.<Bytes>builder()
            .message(bytes)
            .topicName(CONSUME_TOPIC)
            .header(EventHeader.empty())
            .build();
        assertThat(events).containsOnly(expected);
    }

}
