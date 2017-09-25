package com.adven.concordion.extensions.exam.files.commands;

import com.adven.concordion.extensions.exam.html.Html;
import org.concordion.api.CommandCall;
import org.concordion.api.Evaluator;
import org.concordion.api.ResultRecorder;

import java.io.File;

import static com.adven.concordion.extensions.exam.html.Html.*;
import static java.io.File.separator;

public class FilesSetCommand extends BaseCommand {

    public FilesSetCommand(String name, String tag) {
        super(name, tag);
    }

    @Override
    public void setUp(CommandCall commandCall, Evaluator evaluator, ResultRecorder resultRecorder) {
        Html root = Html.tableSlim(commandCall.getElement());

        final String path = root.takeAwayAttr("dir");
        if (path != null) {
            final File dir = new File(evaluator.evaluate(path).toString());
            clearFolder(dir);

            root.childs(caption(dir.getPath()));
            addHeader(root, HEADER, FILE_CONTENT);
            boolean empty = true;
            for (Html f : root.childs()) {
                if ("file".equals(f.localName())) {
                    final FileTag fileTag = readFileTag(f, evaluator);
                    createFileWith(new File(dir.getPath() + separator + fileTag.name()), fileTag.content());
                    root.childs(trWithTDs(
                            span(fileTag.name()),
                            codeXml(fileTag.content()).
                                    attr("autoFormat", String.valueOf(fileTag.autoFormat())).
                                    attr("lineNumbers", String.valueOf(fileTag.lineNumbers()))
                    )).remove(f);

                    empty = false;
                }
            }
            if (empty) {
                addRow(root, EMPTY, "");
            }
        }
    }
}