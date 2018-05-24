package com.adven.concordion.extensions.exam.kafka.check.verify;

import com.adven.concordion.extensions.exam.kafka.Event;
import org.apache.kafka.common.utils.Bytes;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Ruslan Ustits
 */
public class CompositeVerifierTest {

    @Test
    public void testVerify() {
        final CompositeVerifier verifier = new CompositeVerifier(
                MockVerifier.returningTrue(),
                MockVerifier.returningTrue());
        final boolean result = verifier.verify(Event.<Bytes>empty(), Event.<String>empty());
        assertThat(result).isTrue();
    }

    @Test
    public void testVerifyWithNoVerifiers() {
        final CompositeVerifier verifier = new CompositeVerifier();
        final boolean result = verifier.verify(Event.<Bytes>empty(), Event.<String>empty());
        assertThat(result).isTrue();
    }

    @Test
    public void testFailedVerify() {
        final CompositeVerifier verifier = new CompositeVerifier(
                MockVerifier.returningTrue(),
                MockVerifier.returningFalse(),
                MockVerifier.returningTrue());
        final boolean result = verifier.verify(Event.<Bytes>empty(), Event.<String>empty());
        assertThat(result).isFalse();
    }

}