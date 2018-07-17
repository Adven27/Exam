package com.adven.concordion.extensions.exam.entities;

import com.google.common.base.Optional;
import org.junit.Before;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class EmptyEntityTest {

    private EmptyEntity entity;

    @Before
    public void setUp() {
        entity = new EmptyEntity();
    }

    @Test
    public void testIsEqualToEmptyByteArray() {
        assertThat(entity.isEqualTo(new byte[0])).isTrue();
    }

    @Test
    public void testIsEqualToNull() {
        assertThat(entity.isEqualTo((byte[]) null)).isTrue();
    }

    @Test
    public void testToBytes() {
        assertThat(entity.toBytes()).isEmpty();
    }

    @Test
    public void testOriginal() {
        assertThat(entity.original()).isEqualTo(Optional.absent());
    }

}