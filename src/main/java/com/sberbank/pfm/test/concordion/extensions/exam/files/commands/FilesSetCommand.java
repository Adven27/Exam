package com.sberbank.pfm.test.concordion.extensions.exam.files.commands;

import com.google.common.io.Files;
import com.sberbank.pfm.test.concordion.extensions.exam.PlaceholdersResolver;
import com.sberbank.pfm.test.concordion.extensions.exam.html.Html;
import org.concordion.api.CommandCall;
import org.concordion.api.Element;
import org.concordion.api.Evaluator;
import org.concordion.api.ResultRecorder;

import java.io.File;
import java.io.IOException;

import static com.google.common.base.Strings.isNullOrEmpty;
import static com.google.common.io.Resources.getResource;
import static java.io.File.separator;
import static java.nio.charset.Charset.defaultCharset;

public class FilesSetCommand extends BaseCommand {

    @Override
    public void setUp(CommandCall commandCall, Evaluator evaluator, ResultRecorder resultRecorder) {
        Html element = new Html(commandCall.getElement()).style("table table-condensed");

        final String path = element.takeAwayAttr("dir");
        if (path != null) {
            final File dir = new File(evaluator.evaluate(path).toString());
            clearFolder(dir);

            addHeader(element, HEADER + dir.getPath(), FILE_CONTENT);
            boolean empty = true;
            for (Html f : element.childs()) {
                if ("file".equals(f.localName())) {
                    String name = f.attr("name");
                    String content = PlaceholdersResolver.resolve(getContentFor(f), evaluator).trim();
                    createFileWith(new File(dir.getPath() + separator + name), content);
                    element.remove(f);
                    addRow(element.el(), new Element("span").appendText(name), new Element("pre").appendText(content));
                    empty = false;
                }
            }
            if (empty) {
                addRow(element.el(), EMPTY, "");
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