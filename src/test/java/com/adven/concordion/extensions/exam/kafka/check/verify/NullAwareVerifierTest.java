package com.adven.concordion.extensions.exam.kafka.check.verify;

import com.adven.concordion.extensions.exam.kafka.Event;
import com.adven.concordion.extensions.exam.entities.ProtoEntity;
import org.apache.kafka.common.utils.Bytes;
import org.junit.Before;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class NullAwareVerifierTest {

    private NullAwareVerifier verifier;

    @Before
    public void setUp() {
        verifier = new NullAwareVerifier();
    }

    @Test
    public void testVerify() {
        final boolean result = verifier.verify(Event.<Bytes>empty(), Event.<ProtoEntity>empty());
        assertThat(result).isTrue();
    }

    @Test
    public void testVerifyWithFirstNullEvent() {
        final boolean result = verifier.verify(null, Event.<ProtoEntity>empty());
        assertThat(result).isFalse();
    }

    @Test
    public void testVerifyWithSecondNullEvent() {
        final boolean result = verifier.verify(Event.<Bytes>empty(), null);
        assertThat(result).isFalse();
    }

}