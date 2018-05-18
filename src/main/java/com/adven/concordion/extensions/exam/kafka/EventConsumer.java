package com.adven.concordion.extensions.exam.kafka;

import lombok.NonNull;

import java.util.List;

/**
 * @author Ruslan Ustits
 */
public interface EventConsumer {

    List<Event<String>> consume(@NonNull final String fromTopic);

}
