package com.adven.concordion.extensions.exam.kafka;

import com.google.protobuf.Message;
import lombok.NonNull;

/**
 * @author Ruslan Ustits
 */
public interface EventProducer {

    boolean produce(@NonNull final String topic, final String key, @NonNull final Message message);

    boolean produce(@NonNull final String topic, final String key, final EventHeader eventHeader,
                    @NonNull final Message message);

}
