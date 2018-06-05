package com.adven.concordion.extensions.exam.kafka;

import com.adven.concordion.extensions.exam.kafka.protobuf.ProtoEntity;

import java.util.ArrayList;
import java.util.List;

public final class DummyEventProducer implements EventProducer {

    private boolean result;

    private List<ProtoEntity> producedMessage = new ArrayList<>();

    public static DummyEventProducer defaultInstance() {
        return new DummyEventProducer();
    }

    public void mustReturnTrue() {
        result = true;
    }

    public void mustReturnFalse() {
        result = false;
    }

    public List<ProtoEntity> popProducedMessages() {
        final List<ProtoEntity> messageToPop = producedMessage;
        producedMessage = new ArrayList<>();
        return messageToPop;
    }

    @Override
    public boolean produce(String topic, String key, ProtoEntity message) {
        if (topic == null || message == null) {
            throw new NullPointerException();
        }
        producedMessage.add(message);
        return result;
    }

    @Override
    public boolean produce(String topic, String key, EventHeader eventHeader, ProtoEntity message) {
        return produce(topic, key, message);
    }

}
