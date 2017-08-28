package com.adven.concordion.extensions.exam.files;

import org.concordion.api.Element;
import org.concordion.api.listener.*;

public class FilesResultRenderer implements AssertEqualsListener, AssertTrueListener, AssertFalseListener {

    public void failureReported(AssertFailureEvent event) {
        Element element = event.getElement();
        element.addStyleClass("rest-failure");

        Element expected = new Element("del");
        expected.addStyleClass("expected");
        element.moveChildrenTo(expected);
        element.appendChild(expected);
        expected.appendNonBreakingSpaceIfBlank();

        Element actual = new Element("ins");
        actual.addStyleClass("actual");
        actual.appendText(convertToString(event.getActual()) +
                (event.getExpected() == null ? " (surplus)" : ""));
        actual.appendNonBreakingSpaceIfBlank();

        element.appendText("\n");
        element.appendChild(actual);
    }

    public void successReported(AssertSuccessEvent event) {
        event.getElement().addStyleClass("rest-success").appendNonBreakingSpaceIfBlank();
    }

    private String convertToString(Object object) {
        return object == null ? "(absent)" : "" + object;
    }
}