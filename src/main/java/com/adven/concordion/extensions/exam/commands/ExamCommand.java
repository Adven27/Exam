package com.adven.concordion.extensions.exam.commands;

import org.concordion.api.*;
import org.concordion.api.listener.ExecuteEvent;
import org.concordion.api.listener.ExecuteListener;
import org.concordion.internal.util.Announcer;

public class ExamCommand extends AbstractCommand {
    private Announcer<ExecuteListener> listeners = Announcer.to(ExecuteListener.class);

    @Override
    public void execute(CommandCall commandCall, Evaluator evaluator, ResultRecorder resultRecorder) {
        commandCall.getChildren().processSequentially(evaluator, resultRecorder);
        announceExecuteCompleted(commandCall.getElement());
    }

    private void announceExecuteCompleted(Element element) {
        listeners.announce().executeCompleted(new ExecuteEvent(element));
    }
}