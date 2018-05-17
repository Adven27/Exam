package com.adven.concordion.extensions.exam.kafka.commands;

import com.adven.concordion.extensions.exam.commands.ExamCommand;
import com.adven.concordion.extensions.exam.kafka.EventProcessor;

public class EventReplyCommand extends ExamCommand {

    EventProcessor eventProcessor;

    public EventReplyCommand(String name, String tag, EventProcessor eventProcessor) {
        super(name, tag);
        this.eventProcessor = eventProcessor;
    }
}
