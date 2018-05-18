package com.adven.concordion.extensions.exam.kafka;

import lombok.NonNull;

import java.util.List;
import java.util.Properties;

/**
 * @author Ruslan Ustits
 */
public interface EventConsumer {

    List<Event> consume(@NonNull final String fromTopic, @NonNull final Properties properties);

}
