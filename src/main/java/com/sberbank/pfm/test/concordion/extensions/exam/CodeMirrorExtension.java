package com.sberbank.pfm.test.concordion.extensions.exam;

import org.concordion.api.Resource;
import org.concordion.api.extension.ConcordionExtender;
import org.concordion.api.extension.ConcordionExtension;

public class CodeMirrorExtension implements ConcordionExtension {

    public static final String BASE = "/codemirror/";

    @Override
    public void addTo(ConcordionExtender e) {
        e.withLinkedCSS(BASE + "codemirror.css", new Resource(BASE + "codemirror.css"));
        e.withLinkedCSS(BASE + "merge.css", new Resource(BASE + "merge.css"));
        e.withLinkedCSS(BASE + "enable-codemirror.css", new Resource(BASE + "enable-codemirror.css"));

        e.withLinkedJavaScript(BASE + "codemirror.js", new Resource(BASE + "codemirror.js"));
        e.withLinkedJavaScript(BASE + "javascript.js", new Resource(BASE + "javascript.js"));
        e.withLinkedJavaScript(BASE + "xml.js", new Resource(BASE + "xml.js"));
        e.withLinkedJavaScript(BASE + "diff_match_patch.js", new Resource(BASE + "diff_match_patch.js"));
        e.withLinkedJavaScript(BASE + "merge.js", new Resource(BASE + "merge.js"));
        e.withLinkedJavaScript(BASE + "enable-codemirror.js", new Resource(BASE + "enable-codemirror.js"));
    }
}