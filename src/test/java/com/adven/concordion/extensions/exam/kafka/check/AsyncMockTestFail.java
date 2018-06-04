package com.adven.concordion.extensions.exam.kafka.check;

import com.adven.concordion.extensions.exam.kafka.Event;
import com.adven.concordion.extensions.exam.kafka.protobuf.ProtoEntity;
import com.adven.concordion.extensions.exam.kafka.protobuf.TestEntity;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.utils.Bytes;
import org.junit.Test;

import java.util.concurrent.ExecutionException;

import static com.adven.concordion.extensions.exam.RandomUtils.anyInt;
import static com.adven.concordion.extensions.exam.RandomUtils.anyString;
import static com.adven.concordion.extensions.exam.kafka.EventUtils.goodEvent;
import static org.assertj.core.api.Assertions.assertThat;

public class AsyncMockTestFail extends AsyncTest {

    @Test
    public void testAsyncVerifyWithFailReply() throws ExecutionException, InterruptedException {
        final Event<ProtoEntity> eventToVerify = goodEvent(anyString(), anyInt())
                .toBuilder()
                .topicName(CONSUME_TOPIC)
                .build();
        final TestEntity.Entity entity = TestEntity.Entity.newBuilder()
                .setName(anyString())
                .setNumber(anyInt())
                .build();

        final ConsumerRecord<String, Bytes> record = startTest(eventToVerify, entity);
        assertThat(record.value()).isEqualTo(Bytes.wrap(FAIL.toByteArray()));
    }
}
