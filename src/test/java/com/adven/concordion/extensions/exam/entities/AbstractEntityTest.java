package com.adven.concordion.extensions.exam.entities;

import org.junit.Test;

import static com.adven.concordion.extensions.exam.RandomUtils.anyString;
import static org.assertj.core.api.Assertions.assertThat;

public class AbstractEntityTest {

    @Test
    public void testCleanup() {
        final String expected = anyString();
        final AbstractEntity entity = new MockEntity();
        final String result = entity.cleanup("\n" + expected + "\t");
        assertThat(result).isEqualTo(expected);
    }

}