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
                Companion.thead().childs(
                        Companion.th(header).style("width:20%"),
                        Companion.th(content)
                )
        );
    }

    protected void addRow(Html element, String... texts) {
        Html tr = Companion.tr();
        for (String text : texts) {
            tr.childs(Companion.td(text));
        }
        element.childs(tr);
    }

    protected Html flCaption(String dirPath) {
        return Html.Companion.caption().childs(
                Companion.italic("").css("fa fa-folder-open fa-pull-left fa-border")
        ).text(dirPath);
    }
}