package com.adven.concordion.extensions.exam.bootstrap;

import org.concordion.api.Resource;
import org.concordion.api.extension.ConcordionExtender;
import org.concordion.api.extension.ConcordionExtension;

public class BootstrapExtension implements ConcordionExtension {

    @Override
    public void addTo(ConcordionExtender e) {
        e.withLinkedJavaScript("/bootstrap/jquery-3.2.1.slim.min.js", new Resource("/bootstrap/jquery-3.2.1.slim.min.js"));
        e.withLinkedJavaScript("/bootstrap/popper.min.js", new Resource("/bootstrap/popper.min.js"));
        e.withLinkedJavaScript("/bootstrap/bootstrap.min.js", new Resource("/bootstrap/bootstrap.min.js"));
        e.withLinkedJavaScript("/bootstrap/sidebar.js", new Resource("/bootstrap/sidebar.js"));
        e.withLinkedCSS("/bootstrap/enable-bootstrap.css", new Resource("/bootstrap/enable-bootstrap.css"));
        e.withLinkedCSS("/bootstrap/bootstrap.min.css", new Resource("/bootstrap/bootstrap.min.css"));
        e.withLinkedCSS("/bootstrap/callout.css", new Resource("/bootstrap/callout.css"));
    }
}