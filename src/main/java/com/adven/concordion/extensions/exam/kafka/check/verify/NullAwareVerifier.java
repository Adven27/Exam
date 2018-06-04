package com.adven.concordion.extensions.exam.kafka.check.verify;

import com.adven.concordion.extensions.exam.kafka.Event;
import com.adven.concordion.extensions.exam.kafka.protobuf.ProtoEntity;
import org.apache.kafka.common.utils.Bytes;


public final class NullAwareVerifier implements Verifier {

    @Override
    public boolean verify(final Event<Bytes> first, final Event<ProtoEntity> second) {
        return first != null && second != null;
    }

}
