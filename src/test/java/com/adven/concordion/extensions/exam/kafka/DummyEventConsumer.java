package com.adven.concordion.extensions.exam.kafka;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author Ruslan Ustits
 */
public final class DummyEventConsumer implements EventConsumer {

    private final List<Event<String>> events;

    private DummyEventConsumer(final List<Event<String>> events) {
        this.events = events;
    }

    public static DummyEventConsumer returning(final Event<String>... events) {
        return new DummyEventConsumer(Arrays.asList(events));
    }

    public static DummyEventConsumer defaultInstance() {
        return new DummyEventConsumer(new ArrayList<Event<String>>());
    }

    public DummyEventConsumer addEventToReturn(final Event<String> event) {
        events.add(event);
        return this;
    }

    @Override
    public List<Event<String>> consume(final String fromTopic) {
        if (fromTopic == null) {
            throw new NullPointerException();
        }
        return events;
    }

}
