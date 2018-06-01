package com.adven.concordion.extensions.exam.files.commands;

import com.adven.concordion.extensions.exam.commands.ExamCommand;
import com.adven.concordion.extensions.exam.html.Html;

import static com.adven.concordion.extensions.exam.html.Html.*;

class BaseCommand extends ExamCommand {
    protected static final String EMPTY = "<EMPTY>";
    protected static final String HEADER = "file";
    protected static final String FILE_CONTENT = "content";

    public BaseCommand(String name, String tag) {
        super(name, tag);
    }

    protected void addHeader(Html element, String header, String content) {
        element.childs(
                thead().childs(
                        th(header).style("width:20%"),
                        th(content)
                )
        );
    }

    protected void addRow(Html element, String... texts) {
        Html tr = tr();
        for (String text : texts) {
            tr.childs(td(text));
        }
        element.childs(tr);
    }

    protected Html flCaption(String dirPath) {
        return caption(dirPath).childs(
                italic("").css("fa fa-folder-open fa-pull-left fa-border")
        );
    }
}