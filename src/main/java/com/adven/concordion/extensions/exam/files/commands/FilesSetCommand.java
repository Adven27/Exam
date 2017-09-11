package com.adven.concordion.extensions.exam.files.commands;

import com.adven.concordion.extensions.exam.PlaceholdersResolver;
import com.adven.concordion.extensions.exam.html.Html;
import com.google.common.io.Files;
import org.concordion.api.CommandCall;
import org.concordion.api.Evaluator;
import org.concordion.api.ResultRecorder;

import java.io.File;
import java.io.IOException;

import static com.adven.concordion.extensions.exam.html.Html.caption;
import static com.adven.concordion.extensions.exam.html.Html.codeXml;
import static com.adven.concordion.extensions.exam.html.Html.span;
import static com.google.common.base.Strings.isNullOrEmpty;
import static com.google.common.io.Resources.getResource;
import static java.io.File.separator;
import static java.nio.charset.Charset.defaultCharset;

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
                    String content = PlaceholdersResolver.resolve(getContentFor(f), evaluator).trim();
                    createFileWith(new File(dir.getPath() + separator + name), content);
                    root.remove(f);
                    addRow(root.el(), span(name), codeXml(content));
                    empty = false;
                }
            }
            if (empty) {
                addRow(root, EMPTY, "");
            }
        }
    }

    private void createFileWith(File to, String content) {
        try {
            if (to.createNewFile() && !isNullOrEmpty(content)) {
                Files.append(content, to, defaultCharset());
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private String getContentFor(Html f) {
        String from = f.attr("from");
        if (from != null) {
            try {
                return Files.toString(new File(getResource(from).getFile()), defaultCharset());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return f.text();
    }
}