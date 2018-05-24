package com.adven.concordion.extensions.exam.kafka;

/**
 * @author Ruslan Ustits
 */
public interface EventProcessor {

    boolean check(final Event<String> eventToCheck, final String eventToCheckClass, final boolean isAsync);

    boolean checkWithReply(final Event<String> eventToCheck, final String eventToCheckClass,
                           final Event<String> replySuccessEvent, final Event<String> replyFailEvent, final String replyEventClass,
                           final boolean isAsync);

    boolean send(final Event<String> event, final String eventClass);

}
