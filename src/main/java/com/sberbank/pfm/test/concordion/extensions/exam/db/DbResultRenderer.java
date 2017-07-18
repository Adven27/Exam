package com.sberbank.pfm.test.concordion.extensions.exam.db;

import com.sberbank.pfm.test.concordion.extensions.exam.rest.RestResultRenderer;
import org.concordion.api.listener.AssertEqualsListener;
import org.concordion.api.listener.AssertFalseListener;
import org.concordion.api.listener.AssertTrueListener;

public class DbResultRenderer extends RestResultRenderer
        implements AssertEqualsListener, AssertTrueListener, AssertFalseListener {
}