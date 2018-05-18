package com.adven.concordion.extensions.exam.kafka;

import com.google.protobuf.Message;
import lombok.NonNull;

import java.util.Properties;

/**
 * @author Ruslan Ustits
 */
public interface EventProducer {

    boolean produce(@NonNull final String topic, final String key, @NonNull final Message message,
                    @NonNull final Properties properties);

}
