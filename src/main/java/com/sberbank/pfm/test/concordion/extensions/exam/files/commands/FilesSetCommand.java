package com.sberbank.pfm.test.concordion.extensions.exam.files.commands;

import com.google.common.io.Files;
import com.sberbank.pfm.test.concordion.extensions.exam.PlaceholdersResolver;
import org.concordion.api.CommandCall;
import org.concordion.api.Element;
import org.concordion.api.Evaluator;
import org.concordion.api.ResultRecorder;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;

import static com.google.common.base.Strings.isNullOrEmpty;
import static java.io.File.separator;

public class FilesSetCommand extends BaseCommand {

    @Override
    public void setUp(CommandCall commandCall, Evaluator evaluator, ResultRecorder resultRecorder) {
        Element element = commandCall.getElement();
        element.addStyleClass("table table-condensed");

        final String path = element.getAttributeValue("dir");
        element.removeAttribute("dir");
        if (path != null) {
            final File dir = new File(evaluator.evaluate(path).toString());
            clearFolder(dir);

            addHeader(element, HEADER + dir.getPath(), FILE_CONTENT);
            boolean empty = true;
            for (Element f : element.getChildElements()) {
                if ("file".equals(f.getLocalName())) {
                    String name = f.getAttributeValue("name");
                    String content = PlaceholdersResolver.resolve(f.getText(), evaluator).trim();
                    try {
                        File to = new File(dir.getPath() + separator + name);
                        if (to.createNewFile() && !isNullOrEmpty(content)) {
                            Files.append(content, to, Charset.defaultCharset());
                        }
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    element.removeChild(f);
                    addRow(element, new Element("span").appendText(name), new Element("pre").appendText(content));
                    empty = false;
                }
            }
            if (empty) {
                addRow(element, EMPTY, "");
            }
        }
    }
}