package com.adven.concordion.extensions.exam.kafka;

/**
 * @author Ruslan Ustits
 */
public interface EventProcessor {

    boolean configureReply(final Event<String> event, final String eventClass);

    Event<String> consume(final String fromTopic);

    boolean reply();

    boolean send(final Event<String> event, final String eventClass);

    boolean hasReply();
}
