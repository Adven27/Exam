package com.adven.concordion.extensions.exam.kafka;

import lombok.Builder;
import lombok.Value;

@Value
@Builder(toBuilder = true)
public class Event<T> {

    String topicName;
    String key;
    T message;
    EventHeader header;
    String verifier;

    public static <T> Event<T> empty() {
        return Event.<T>builder().build();
    }

}
