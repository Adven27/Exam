package com.adven.concordion.extensions.exam.kafka;

import com.adven.concordion.extensions.exam.entities.Entity;
import lombok.NonNull;

public interface EventProducer {

    boolean produce(@NonNull final String topic, final String key, @NonNull final Entity message);

    boolean produce(@NonNull final String topic, final String key, final EventHeader eventHeader,
                    @NonNull final Entity message);

}
