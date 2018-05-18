package com.adven.concordion.extensions.exam.kafka;

import com.google.protobuf.Message;

import java.util.Properties;

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
    public boolean produce(String topic, String key, Message message, Properties properties) {
        if (topic == null || message == null || properties == null) {
            throw new NullPointerException();
        }
        return result;
    }

}
