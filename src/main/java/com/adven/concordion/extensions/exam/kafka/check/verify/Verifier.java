package com.adven.concordion.extensions.exam.kafka.check.verify;

import com.adven.concordion.extensions.exam.kafka.Event;
import com.adven.concordion.extensions.exam.kafka.protobuf.ProtoEntity;
import org.apache.kafka.common.utils.Bytes;

/**
 * @author Ruslan Ustits
 */
public interface Verifier {

    boolean verify(final Event<Bytes> first, final Event<ProtoEntity> second);

}
