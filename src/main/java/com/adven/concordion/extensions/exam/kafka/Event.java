package com.adven.concordion.extensions.exam.kafka;

import lombok.Builder;
import lombok.Value;

/**
 * @author Ruslan Ustits
 */
@Value
@Builder
public class Event<T> {

    String topicName;
    String key;
    T message;

    public static <T> Event<T> empty() {
        return Event.<T>builder().build();
    }

}
