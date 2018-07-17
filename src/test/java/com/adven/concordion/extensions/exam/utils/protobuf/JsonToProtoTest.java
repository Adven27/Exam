package com.adven.concordion.extensions.exam.utils.protobuf;

import com.google.common.base.Optional;
import com.google.protobuf.Message;
import org.junit.Before;
import org.junit.Test;

import static com.adven.concordion.extensions.exam.RandomUtils.anyInt;
import static com.adven.concordion.extensions.exam.RandomUtils.anyString;
import static com.adven.concordion.extensions.exam.kafka.EventUtils.goodClass;
import static com.adven.concordion.extensions.exam.kafka.EventUtils.goodMessage;
import static com.adven.concordion.extensions.exam.utils.protobuf.TestEntity.Entity;
import static org.assertj.core.api.Assertions.assertThat;


public class JsonToProtoTest {

    private JsonToProto<Entity> converter;

    @Before
    public void setUp() {
        converter = new JsonToProto<>(Entity.class);
    }

    @Test
    public void testConvertToProto() {
        final Class<? extends Message> expectedClass = goodClass();
        final String name = anyString();
        final int number = anyInt();
        final String event = goodMessage(name, number);
        final Optional<Entity> result = converter.convert(event);

        final Message message = result.get();
        assertThat(message).isNotNull().isInstanceOf(expectedClass);
        assertThat(((TestEntity.Entity) message).getName()).isEqualTo(name);
        assertThat(((TestEntity.Entity) message).getNumber()).isEqualTo(number);
    }

    @Test
    public void testConvertToProtoWithBadMessage() {
        final String bad = anyString();
        final Optional<Entity> message = converter.convert(bad);
        assertThat(message).isEqualTo(Optional.absent());
    }

    @Test
    public void testConvertWithNullString() {
        final Optional<Entity> result = converter.convert(null);
        assertThat(result).isEqualTo(Optional.absent());
    }

    @Test
    public void testBuildMessageBuilder() {
        final Optional<Message.Builder> builder = converter.buildMessageBuilder();
        assertThat(builder.get()).isNotNull();
    }

}