package com.adven.concordion.extensions.exam.kafka.check.verify;

import com.adven.concordion.extensions.exam.kafka.Event;
import com.adven.concordion.extensions.exam.kafka.EventUtils;
import org.apache.kafka.common.utils.Bytes;
import org.junit.Before;
import org.junit.Test;

import static com.adven.concordion.extensions.exam.RandomUtils.anyInt;
import static com.adven.concordion.extensions.exam.RandomUtils.anyString;
import static com.adven.concordion.extensions.exam.kafka.protobuf.TestEntity.Entity;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Ruslan Ustits
 */
public class MessageVerifierTest {

    private MessageVerifier verifier;

    @Before
    public void setUp() {
        verifier = new MessageVerifier(Entity.class.getName());
    }

    @Test
    public void testVerify() {
        final String name = anyString();
        final int number = anyInt();
        final Entity entity = Entity.newBuilder()
                .setName(name)
                .setNumber(number)
                .build();
        final Bytes bytes = Bytes.wrap(entity.toByteArray());
        final String expected = EventUtils.goodMessage(name, number);

        final Event<Bytes> first = Event.<Bytes>builder()
                .message(bytes)
                .build();
        final Event<String> second = Event.<String>builder()
                .message(expected)
                .build();
        final boolean result = verifier.verify(first, second);
        assertThat(result).isTrue();
    }

    @Test
    public void testVerifyWithBadFirstMessage() {
        final Event<Bytes> first = Event.<Bytes>builder()
                .message(Bytes.wrap(anyString().getBytes()))
                .build();
        final Event<String> second = Event.<String>builder()
                .message(anyString())
                .build();
        final boolean result = verifier.verify(first, second);
        assertThat(result).isFalse();
    }

    @Test
    public void testVerifyWhenMessagesAreNotEqual() {
        final Entity entity = Entity.newBuilder()
                .setName(anyString())
                .setNumber(anyInt())
                .build();
        final Bytes bytes = Bytes.wrap(entity.toByteArray());

        final Event<Bytes> first = Event.<Bytes>builder()
                .message(bytes)
                .build();
        final Event<String> second = Event.<String>builder()
                .message(anyString())
                .build();
        final boolean result = verifier.verify(first, second);
        assertThat(result).isFalse();
    }

}