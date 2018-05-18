package com.adven.concordion.extensions.exam.kafka;

import com.google.protobuf.Message;

/**
 * @author Ruslan Ustits
 */
public final class DummyEventProducer implements EventProducer {

    private boolean result;

    public static DummyEventProducer defaultInstance() {
        return new DummyEventProducer();
    }

    public void mustReturnTrue() {
        result = true;
    }

    public void mustReturnFalse() {
        result = false;
    }

    @Override
    public boolean produce(String topic, String key, Message message) {
        if (topic == null || message == null) {
            throw new NullPointerException();
        }
        return result;
    }

}
