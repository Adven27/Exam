package com.adven.concordion.extensions.exam.files.commands;

import com.adven.concordion.extensions.exam.html.Html;
import org.concordion.api.CommandCall;
import org.concordion.api.Evaluator;
import org.concordion.api.ResultRecorder;

import java.io.File;

import static com.adven.concordion.extensions.exam.PlaceholdersResolver.resolveJson;
import static com.adven.concordion.extensions.exam.html.Html.*;
import static com.google.common.io.Resources.getResource;
import static java.io.File.separator;
import static java.lang.Boolean.parseBoolean;

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
                    String name = f.attr("name");
                    String content = resolveJson(getContentFor(f), evaluator).trim();
                    createFileWith(new File(dir.getPath() + separator + name), content);
                    root.remove(f);
                    Boolean autoFormat = parseBoolean(f.attr("autoFormat"));
                    Boolean lineNumbers = parseBoolean(f.attr("lineNumbers"));

                    root.childs(trWithTDs(
                            span(name),
                            codeXml(content).
                                    attr("autoFormat", autoFormat.toString()).
                                    attr("lineNumbers", lineNumbers.toString())
                    ));

                    empty = false;
                }
            }
            if (empty) {
                addRow(root, EMPTY, "");
            }
        }
    }

    private String getContentFor(Html f) {
        String from = f.attr("from");
        return from != null ? readFile(new File(getResource(from).getFile())) : f.text();
    }
}