package com.adven.concordion.extensions.exam.kafka;

import com.adven.concordion.extensions.exam.entities.Entity;

public interface EventProcessor {

    boolean check(final Event<? extends Entity> eventToCheck, final boolean isAsync);

    boolean checkWithReply(final Event<? extends Entity> eventToCheck, final Event<? extends Entity> replySuccessEvent,
                           final Event<? extends Entity> replyFailEvent, final boolean isAsync);

    boolean send(final Event<? extends Entity> event);

}
