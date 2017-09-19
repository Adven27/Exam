package com.adven.concordion.extensions.exam.commands;

import com.adven.concordion.extensions.exam.html.Html;
import org.concordion.api.Element;
import org.concordion.api.ResultRecorder;
import org.concordion.api.listener.AssertEqualsListener;
import org.concordion.api.listener.AssertFailureEvent;
import org.concordion.api.listener.AssertSuccessEvent;
import org.concordion.internal.util.Announcer;

import static org.concordion.api.Result.FAILURE;
import static org.concordion.api.Result.SUCCESS;

public class ExamVerifyCommand extends ExamCommand {
    private Announcer<AssertEqualsListener> listeners = Announcer.to(AssertEqualsListener.class);

    public ExamVerifyCommand(String name, String tag, AssertEqualsListener listener) {
        super(name, tag);
        listeners.addListener(listener);
    }

    protected void success(ResultRecorder resultRecorder, Element element) {
        resultRecorder.record(SUCCESS);
        listeners.announce().successReported(new AssertSuccessEvent(element));
    }

    protected void success(ResultRecorder resultRecorder, Html element) {
        success(resultRecorder, element.el());
    }

    protected void failure(ResultRecorder resultRecorder, Element element, Object actual, String expected) {
        resultRecorder.record(FAILURE);
        listeners.announce().failureReported(new AssertFailureEvent(element, expected, actual));
    }

    protected void failure(ResultRecorder resultRecorder, Html element, Object actual, String expected) {
        failure(resultRecorder, element.el(), actual, expected);
    }
}