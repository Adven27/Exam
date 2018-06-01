package com.adven.concordion.extensions.exam.kafka.protobuf;

import com.adven.concordion.extensions.exam.kafka.Event;
import com.google.common.base.Optional;
import com.google.protobuf.Message;
import org.junit.Test;

import static com.adven.concordion.extensions.exam.RandomUtils.anyString;
import static com.adven.concordion.extensions.exam.kafka.EventUtils.goodClass;
import static com.adven.concordion.extensions.exam.kafka.EventUtils.goodMessage;
import static org.assertj.core.api.Assertions.assertThat;


public class ProtoUtilsTest {

    @Test
    public void testConvertEventToProtoEvent() {
        final Class<?> expectedClass = goodClass();
        final String message = goodMessage();
        final Event<String> event = Event.<String>builder()
                .message(message)
                .build();
        final Optional<Event<Message>> convertedEvent = ProtoUtils.fromJsonToProto(event, expectedClass.getName());
        assertThat(convertedEvent).isNotEqualTo(Optional.absent());
    }

    @Test
    public void testConvertEventToProtoEventWithBadMessage() {
        final Class<?> expectedClass = goodClass();
        final String message = anyString();
        final Event<String> event = Event.<String>builder()
                .message(message)
                .build();
        final Optional<Event<Message>> convertedEvent = ProtoUtils.fromJsonToProto(event, expectedClass.getName());
        assertThat(convertedEvent).isEqualTo(Optional.absent());
    }

    @Test
    public void testSafeForName() {
        final Optional<Class<Message>> clazz = ProtoUtils.safeForName(TestEntity.Entity.class.getName());
        assertThat(clazz).isEqualTo(Optional.of(TestEntity.Entity.class));
    }

    @Test
    public void testSafeForNameWithBadClassName() {
        final Optional<Class<Message>> clazz = ProtoUtils.safeForName(anyString());
        assertThat(clazz).isEqualTo(Optional.absent());
    }

}