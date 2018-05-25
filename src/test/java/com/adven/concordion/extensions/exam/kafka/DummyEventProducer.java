package com.adven.concordion.extensions.exam.kafka;

import com.google.protobuf.Message;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Ruslan Ustits
 */
public final class DummyEventProducer implements EventProducer {

    private boolean result;

    private List<Message> producedMessage = new ArrayList<>();

    public static DummyEventProducer defaultInstance() {
        return new DummyEventProducer();
    }

    public void mustReturnTrue() {
        result = true;
    }

    public void mustReturnFalse() {
        result = false;
    }

    public List<Message> popProducedMessages() {
        final List<Message> messageToPop = producedMessage;
        producedMessage = new ArrayList<>();
        return messageToPop;
    }

    @Override
    public boolean produce(String topic, String key, Message message) {
        if (topic == null || message == null) {
            throw new NullPointerException();
        }
        producedMessage.add(message);
        return result;
    }

    @Override
    public boolean produce(String topic, String key, EventHeader eventHeader, Message message) {
        return produce(topic, key, message);
    }

}
