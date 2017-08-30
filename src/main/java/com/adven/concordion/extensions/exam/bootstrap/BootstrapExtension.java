package com.adven.concordion.extensions.exam.bootstrap;

import org.concordion.api.Resource;
import org.concordion.api.extension.ConcordionExtender;
import org.concordion.api.extension.ConcordionExtension;

public class BootstrapExtension implements ConcordionExtension {

    @Override
    public void addTo(ConcordionExtender e) {
        e.withLinkedCSS("/bootstrap/bootstrap.min.js", new Resource("/bootstrap/bootstrap.min.js"));
        e.withLinkedCSS("/bootstrap/co.scss", new Resource("/bootstrap/co.scss"));
        e.withLinkedCSS("/bootstrap/bootstrap.min.css", new Resource("/bootstrap/bootstrap.min.css"));
        e.withLinkedCSS("/bootstrap/enable-bootstrap.css", new Resource("/bootstrap/enable-bootstrap.css"));
    }
}