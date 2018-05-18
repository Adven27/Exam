package com.adven.concordion.extensions.exam.kafka;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

/**
 * @author Ruslan Ustits
 */
public final class DummyEventConsumer implements EventConsumer {

    private final List<Event> events;

    private DummyEventConsumer(final List<Event> events) {
        this.events = events;
    }

    public static DummyEventConsumer returning(final Event... events) {
        return new DummyEventConsumer(Arrays.asList(events));
    }

    public static DummyEventConsumer defaultInstance() {
        return new DummyEventConsumer(new ArrayList<Event>());
    }

    public DummyEventConsumer addEventToReturn(final Event event) {
        events.add(event);
        return this;
    }

    @Override
    public List<Event> consume(final String fromTopic, final Properties properties) {
        if (fromTopic == null || properties == null) {
            throw new NullPointerException();
        }
        return events;
    }

}
