package com.adven.concordion.extensions.exam.kafka.check.verify;

import com.adven.concordion.extensions.exam.kafka.Entity;
import com.adven.concordion.extensions.exam.kafka.Event;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.common.utils.Bytes;

@RequiredArgsConstructor
public final class MessageVerifier implements Verifier {

    @Override
    public boolean verify(final Event<Bytes> first, final Event<? extends Entity> second) {
        return second.getMessage().isEqualTo(first.getMessage().get());
    }

}
