package com.adven.concordion.extensions.exam.ws;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;


public class StatusBuilderTest {

    private StatusBuilder builder;

    @Before
    public void setUp() {
        final String protocol = "protocol";
        final String status = "status";
        final String reason = "reason";
        builder = new StatusBuilder(protocol, status, reason);
    }

    @Test
    public void testBuild() {
        final String result = builder.build();
        assertEquals("protocol status reason", result);
    }

    @Test
    public void testClean() {
        assertEquals("some text with spaces", builder.clean("some text with spaces  "));
    }

}