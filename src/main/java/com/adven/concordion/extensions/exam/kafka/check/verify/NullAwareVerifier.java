package com.adven.concordion.extensions.exam.kafka.check.verify;

import com.adven.concordion.extensions.exam.kafka.Event;
import org.apache.kafka.common.utils.Bytes;

/**
 * @author Ruslan Ustits
 */
public final class NullAwareVerifier implements Verifier {

    @Override
    public boolean verify(final Event<Bytes> first, final Event<String> second) {
        return first != null && second != null;
    }

}
