package com.adven.concordion.extensions.exam.kafka.check;

import com.adven.concordion.extensions.exam.kafka.Event;
import com.adven.concordion.extensions.exam.kafka.KafkaAwareTest;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.utils.Bytes;
import org.junit.Test;

import java.util.concurrent.ExecutionException;

import static com.adven.concordion.extensions.exam.RandomUtils.anyInt;
import static com.adven.concordion.extensions.exam.RandomUtils.anyString;
import static com.adven.concordion.extensions.exam.kafka.EventUtils.goodEvent;
import static com.adven.concordion.extensions.exam.kafka.protobuf.TestEntity.Entity;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Ruslan Ustits
 */
public class AsyncMockTest extends KafkaAwareTest {

    @Test
    public void testAsyncVerifyWithReply() throws ExecutionException, InterruptedException {
        final String name = anyString();
        final int number = anyInt();
        final Event<String> eventToVerify = goodEvent(name, number)
                .toBuilder()
                .topicName(CONSUME_TOPIC)
                .build();
        final Entity entity = Entity.newBuilder()
                .setName(name)
                .setNumber(number)
                .build();

        final ConsumerRecord<String, Bytes> record = startTest(eventToVerify, entity);
        assertThat(record.value()).isEqualTo(Bytes.wrap(SUCCESS.toByteArray()));
    }

}