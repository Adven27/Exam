package com.adven.concordion.extensions.exam.db;

import com.adven.concordion.extensions.exam.rest.RestResultRenderer;
import org.concordion.api.listener.AssertEqualsListener;
import org.concordion.api.listener.AssertFalseListener;
import org.concordion.api.listener.AssertSuccessEvent;
import org.concordion.api.listener.AssertTrueListener;

public class DbResultRenderer extends RestResultRenderer
        implements AssertEqualsListener, AssertTrueListener, AssertFalseListener {

    @Override
    public void successReported(AssertSuccessEvent event) {
        event.getElement().addStyleClass("table-success").appendNonBreakingSpaceIfBlank();
    }
}