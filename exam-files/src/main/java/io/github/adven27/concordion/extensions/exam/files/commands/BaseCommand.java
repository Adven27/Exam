package io.github.adven27.concordion.extensions.exam.files.commands;

import io.github.adven27.concordion.extensions.exam.core.commands.ExamCommand;
import io.github.adven27.concordion.extensions.exam.core.html.Html;

import static io.github.adven27.concordion.extensions.exam.core.html.HtmlBuilder.caption;
import static io.github.adven27.concordion.extensions.exam.core.html.HtmlBuilder.italic;
import static io.github.adven27.concordion.extensions.exam.core.html.HtmlBuilder.span;
import static io.github.adven27.concordion.extensions.exam.core.html.HtmlBuilder.td;
import static io.github.adven27.concordion.extensions.exam.core.html.HtmlBuilder.th;
import static io.github.adven27.concordion.extensions.exam.core.html.HtmlBuilder.thead;
import static io.github.adven27.concordion.extensions.exam.core.html.HtmlBuilder.tr;

public class BaseCommand extends ExamCommand {
    static final String EMPTY = "<EMPTY>";
    static final String HEADER = "file";
    static final String FILE_CONTENT = "content";

    BaseCommand(String name, String tag) {
        super(name, tag);
    }

    void addHeader(Html element, String header, String content) {
        element.childs(
            thead().childs(
                th(header).style("width:20%"),
                th(content)
            )
        );
    }

    void addRow(Html element, String... texts) {
        Html tr = tr();
        for (String text : texts) {
            tr.childs(td(text));
        }
        element.childs(tr);
    }

    Html flCaption(String dirPath) {
        return caption().childs(
            italic(" ").css("far fa-folder-open me-1"),
            span(" ")
        ).text(dirPath);
    }
}