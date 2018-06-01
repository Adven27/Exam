package com.adven.concordion.extensions.exam.kafka;

import lombok.NonNull;
import org.apache.kafka.common.utils.Bytes;

import java.util.List;


public interface EventConsumer {

    List<Event<Bytes>> consume(@NonNull final String fromTopic);

}
