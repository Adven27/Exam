package com.adven.concordion.extensions.exam.configurators;

import com.adven.concordion.extensions.exam.ExamExtension;
import com.adven.concordion.extensions.exam.kafka.DefaultEventProcessor;
import lombok.RequiredArgsConstructor;

/**
 * @author Ruslan Ustits
 */
@RequiredArgsConstructor
public final class KafkaTester {

    private final ExamExtension examExtension;

    private String brokers;

    public KafkaTester brokers(final String brokers) {
        this.brokers = brokers;
        return this;
    }

    public ExamExtension end() {
        if (brokers == null) {
            throw new ConfigurationException("Brokers must be specified in order to use kafka module");
        }
        return examExtension.kafka(new DefaultEventProcessor(brokers));
    }

}
