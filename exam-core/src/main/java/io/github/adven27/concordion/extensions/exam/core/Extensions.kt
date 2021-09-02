package io.github.adven27.concordion.extensions.exam.core

import io.github.adven27.concordion.extensions.exam.core.html.Html
import io.github.adven27.concordion.extensions.exam.core.html.ID
import io.github.adven27.concordion.extensions.exam.core.html.ONCLICK
import io.github.adven27.concordion.extensions.exam.core.html.button
import io.github.adven27.concordion.extensions.exam.core.html.italic
import nu.xom.Attribute
import nu.xom.Document
import nu.xom.Element
import nu.xom.XPathContext
import nu.xom.converters.DOMConverter
import org.concordion.api.Resource
import org.concordion.api.extension.ConcordionExtender
import org.concordion.api.extension.ConcordionExtension
import org.concordion.api.Element as ConcordionElement

class CodeMirrorExtension : ConcordionExtension {
    override fun addTo(e: ConcordionExtender) {
        e.linkedCss(
            BASE,
            "codemirror.css",
            "enable-codemirror.css",
            "merge.css",
            "foldgutter.css",
            "simplescrollbar.css",
        )

        e.linkedJs(
            BASE,
            "codemirror.js",
            "enable-codemirror.js",
            "javascript.js",
            "xml.js",
            "addon/mode/simple.js",
            "http.js",
            "diff_match_patch.js",
            "merge.js",
            "xml-fold.js",
            "simplescrollbar.js"
        )
    }

    companion object {
        const val BASE = "ext/codemirror"
    }
}

class HighlightExtension : ConcordionExtension {
    override fun addTo(e: ConcordionExtender) {
        e.linkedCss(BASE, "stackoverflow-light.min.css")
        e.linkedJs(BASE, "highlight.min.js", "http.min.js", "java.min.js")
        e.withEmbeddedJavaScript( // language=js
            """
            document.addEventListener('DOMContentLoaded', function (event) {
                hljs.configure({
                    cssSelector: 'pre code, pre.highlight'
                });
                hljs.highlightAll();
            });
            """.trimIndent()
        )
        e.withDocumentParsingListener { doc ->
            doc.query("//pre/code").forEach {
                (it as Element).html().content().apply {
                    it.removeChildren()
                    it.appendChild(this)
                }
            }
        }
    }

    companion object {
        const val BASE = "ext/highlight"
    }
}

class TocbotExtension : ConcordionExtension {
    override fun addTo(e: ConcordionExtender) {
        e.linkedCss(BASE, "tocbot.css")
        e.linkedJs(BASE, "tocbot.min.js")
        e.withEmbeddedJavaScript( // language=js
            """
            window.addEventListener('DOMContentLoaded', function (event) {
                anchors.options = {
                    placement: 'left',
                    icon: '#'
                };
                anchors.add();
                tocbot.init({
                    tocSelector: '.js-toc',
                    contentSelector: '.bd-content',
                    headingSelector: 'h1, h2, h3, h4, h5, h6',
                    hasInnerContainers: true,
                    collapseDepth: 3,
                    scrollSmooth: false,
                    fixedSidebarOffset: 'auto',
                    includeHtml: true
                });
                var collapseDepth = 3; 
                jQuery( "#example-summary-badge" ).click(function() {
                    if (collapseDepth === 3) {
                        collapseDepth = 30;
                    } else {
                        collapseDepth = 3;
                    }
                    tocbot.refresh({
                        tocSelector: '.js-toc',
                        contentSelector: '.bd-content',
                        headingSelector: 'h1, h2, h3, h4, h5, h6',
                        hasInnerContainers: true,
                        collapseDepth: collapseDepth,
                        scrollSmooth: false,
                        fixedSidebarOffset: 'auto',
                        includeHtml: true
                    });
                    if (collapseDepth === 30) {
                        jQuery( ".toc-link" ).filter(function( index ) {
                            return jQuery( "i", this ).length !== 1
                        }).css("display", "none");
                    } else {
                        jQuery( ".toc-link" ).filter(function( index ) {
                            return jQuery( "i", this ).length !== 1
                        }).css("display", "unset");
                    }
                });
            }); 
            """.trimIndent()
        )
    }

    companion object {
        const val BASE = "ext/tocbot"
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
        const val BASE = "ext/fontawesome"
    }
}

class BootstrapExtension : ConcordionExtension {
    override fun addTo(e: ConcordionExtender) {
        e.linkedCss(BASE, "bootstrap.min.css", "enable-bootstrap.css", "doc.min.css", "scrollToTop.css")
        e.linkedJs(BASE, "bootstrap.min.js", "jquery-3.2.1.slim.min.js", "sidebar.js", "doc.min.js", "scrollToTop.js")
    }

    companion object {
        const val BASE = "ext/bootstrap"
    }
}

class NomNomlExtension : ConcordionExtension {
    override fun addTo(e: ConcordionExtender) {
        e.linkedJs("ext/nomnoml", "graphre.js", "nomnoml.min.js")
        e.withEmbeddedJavaScript( // language=js
            """
            document.addEventListener('DOMContentLoaded', function (event) {
                Array.from(document.getElementsByClassName("nomnoml")).forEach(function(el) {
                    nomnoml.draw(el, el.textContent); 
                });  
            });
            """.trimIndent()
        )
        e.withDocumentParsingListener { doc ->
            doc.queryExamTag("canvas").forEach {
                (it as Element).html().content().apply {
                    it.removeChildren()
                    it.appendChild(this)
                }
            }
        }
    }
}

class TopButtonExtension : ConcordionExtension {
    override fun addTo(e: ConcordionExtender) {
        e.withDocumentParsingListener {
            Html(ConcordionElement(it.rootElement))(
                button("", ID to "btnToTop", ONCLICK to "topFunction()")(
                    italic("").css("fa fa-arrow-up")
                )
            )
        }
    }
}

class IncludesExtension : ConcordionExtension {
    override fun addTo(e: ConcordionExtender) {
        e.withDocumentParsingListener { doc ->
            doc.queryExamTag("include")?.forEach {
                val template = DOMConverter.convert(
                    loadXMLFromString((it as Element).html().content())
                ).rootElement
                val parent = it.parent
                val position = parent.indexOf(it)
                for (j in template.childElements.size() - 1 downTo 0) {
                    parent.insertChild(template.childElements[j].apply { detach() }, position)
                }
                parent.removeChild(it)
            }
        }
    }
}

class CommandPrinterExtension : ConcordionExtension {
    override fun addTo(e: ConcordionExtender) {
        e.withDocumentParsingListener { visit(it.rootElement) }
    }

    private fun visit(elem: Element) {
        print(elem)
        elem.childElements.forEach { visit(it) }
    }

    fun print(elem: Element) {
        if ((elem.getAttributeValue("print") ?: "false").toBoolean()) {
            (elem.parent as Element).also {
                it.insertChild(
                    Element("div").apply {
                        addAttribute(Attribute("class", "mb-4 mt-4"))
                        appendChild(Element("mark").apply { appendChild("The following markup:") })
                        appendChild(codeOf(elem))
                        appendChild(Element("mark").apply { appendChild("will be rendered as:") })
                    },
                    it.indexOf(elem)
                )
            }
        }
    }

    private fun codeOf(elem: Element) = Element("pre").apply {
        addAttribute(Attribute("class", "doc-code language-xml mt-2"))
        appendChild(
            Element("code").apply {
                appendChild(
                    Document(elem.copy() as Element).prettyXml()
                        .replace(" xmlns:e=\"http://exam.extension.io\"", "")
                        .replace(" xmlns:cc=\"http://www.concordion.org/2007/concordion\"", "")
                        .replace(" print=\"true\"", "")
                        .lines()
                        .filterNot { it.startsWith("<?xml version") }
                        .filterNot { it.isBlank() }
                        .joinToString(separator = "\n")
                )
            }
        )
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

private fun Document.queryExamTag(name: String) =
    rootElement.query(".//$name | .//e:$name", XPathContext("e", ExamExtension.NS))

fun Element.html() = Html(ConcordionElement(this))
