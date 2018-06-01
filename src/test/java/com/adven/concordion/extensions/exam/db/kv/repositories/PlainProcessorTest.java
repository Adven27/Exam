package com.adven.concordion.extensions.exam.db.kv.repositories;

import com.google.common.base.Optional;
import org.junit.Before;
import org.junit.Test;

import static com.adven.concordion.extensions.exam.RandomUtils.anyInt;
import static com.adven.concordion.extensions.exam.RandomUtils.anyString;
import static org.assertj.core.api.Assertions.assertThat;


public class PlainProcessorTest {

    private PlainProcessor processor;

    @Before
    public void setUp() {
        processor = new PlainProcessor();
    }

    @Test
    public void testConvert() {
        final String text = anyString();
        final Optional<String> result = processor.convert(text);
        assertThat(result).isEqualTo(Optional.of(text));
    }

    @Test
    public void testConvertWithNonString() {
        final int notText = anyInt();
        final Optional<String> result = processor.convert(notText);
        assertThat(result).isEqualTo(Optional.absent());
    }

    @Test
    public void testVerify() {
        final String single = anyString();
        final boolean result = processor.verify(single, single);
        assertThat(result).isTrue();
    }

    @Test
    public void testVerifyWithNotEqualString() {
        final String first = anyString();
        final String second = anyString();
        final boolean result = processor.verify(first, second);
        assertThat(result).isFalse();
    }

    @Test
    public void testVerifyIfValuesAreNull() {
        final boolean result = processor.verify(null, null);
        assertThat(result).isFalse();
    }
}