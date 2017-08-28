package com.adven.concordion.extensions.exam.rest.commands;

import com.adven.concordion.extensions.exam.html.Html;
import com.adven.concordion.extensions.exam.rest.JsonPrettyPrinter;
import com.adven.concordion.extensions.exam.rest.RestResultRenderer;
import org.concordion.api.AbstractCommand;
import org.concordion.api.Element;
import org.concordion.api.ResultRecorder;
import org.concordion.api.listener.AssertEqualsListener;
import org.concordion.api.listener.AssertFailureEvent;
import org.concordion.api.listener.AssertSuccessEvent;
import org.concordion.internal.util.Announcer;

import static org.concordion.api.Result.FAILURE;
import static org.concordion.api.Result.SUCCESS;

public class RestVerifyCommand extends AbstractCommand {
    protected final JsonPrettyPrinter printer = new JsonPrettyPrinter();
    private Announcer<AssertEqualsListener> listeners = Announcer.to(AssertEqualsListener.class);

    public RestVerifyCommand() {
        listeners.addListener(new RestResultRenderer());
    }

    protected void success(ResultRecorder resultRecorder, Element element) {
        resultRecorder.record(SUCCESS);
        listeners.announce().successReported(new AssertSuccessEvent(element));
    }

    protected void success(ResultRecorder resultRecorder, Html element) {
        success(resultRecorder, element.el());
    }

    protected void failure(ResultRecorder resultRecorder, Element element, String actual, String expected) {
        resultRecorder.record(FAILURE);
        listeners.announce().failureReported(new AssertFailureEvent(element, expected, actual));
    }

    protected void failure(ResultRecorder resultRecorder, Html element, String actual, String expected) {
        failure(resultRecorder, element.el(), actual, expected);
    }
}