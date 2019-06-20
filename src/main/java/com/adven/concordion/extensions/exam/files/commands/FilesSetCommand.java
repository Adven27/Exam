package com.adven.concordion.extensions.exam.files.commands;

import com.adven.concordion.extensions.exam.files.FilesLoader;
import com.adven.concordion.extensions.exam.core.html.Html;
import org.concordion.api.CommandCall;
import org.concordion.api.Evaluator;
import org.concordion.api.ResultRecorder;

import static com.adven.concordion.extensions.exam.core.html.HtmlBuilder.*;
import static java.io.File.separator;
import static kotlin.TuplesKt.to;

public class FilesSetCommand extends BaseCommand {
    private FilesLoader filesLoader;

    public FilesSetCommand(String name, String tag, FilesLoader filesLoader) {
        super(name, tag);
        this.filesLoader = filesLoader;
    }

    @Override
    public void setUp(CommandCall commandCall, Evaluator evaluator, ResultRecorder resultRecorder) {
        Html root = tableSlim(commandCall.getElement());

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
                    root.childs(
                        trWithTDs(
                            span(fileTag.name()),
                            codeXml(fileTag.content()).attrs(
                                    to("autoFormat", String.valueOf(fileTag.autoFormat())),
                                    to("lineNumbers", String.valueOf(fileTag.lineNumbers()))
                            ))).remove(f);

                    empty = false;
                }
            }
            if (empty) {
                addRow(root, EMPTY, "");
            }
        }
    }
}