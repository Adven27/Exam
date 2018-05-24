package com.adven.concordion.extensions.exam.kafka.check;

import com.adven.concordion.extensions.exam.kafka.Event;
import com.adven.concordion.extensions.exam.kafka.KafkaAwareTest;
import com.google.protobuf.Message;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.utils.Bytes;
import org.junit.Test;

import java.util.concurrent.ExecutionException;

import static com.adven.concordion.extensions.exam.RandomUtils.anyInt;
import static com.adven.concordion.extensions.exam.RandomUtils.anyString;
import static com.adven.concordion.extensions.exam.kafka.EventUtils.goodClass;
import static com.adven.concordion.extensions.exam.kafka.EventUtils.goodEvent;
import static com.adven.concordion.extensions.exam.kafka.protobuf.TestEntity.*;
import static org.assertj.core.api.Assertions.*;

/**
 * @author Ruslan Ustits
 */
public class AsyncMockTest extends KafkaAwareTest {

    private static final Entity SUCCESS = Entity.newBuilder().setName("OK").build();
    private static final Entity FAIL = Entity.newBuilder().setName("FAIL").build();

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

    @Test
    public void testAsyncVerifyWithFailReply() throws ExecutionException, InterruptedException {
        final Event<String> eventToVerify = goodEvent(anyString(), anyInt())
                .toBuilder()
                .topicName(CONSUME_TOPIC)
                .build();
        final Entity entity = Entity.newBuilder()
                .setName(anyString())
                .setNumber(anyInt())
                .build();

        final ConsumerRecord<String, Bytes> record = startTest(eventToVerify, entity);
        assertThat(record.value()).isEqualTo(Bytes.wrap(FAIL.toByteArray()));
    }

    private ConsumerRecord<String, Bytes> startTest(final Event<String> eventToVerify, final Entity entityToSend)
            throws ExecutionException, InterruptedException {
        final Event<Message> successReplyEvent = Event.<Message>builder()
                .topicName(PRODUCE_TOPIC)
                .message(SUCCESS)
                .build();
        final Event<Message> failReplyEvent = Event.<Message>builder()
                .topicName(PRODUCE_TOPIC)
                .message(FAIL)
                .build();

        final SyncMock mock = new SyncMock(eventToVerify, goodClass().getName(), eventConsumer());
        final WithReply withReply = new WithReply(successReplyEvent, failReplyEvent, eventProducer(), mock);
        final AsyncMock asyncMock = new AsyncMock(withReply);

        final boolean result = asyncMock.verify();
        assertThat(result).isTrue();

        final Bytes bytes = Bytes.wrap(entityToSend.toByteArray());
        produceEvent(bytes);

        return consumeSingleEvent();
    }
}