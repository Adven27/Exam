package com.adven.concordion.extensions.exam.kafka;

/**
 * @author Ruslan Ustits
 */
public interface EventProcessor {

    boolean configureReply(final Event event, final String eventClass);

    Event consume(final String fromTopic);

    boolean reply();

    boolean send(final Event event);

}
