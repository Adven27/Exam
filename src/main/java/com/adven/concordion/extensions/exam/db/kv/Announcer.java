package com.adven.concordion.extensions.exam.db.kv;

import lombok.val;
import org.concordion.api.Element;
import org.concordion.api.ResultRecorder;
import org.concordion.api.listener.AssertEqualsListener;
import org.concordion.api.listener.AssertFailureEvent;
import org.concordion.api.listener.AssertSuccessEvent;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.concordion.api.Result.FAILURE;
import static org.concordion.api.Result.SUCCESS;


public final class Announcer {

    private final List<AssertEqualsListener> listeners = new ArrayList<>();

    public Announcer(final AssertEqualsListener... listeners) {
        this.listeners.addAll(Arrays.asList(listeners));
    }

    public void failure(ResultRecorder resultRecorder, Element element, Object actual, String expected) {
        resultRecorder.record(FAILURE);
        for (val listener : listeners) {
            listener.failureReported(new AssertFailureEvent(element, expected, actual));
        }
    }

    public void success(ResultRecorder resultRecorder, Element element) {
        resultRecorder.record(SUCCESS);
        for (val listener : listeners) {
            listener.successReported(new AssertSuccessEvent(element));
        }
    }

}
