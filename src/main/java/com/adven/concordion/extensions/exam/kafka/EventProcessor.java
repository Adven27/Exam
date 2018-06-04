package com.adven.concordion.extensions.exam.kafka;

import com.adven.concordion.extensions.exam.kafka.protobuf.ProtoEntity;

public interface EventProcessor {

    boolean check(final Event<ProtoEntity> eventToCheck, final boolean isAsync);

    boolean checkWithReply(final Event<ProtoEntity> eventToCheck, final Event<ProtoEntity> replySuccessEvent,
                           final Event<ProtoEntity> replyFailEvent, final boolean isAsync);

    boolean send(final Event<ProtoEntity> event);

}
