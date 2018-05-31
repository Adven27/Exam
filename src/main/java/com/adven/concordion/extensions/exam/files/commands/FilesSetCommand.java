package com.adven.concordion.extensions.exam.files.commands;

import com.adven.concordion.extensions.exam.files.FilesLoader;
import com.adven.concordion.extensions.exam.html.Html;
import org.concordion.api.CommandCall;
import org.concordion.api.Evaluator;
import org.concordion.api.ResultRecorder;

import static com.adven.concordion.extensions.exam.html.Html.*;
import static java.io.File.separator;

public class FilesSetCommand extends BaseCommand {

    FilesLoader filesLoader;

    public FilesSetCommand(String name, String tag, FilesLoader filesLoader) {
        super(name, tag);
        this.filesLoader = filesLoader;
    }

    @Override
    public void setUp(CommandCall commandCall, Evaluator evaluator, ResultRecorder resultRecorder) {
        Html root = Html.Companion.tableSlim(commandCall.getElement());

        final String path = root.takeAwayAttr("dir");
        if (path != null) {

            String evalPath = evaluator.evaluate(path).toString();

            filesLoader.clearFolder(evalPath);

            root.childs(flCaption(evalPath));
            addHeader(root, HEADER, FILE_CONTENT);
            boolean empty = true;
            for (Html f : root.childs()) {
                if ("file".equals(f.localName())) {
                    final FilesLoader.FileTag fileTag = filesLoader.readFileTag(f, evaluator);
                    filesLoader.createFileWith(evalPath + separator + fileTag.name(), fileTag.content());
                    root.childs(Companion.trWithTDs(
                            Companion.span(fileTag.name()),
                            Companion.codeXml(fileTag.content()).
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