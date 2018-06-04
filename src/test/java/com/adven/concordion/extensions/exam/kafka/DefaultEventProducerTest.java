package com.adven.concordion.extensions.exam.kafka;

import com.adven.concordion.extensions.exam.kafka.protobuf.ProtoEntity;
import org.junit.Before;
import org.junit.Test;

import java.util.Properties;

import static com.adven.concordion.extensions.exam.RandomUtils.anyLong;
import static com.adven.concordion.extensions.exam.RandomUtils.anyString;

/**
 * @author Ruslan Ustits
 */
public class DefaultEventProducerTest {

    private DefaultEventProducer producer;

    @Before
    public void setUp() {
        producer = new DefaultEventProducer(1000L, new Properties());
    }

    @Test(expected = NullPointerException.class)
    public void testProduceWithNullTopic() {
        producer.produce(null, anyString(), new ProtoEntity(anyString(), anyString()));
    }

    @Test(expected = NullPointerException.class)
    public void testProduceWithNullMessage() {
        producer.produce(anyString(), anyString(), null);
    }

    @Test(expected = NullPointerException.class)
    public void testCreateInstanceWithNullProperty() {
        new DefaultEventProducer(anyLong(), (Properties) null);
    }
}