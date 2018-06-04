package com.adven.concordion.extensions.exam.kafka.check.verify;

import com.adven.concordion.extensions.exam.kafka.Event;
import com.adven.concordion.extensions.exam.kafka.protobuf.ProtoEntity;
import org.apache.kafka.common.utils.Bytes;

import java.util.ArrayList;
import java.util.List;

import static java.util.Arrays.asList;

/**
 * @author Ruslan Ustits
 */
public final class CompositeVerifier implements Verifier {

    private final List<Verifier> verifiers = new ArrayList<>();

    public CompositeVerifier(final Verifier... verifiers) {
        this.verifiers.addAll(asList(verifiers));
    }

    @Override
    public boolean verify(final Event<Bytes> first, final Event<ProtoEntity> second) {
        for (final Verifier verifier : verifiers) {
            final boolean result = verifier.verify(first, second);
            if (!result) {
                return false;
            }
        }
        return true;
    }

}
