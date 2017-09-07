package com.adven.concordion.extensions.exam.bootstrap;

import org.concordion.api.Resource;
import org.concordion.api.extension.ConcordionExtender;
import org.concordion.api.extension.ConcordionExtension;

public class BootstrapExtension implements ConcordionExtension {
    private static final String BOOTSTRAP_JQUERY = "/bootstrap/jquery-3.2.1.slim.min.js";
    private static final String BOOTSTRAP_POPPER = "/bootstrap/popper.min.js";
    private static final String BOOTSTRAP = "/bootstrap/bootstrap.min.js";
    private static final String ENABLE_BOOTSTRAP_CSS = "/bootstrap/enable-bootstrap.css";
    private static final String BOOTSTRAP_SIDEBAR = "/bootstrap/sidebar.js";
    private static final String BOOTSTRAP_CSS = "/bootstrap/bootstrap.min.css";
    private static final String BOOTSTRAP_CALLOUT_CSS = "/bootstrap/callout.css";

    @Override
    public void addTo(ConcordionExtender e) {
        e.withLinkedJavaScript(BOOTSTRAP_JQUERY, new Resource(BOOTSTRAP_JQUERY));
        e.withLinkedJavaScript(BOOTSTRAP_POPPER, new Resource(BOOTSTRAP_POPPER));
        e.withLinkedJavaScript(BOOTSTRAP, new Resource(BOOTSTRAP));
        e.withLinkedJavaScript(BOOTSTRAP_SIDEBAR, new Resource(BOOTSTRAP_SIDEBAR));
        e.withLinkedCSS(ENABLE_BOOTSTRAP_CSS, new Resource(ENABLE_BOOTSTRAP_CSS));
        e.withLinkedCSS(BOOTSTRAP_CSS, new Resource(BOOTSTRAP_CSS));
        e.withLinkedCSS(BOOTSTRAP_CALLOUT_CSS, new Resource(BOOTSTRAP_CALLOUT_CSS));
    }
}