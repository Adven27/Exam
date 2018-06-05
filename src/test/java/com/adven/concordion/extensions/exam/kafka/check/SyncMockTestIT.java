package com.adven.concordion.extensions.exam.kafka.check;

import com.adven.concordion.extensions.exam.kafka.Event;
import com.adven.concordion.extensions.exam.kafka.KafkaAwareTest;
import com.adven.concordion.extensions.exam.kafka.protobuf.ProtoEntity;
import org.apache.kafka.common.utils.Bytes;
import org.junit.Test;

import java.util.concurrent.ExecutionException;

import static com.adven.concordion.extensions.exam.RandomUtils.anyInt;
import static com.adven.concordion.extensions.exam.RandomUtils.anyString;
import static com.adven.concordion.extensions.exam.kafka.EventUtils.goodEvent;
import static com.adven.concordion.extensions.exam.kafka.protobuf.TestEntity.Entity;
import static org.assertj.core.api.Assertions.assertThat;

public class SyncMockTestIT extends KafkaAwareTest {

    @Test
    public void testVerify() throws ExecutionException, InterruptedException {
        final String name = anyString();
        final int number = anyInt();
        final Event<ProtoEntity> event = goodEvent(name, number)
            .toBuilder()
            .topicName(CONSUME_TOPIC)
            .build();

        final SyncMock mock = new SyncMock(event, eventConsumer());

        final Entity entity = Entity.newBuilder()
            .setName(name)
            .setNumber(number)
            .build();
        final Bytes bytes = Bytes.wrap(entity.toByteArray());
        produceEvent(bytes);

        final boolean result = mock.verify();
        assertThat(result).isTrue();
    }

}