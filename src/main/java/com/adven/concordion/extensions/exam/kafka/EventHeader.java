package com.adven.concordion.extensions.exam.kafka;

import lombok.NonNull;

import java.util.Arrays;


public final class EventHeader {

    public static final String REPLY_TOPIC = "kafka_replyTopic";
    public static final String CORRELATION_ID = "kafka_correlationId";

    private final byte[] replyToTopic;
    private final byte[] correlationId;

    public EventHeader(@NonNull final byte[] replyToTopic, @NonNull final byte[] correlationId) {
        this.replyToTopic = Arrays.copyOf(replyToTopic, replyToTopic.length);
        this.correlationId = Arrays.copyOf(correlationId, correlationId.length);
    }

    public static EventHeader empty() {
        return new EventHeader(new byte[]{}, new byte[]{});
    }

    public byte[] getReplyToTopic() {
        return Arrays.copyOf(replyToTopic, replyToTopic.length);
    }

    public byte[] getCorrelationId() {
        return Arrays.copyOf(correlationId, correlationId.length);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        EventHeader header = (EventHeader) o;
        return Arrays.equals(replyToTopic, header.replyToTopic) && Arrays.equals(correlationId, header.correlationId);
    }

    @Override
    public int hashCode() {
        int result = Arrays.hashCode(replyToTopic);
        result = 31 * result + Arrays.hashCode(correlationId);
        return result;
    }
}
