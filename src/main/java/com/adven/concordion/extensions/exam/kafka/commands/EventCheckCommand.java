package com.adven.concordion.extensions.exam.kafka.commands;

import com.adven.concordion.extensions.exam.commands.ExamCommand;
import com.adven.concordion.extensions.exam.kafka.EventProcessor;

public class EventCheckCommand extends ExamCommand {

    EventProcessor eventProcessor;

    public EventCheckCommand(String name, String tag, EventProcessor eventProcessor) {
        super(name, tag);
        this.eventProcessor = eventProcessor;
    }
}
