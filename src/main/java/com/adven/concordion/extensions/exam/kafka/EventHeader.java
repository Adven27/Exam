package com.adven.concordion.extensions.exam.kafka;

import lombok.Value;

/**
 * @author Ruslan Ustits
 */
@Value
public class EventHeader {

    public static final String REPLY_TOPIC = "kafka_replyTopic";
    public static final String CORRELATION_ID = "kafka_correlationId";

    String replyToTopic;
    String correlationId;

    public static EventHeader empty() {
        return new EventHeader("", "");
    }

}
