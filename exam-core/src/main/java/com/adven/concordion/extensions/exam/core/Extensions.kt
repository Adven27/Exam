package com.adven.concordion.extensions.exam.core

import org.concordion.api.Resource
import org.concordion.api.extension.ConcordionExtender
import org.concordion.api.extension.ConcordionExtension

class CodeMirrorExtension : ConcordionExtension {
    override fun addTo(e: ConcordionExtender) {
        e.apply {
            listOf(
                "codemirror.css",
                "merge.css",
                "enable-codemirror.css",
                "foldgutter.css",
                "simplescrollbar.css"
            ).forEach { withLinkedCSS("$BASE$it", Resource("$BASE$it")) }
            listOf(
                "codemirror.js",
                "enable-codemirror.js",
                "javascript.js",
                "xml.js",
                "htmlmixed.js",
                "formatting.js",
                "markdown.js",
                "diff_match_patch.js",
                "merge.js",
                "xml-fold.js",
                "simplescrollbar.js"
            ).forEach { withLinkedJavaScript("$BASE$it", Resource("$BASE$it")) }
        }
    }

    companion object {
        const val BASE = "/codemirror/"
    }
}

class BootstrapExtension : ConcordionExtension {
    override fun addTo(e: ConcordionExtender) {
        e.withLinkedJavaScript(BOOTSTRAP_JQUERY, Resource(BOOTSTRAP_JQUERY))
        e.withLinkedJavaScript(BOOTSTRAP_POPPER, Resource(BOOTSTRAP_POPPER))
        e.withLinkedJavaScript(BOOTSTRAP, Resource(BOOTSTRAP))
        e.withLinkedJavaScript(BOOTSTRAP_SIDEBAR, Resource(BOOTSTRAP_SIDEBAR))
        e.withLinkedCSS(ENABLE_BOOTSTRAP_CSS, Resource(ENABLE_BOOTSTRAP_CSS))
        e.withLinkedCSS(BOOTSTRAP_CSS, Resource(BOOTSTRAP_CSS))
        e.withLinkedCSS(BOOTSTRAP_CALLOUT_CSS, Resource(BOOTSTRAP_CALLOUT_CSS))
        e.withLinkedCSS(BOOTSTRAP_FA_CSS, Resource(BOOTSTRAP_FA_CSS))
        e.withResource(BOOTSTRAP_FA_FONT, Resource(BOOTSTRAP_FA_FONT))
        e.withLinkedCSS(SCROLL_TO_TOP_CSS, Resource(SCROLL_TO_TOP_CSS))
        e.withLinkedJavaScript(SCROLL_TO_TOP_JS, Resource(SCROLL_TO_TOP_JS))
    }

    companion object {
        private const val BOOTSTRAP_JQUERY = "/bootstrap/jquery-3.2.1.slim.min.js"
        private const val BOOTSTRAP_POPPER = "/bootstrap/popper.min.js"
        private const val BOOTSTRAP = "/bootstrap/bootstrap.min.js"
        private const val ENABLE_BOOTSTRAP_CSS = "/bootstrap/enable-bootstrap.css"
        private const val BOOTSTRAP_SIDEBAR = "/bootstrap/sidebar.js"
        private const val BOOTSTRAP_CSS = "/bootstrap/bootstrap.min.css"
        private const val BOOTSTRAP_CALLOUT_CSS = "/bootstrap/callout.css"
        private const val BOOTSTRAP_FA_CSS = "/bootstrap/font-awesome.min.css"
        private const val BOOTSTRAP_FA_FONT = "/bootstrap/fonts/fontawesome-webfont.woff2"
        private const val SCROLL_TO_TOP_CSS = "/bootstrap/scrollToTop.css"
        private const val SCROLL_TO_TOP_JS = "/bootstrap/scrollToTop.js"
    }
}
