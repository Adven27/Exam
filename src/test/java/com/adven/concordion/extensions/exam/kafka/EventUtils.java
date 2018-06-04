package com.adven.concordion.extensions.exam.kafka;

import com.adven.concordion.extensions.exam.kafka.protobuf.ProtoEntity;
import com.adven.concordion.extensions.exam.kafka.protobuf.TestEntity;

import static com.adven.concordion.extensions.exam.RandomUtils.anyInt;
import static com.adven.concordion.extensions.exam.RandomUtils.anyString;

/**
 * @author Ruslan Ustits
 */
public final class EventUtils {

    private EventUtils() {
    }

    public static Event<ProtoEntity> goodEvent() {
        return Event.<ProtoEntity>builder()
                .message(new ProtoEntity(goodMessage(), goodClass().getName()))
                .build();
    }

    public static Event<ProtoEntity> goodEvent(final String name) {
        return Event.<ProtoEntity>builder()
                .message(new ProtoEntity(goodMessage(name), goodClass().getName()))
                .build();
    }

    public static Event<ProtoEntity> goodEvent(final String name, final int number) {
        return Event.<ProtoEntity>builder()
                .message(new ProtoEntity(goodMessage(name, number), goodClass().getName()))
                .build();
    }

    public static Event<ProtoEntity> badEvent() {
        return Event.<ProtoEntity>builder()
                .message(new ProtoEntity(anyString(), anyString()))
                .build();
    }

    public static String goodMessage() {
        return goodMessage(anyString(), anyInt());
    }

    public static String goodMessage(final String name) {
        return "{\n" +
                "  \"name\": \"" + name + "\"\n" +
                "}";
    }

    public static String goodMessage(final String name, final int number) {
        return "{\n" +
                "  \"name\": \"" + name + "\",\n" +
                "  \"number\": " + number + "\n" +
                "}";
    }

    public static Class<TestEntity.Entity> goodClass() {
        return TestEntity.Entity.class;
    }

}
