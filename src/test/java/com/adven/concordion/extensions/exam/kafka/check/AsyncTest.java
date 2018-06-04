package com.adven.concordion.extensions.exam.kafka.check;

import com.adven.concordion.extensions.exam.kafka.Event;
import com.adven.concordion.extensions.exam.kafka.KafkaAwareTest;
import com.adven.concordion.extensions.exam.kafka.protobuf.ProtoEntity;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.utils.Bytes;

import java.util.concurrent.ExecutionException;

import static com.adven.concordion.extensions.exam.kafka.EventUtils.goodEvent;
import static com.adven.concordion.extensions.exam.kafka.protobuf.TestEntity.Entity;
import static org.assertj.core.api.Assertions.assertThat;

public abstract class AsyncTest extends KafkaAwareTest {

    protected static final Entity SUCCESS = Entity.newBuilder().setName("OK").build();
    protected static final Entity FAIL = Entity.newBuilder().setName("FAIL").build();

    protected ConsumerRecord<String, Bytes> startTest(final Event<ProtoEntity> eventToVerify, final Entity entityToSend)
        throws ExecutionException, InterruptedException {
        final Event<ProtoEntity> successReplyEvent = goodEvent("OK")
            .toBuilder()
            .topicName(PRODUCE_TOPIC)
            .build();
        final Event<ProtoEntity> failReplyEvent = goodEvent("FAIL")
            .toBuilder()
            .topicName(PRODUCE_TOPIC)
            .build();

        final SyncMock mock = new SyncMock(eventToVerify, eventConsumer());
        final WithReply withReply = new WithReply(successReplyEvent, failReplyEvent, eventProducer(), mock);
        final AsyncMock asyncMock = new AsyncMock(withReply);

        final boolean result = asyncMock.verify();
        assertThat(result).isTrue();

        final Bytes bytes = Bytes.wrap(entityToSend.toByteArray());
        produceEvent(bytes);

        return consumeSingleEvent();
    }

}
