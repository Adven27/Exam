package io.github.adven27.concordion.extensions.exam.core

import org.concordion.api.Resource
import org.concordion.api.extension.ConcordionExtender
import org.concordion.api.extension.ConcordionExtension

class CodeMirrorExtension : ConcordionExtension {
    override fun addTo(e: ConcordionExtender) {
        e.linkedCss(
            BASE,
            "codemirror.css",
            "merge.css",
            "enable-codemirror.css",
            "foldgutter.css",
            "simplescrollbar.css",
            "theme/darcula.css"
        )

        e.linkedJs(
            BASE,
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
        )
    }

    companion object {
        const val BASE = "codemirror"
    }
}

class HighlightExtension : ConcordionExtension {
    override fun addTo(e: ConcordionExtender) {
        e.linkedCss(BASE, "stackoverflow-light.min.css")
        e.linkedJs(BASE, "highlight.min.js")
        e.linkedJs(BASE, "http.min.js")
        e.linkedJs(BASE, "java.min.js")
    }

    companion object {
        const val BASE = "highlight"
    }
}

class FontAwesomeExtension : ConcordionExtension {
    override fun addTo(e: ConcordionExtender) {
        e.linkedCss(
            BASE,
            "css/all.min.css",
            "css/regular.min.css",
            "css/solid.min.css",
            "css/all.min.css",
        )
        e.resources(
            BASE,
            "webfonts/fa-regular-400.woff2",
            "webfonts/fa-regular-400.eot",
            "webfonts/fa-regular-400.svg",
            "webfonts/fa-regular-400.ttf",
            "webfonts/fa-regular-400.woff",
            "webfonts/fa-solid-900.woff2",
            "webfonts/fa-solid-900.eot",
            "webfonts/fa-solid-900.svg",
            "webfonts/fa-solid-900.ttf",
            "webfonts/fa-solid-900.woff",
        )
    }

    companion object {
        const val BASE = "fontawesome"
    }
}


class BootstrapExtension : ConcordionExtension {
    override fun addTo(e: ConcordionExtender) {
        e.linkedCss(
            BASE,
            "bootstrap.min.css",
            "enable-bootstrap.css",
            "doc.min.css",
            "scrollToTop.css",
//            "bootstrap-reboot.min.css",
//            "bootstrap-utilities.min.css",
//            "bootstrap-grid.min.css",
//            "callout.css"
        )
        e.linkedJs(
            BASE,
            "bootstrap.min.js",
            "jquery-3.2.1.slim.min.js",
            "sidebar.js",
            "doc.min.js",
            "scrollToTop.js",
//            "popper.min.js",
        )
    }

    companion object {
        const val BASE = "bootstrap"
    }
}

private fun ConcordionExtender.linkedCss(base: String, vararg css: String) = css.forEach {
    withLinkedCSS("\\$base\\$it", Resource("\\$base\\$it"))
}

private fun ConcordionExtender.linkedJs(base: String, vararg js: String) = js.forEach {
    withLinkedJavaScript("\\$base\\$it", Resource("\\$base\\$it"))
}

private fun ConcordionExtender.resources(base: String, vararg resources: String) = resources.forEach {
    withResource("\\$base\\$it", Resource("\\$base\\$it"))
}

