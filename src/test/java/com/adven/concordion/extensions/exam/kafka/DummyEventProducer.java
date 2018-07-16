package com.adven.concordion.extensions.exam.kafka;

import com.adven.concordion.extensions.exam.entities.Entity;

import java.util.ArrayList;
import java.util.List;

public final class DummyEventProducer implements EventProducer {

    private boolean result;

    private List<Entity> producedMessage = new ArrayList<>();

    public static DummyEventProducer defaultInstance() {
        return new DummyEventProducer();
    }

    public void mustReturnTrue() {
        result = true;
    }

    public void mustReturnFalse() {
        result = false;
    }

    public List<Entity> popProducedMessages() {
        final List<Entity> messageToPop = producedMessage;
        producedMessage = new ArrayList<>();
        return messageToPop;
    }

    @Override
    public boolean produce(String topic, String key, Entity message) {
        if (topic == null || message == null) {
            throw new NullPointerException();
        }
        producedMessage.add(message);
        return result;
    }

    @Override
    public boolean produce(String topic, String key, EventHeader eventHeader, Entity message) {
        return produce(topic, key, message);
    }

}
