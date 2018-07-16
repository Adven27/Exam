package com.adven.concordion.extensions.exam.entities;

import com.adven.concordion.extensions.exam.kafka.protobuf.TestEntity;
import org.junit.Test;

import static com.adven.concordion.extensions.exam.RandomUtils.anyInt;
import static com.adven.concordion.extensions.exam.RandomUtils.anyString;
import static com.adven.concordion.extensions.exam.kafka.EventUtils.goodClass;
import static com.adven.concordion.extensions.exam.kafka.EventUtils.goodMessage;
import static org.assertj.core.api.Assertions.assertThat;

public class ProtoEntityTest {

    @Test
    public void testEntity() {
        final ProtoEntity protoEntity = new ProtoEntity(goodMessage(), goodClass().getName());
        assertThat(protoEntity.toBytes()).isNotEmpty();
    }

    @Test
    public void testEntityWithBadMessage() {
        final ProtoEntity protoEntity = new ProtoEntity(anyString(), goodClass().getName());
        assertThat(protoEntity.toBytes()).isEmpty();
    }

    @Test
    public void testIsEqualTo() {
        final String name = anyString();
        final int number = anyInt();
        final TestEntity.Entity entity = TestEntity.Entity.newBuilder()
            .setName(name)
            .setNumber(number)
            .build();
        final String expected = goodMessage(name, number);
        final ProtoEntity protoEntity = new ProtoEntity(expected, goodClass().getName());
        final boolean result = protoEntity.isEqualTo(entity.toByteArray());
        assertThat(result).isTrue();
    }

    @Test
    public void testIsNotEqualToWithBadJsonValue() {
        final ProtoEntity protoEntity = new ProtoEntity(anyString(), goodClass().getName());
        final TestEntity.Entity entity = TestEntity.Entity.newBuilder()
            .setName(anyString())
            .setNumber(anyInt())
            .build();
        final boolean result = protoEntity.isEqualTo(entity.toByteArray());
        assertThat(result).isFalse();
    }

    @Test
    public void testIsNotEqualTo() {
        final ProtoEntity protoEntity = new ProtoEntity(goodMessage(), goodClass().getName());
        final TestEntity.Entity entity = TestEntity.Entity.newBuilder()
            .setName(anyString())
            .setNumber(anyInt())
            .build();
        final boolean result = protoEntity.isEqualTo(entity.toByteArray());
        assertThat(result).isFalse();
    }

}