package com.adven.concordion.extensions.exam.kafka;

import org.junit.Before;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class EmptyEntityTest {

    private EmptyEntity entity;

    @Before
    public void setUp() throws Exception {
        entity = new EmptyEntity();
    }

    @Test
    public void testIsEqualToEmptyByteArray() {
        assertThat(entity.isEqualTo(new byte[0])).isTrue();
    }

    @Test
    public void testIsEqualToNull() {
        assertThat(entity.isEqualTo(null)).isTrue();
    }

    @Test
    public void testToBytes() {
        assertThat(entity.toBytes()).isEmpty();
    }

}