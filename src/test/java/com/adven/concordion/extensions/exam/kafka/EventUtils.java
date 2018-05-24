package com.adven.concordion.extensions.exam.kafka;

import com.adven.concordion.extensions.exam.kafka.protobuf.TestEntity;

import static com.adven.concordion.extensions.exam.RandomUtils.anyInt;
import static com.adven.concordion.extensions.exam.RandomUtils.anyString;

/**
 * @author Ruslan Ustits
 */
public final class EventUtils {

    private EventUtils() {
    }

    public static Event<String> goodEvent() {
        return Event.<String>builder()
                .message(goodMessage())
                .build();
    }

    public static Event<String> goodEvent(final String name, final int number) {
        return Event.<String>builder()
                .message(goodMessage(name, number))
                .build();
    }

    public static Event<String> badEvent() {
        return Event.<String>builder()
                .message(anyString())
                .build();
    }

    public static String goodMessage() {
        return goodMessage(anyString(), anyInt());
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
