package com.adven.concordion.extensions.exam.kafka;

import lombok.Builder;
import lombok.Value;

/**
 * @author Ruslan Ustits
 */
@Value
@Builder
public class Event {

    String topicName;
    String message;

}
