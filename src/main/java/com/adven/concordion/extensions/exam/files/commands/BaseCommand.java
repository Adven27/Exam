package com.adven.concordion.extensions.exam.files.commands;

import com.adven.concordion.extensions.exam.core.commands.ExamCommand;
import com.adven.concordion.extensions.exam.core.html.Html;

import static com.adven.concordion.extensions.exam.core.html.HtmlBuilder.*;

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
        return caption(dirPath).childs(
            italic("").css("fa fa-folder-open fa-pull-left fa-border")
        );
    }
}