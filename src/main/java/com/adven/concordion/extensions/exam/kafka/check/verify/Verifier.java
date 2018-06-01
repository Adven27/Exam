package com.adven.concordion.extensions.exam.kafka.check.verify;

import com.adven.concordion.extensions.exam.kafka.Event;
import org.apache.kafka.common.utils.Bytes;


public interface Verifier {

    boolean verify(final Event<Bytes> first, final Event<String> second);

}
