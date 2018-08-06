package com.adven.concordion.extensions.exam.kafka.check.verify;

import com.adven.concordion.extensions.exam.entities.Entity;
import com.adven.concordion.extensions.exam.kafka.Event;
import org.apache.kafka.common.utils.Bytes;

public final class NullAwareVerifier implements Verifier {

    @Override
    public boolean verify(final Event<Bytes> first, final Event<? extends Entity> second) {
        return first != null && second != null;
    }

}