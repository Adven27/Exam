package io.github.adven27.concordion.extensions.exam.core

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
                "addon/mode/simple.js",
                "http.js",
                "formatting.js",
                "markdown.js",
                "diff_match_patch.js",
                "merge.js",
                "xml-fold.js",
                "simplescrollbar.js"
            ).forEach { withLinkedJavaScript("$BASE$it", Resource("$BASE$it")) }
            withLinkedCSS("${BASE}theme/darcula.css", Resource("${BASE}theme/darcula.css"))
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
//        e.withLinkedJavaScript(BOOTSTRAP_BUNDLE_MAP, Resource(BOOTSTRAP_BUNDLE_MAP))

        e.withLinkedCSS(ENABLE_BOOTSTRAP_CSS, Resource(ENABLE_BOOTSTRAP_CSS))
        e.withLinkedCSS(BOOTSTRAP_CSS, Resource(BOOTSTRAP_CSS))

        e.withLinkedCSS(DOC_CSS, Resource(DOC_CSS))
        e.withLinkedJavaScript(DOC_JS, Resource(DOC_JS))

        e.withLinkedCSS(HIGHLIGHT_CSS, Resource(HIGHLIGHT_CSS))
        e.withLinkedJavaScript(HIGHLIGHT_JS, Resource(HIGHLIGHT_JS))
//        e.withLinkedJavaScript(BOOTSTRAP_CSS_MAP, Resource(BOOTSTRAP_CSS_MAP))
//        e.withLinkedCSS(BOOTSTRAP_REBOOT_CSS, Resource(BOOTSTRAP_REBOOT_CSS))
//        e.withLinkedCSS(BOOTSTRAP_GRID, Resource(BOOTSTRAP_GRID))
//        e.withLinkedCSS(BOOTSTRAP_UTILITIES, Resource(BOOTSTRAP_UTILITIES))
//        e.withLinkedCSS(BOOTSTRAP_CALLOUT_CSS, Resource(BOOTSTRAP_CALLOUT_CSS))
        e.withLinkedCSS(BOOTSTRAP_FA_CSS_A, Resource(BOOTSTRAP_FA_CSS_A))
        e.withLinkedCSS(BOOTSTRAP_FA_CSS_R, Resource(BOOTSTRAP_FA_CSS_R))
        e.withLinkedCSS(BOOTSTRAP_FA_CSS_S, Resource(BOOTSTRAP_FA_CSS_S))
        e.withResource(BOOTSTRAP_FA_FONT_R, Resource(BOOTSTRAP_FA_FONT_R))
        e.withResource(BOOTSTRAP_FA_FONT_R_EOT, Resource(BOOTSTRAP_FA_FONT_R_EOT))
        e.withResource(BOOTSTRAP_FA_FONT_R_SVG, Resource(BOOTSTRAP_FA_FONT_R_SVG))
        e.withResource(BOOTSTRAP_FA_FONT_R_TTF, Resource(BOOTSTRAP_FA_FONT_R_TTF))
        e.withResource(BOOTSTRAP_FA_FONT_R_WOFF, Resource(BOOTSTRAP_FA_FONT_R_WOFF))
        e.withResource(BOOTSTRAP_FA_FONT_S, Resource(BOOTSTRAP_FA_FONT_S))
        e.withResource(BOOTSTRAP_FA_FONT_S_WOFF, Resource(BOOTSTRAP_FA_FONT_S_WOFF))
        e.withResource(BOOTSTRAP_FA_FONT_S_EOT, Resource(BOOTSTRAP_FA_FONT_S_EOT))
        e.withResource(BOOTSTRAP_FA_FONT_S_SVG, Resource(BOOTSTRAP_FA_FONT_S_SVG))
        e.withResource(BOOTSTRAP_FA_FONT_S_TTF, Resource(BOOTSTRAP_FA_FONT_S_TTF))
        e.withLinkedCSS(SCROLL_TO_TOP_CSS, Resource(SCROLL_TO_TOP_CSS))
        e.withLinkedJavaScript(SCROLL_TO_TOP_JS, Resource(SCROLL_TO_TOP_JS))
    }

    companion object {
        private const val BOOTSTRAP_JQUERY = "/bootstrap/jquery-3.2.1.slim.min.js"
        private const val BOOTSTRAP_POPPER = "/bootstrap/popper.min.js"
        private const val BOOTSTRAP = "/bootstrap/bootstrap.min.js"
        private const val ENABLE_BOOTSTRAP_CSS = "/bootstrap/enable-bootstrap.css"
        private const val BOOTSTRAP_SIDEBAR = "/bootstrap/sidebar.js"
        private const val DOC_CSS = "/bootstrap/doc.min.css"
        private const val DOC_JS = "/bootstrap/doc.min.js"
        private const val HIGHLIGHT_CSS = "/highlight/stackoverflow-light.min.css"
        private const val HIGHLIGHT_JS = "/highlight/highlight.min.js"
        private const val BOOTSTRAP_CSS = "/bootstrap/bootstrap.min.css"
        private const val BOOTSTRAP_BUNDLE_MAP = "/bootstrap/bootstrap.bundle.min.js.map"
        private const val BOOTSTRAP_CSS_MAP = "/bootstrap/bootstrap.min.css.map"
        private const val BOOTSTRAP_REBOOT_CSS = "/bootstrap/bootstrap-reboot.min.css"
        private const val BOOTSTRAP_GRID = "/bootstrap/bootstrap-grid.min.css"
        private const val BOOTSTRAP_UTILITIES = "/bootstrap/bootstrap-utilities.min.css"
        private const val BOOTSTRAP_CALLOUT_CSS = "/bootstrap/callout.css"
        private const val BOOTSTRAP_FA_CSS_A = "/fontawesome/css/all.min.css"
        private const val BOOTSTRAP_FA_CSS_R = "/fontawesome/css/regular.min.css"
        private const val BOOTSTRAP_FA_CSS_S = "/fontawesome/css/solid.min.css"
        private const val BOOTSTRAP_FA_FONT_R = "/fontawesome/webfonts/fa-regular-400.woff2"
        private const val BOOTSTRAP_FA_FONT_R_EOT = "/fontawesome/webfonts/fa-regular-400.eot"
        private const val BOOTSTRAP_FA_FONT_R_SVG = "/fontawesome/webfonts/fa-regular-400.svg"
        private const val BOOTSTRAP_FA_FONT_R_TTF = "/fontawesome/webfonts/fa-regular-400.ttf"
        private const val BOOTSTRAP_FA_FONT_R_WOFF = "/fontawesome/webfonts/fa-regular-400.woff"
        private const val BOOTSTRAP_FA_FONT_S = "/fontawesome/webfonts/fa-solid-900.woff2"
        private const val BOOTSTRAP_FA_FONT_S_EOT = "/fontawesome/webfonts/fa-solid-900.eot"
        private const val BOOTSTRAP_FA_FONT_S_SVG = "/fontawesome/webfonts/fa-solid-900.svg"
        private const val BOOTSTRAP_FA_FONT_S_TTF = "/fontawesome/webfonts/fa-solid-900.ttf"
        private const val BOOTSTRAP_FA_FONT_S_WOFF = "/fontawesome/webfonts/fa-solid-900.woff"
        private const val SCROLL_TO_TOP_CSS = "/bootstrap/scrollToTop.css"
        private const val SCROLL_TO_TOP_JS = "/bootstrap/scrollToTop.js"
    }
}
