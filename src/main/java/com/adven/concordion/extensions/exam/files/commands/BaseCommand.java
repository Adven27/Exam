package com.adven.concordion.extensions.exam.files.commands;

import com.adven.concordion.extensions.exam.commands.ExamCommand;
import com.adven.concordion.extensions.exam.html.Html;
import com.google.common.io.Files;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.Accessors;
import org.concordion.api.Evaluator;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;

import static com.adven.concordion.extensions.exam.PlaceholdersResolver.resolveXml;
import static com.adven.concordion.extensions.exam.html.Html.*;
import static com.google.common.base.Strings.isNullOrEmpty;
import static com.google.common.io.Resources.getResource;
import static java.io.File.separator;
import static java.lang.Boolean.parseBoolean;

class BaseCommand extends ExamCommand {
    protected static final String EMPTY = "<EMPTY>";
    protected static final String HEADER = "file";
    protected static final String FILE_CONTENT = "content";
    private static final Charset CHARSET = Charset.forName("UTF-8");

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

    protected void clearFolder(File dir) {
        File[] files = dir.listFiles();
        if (files != null) {
            for (File file : files) {
                if (!file.delete()) {
                    throw new RuntimeException("could not delete file " + file.getPath());
                }
            }
        }
    }

    protected String readFile(File dir, String file) {
        return readFile(new File(dir + separator + file));
    }

    protected String readFile(File file) {
        try {
            return Files.toString(file, CHARSET);
        } catch (IOException e) {
            return "ERROR WHILE FILE READING";
        }
    }

    protected void createFileWith(File to, String content) {
        try {
            if (to.createNewFile() && !isNullOrEmpty(content)) {
                Files.append(content, to, CHARSET);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private String getContentFor(Html f) {
        final String from = f.attr("from");
        return from == null
                ? f.hasChildren() ? f.text() : null
                : readFile(new File(getResource(from).getFile()));
    }

    protected FileTag readFileTag(Html f, Evaluator eval) {
        final String content = getContentFor(f);
        return FileTag.builder().
                name(f.attr("name")).
                content(content == null ? null : resolveXml(content, eval).trim()).
                autoFormat(parseBoolean(f.attr("autoFormat"))).
                lineNumbers(parseBoolean(f.attr("lineNumbers"))).build();
    }

    @Data
    @Builder
    @Accessors(fluent = true)
    static class FileTag {
        private String name;
        private String content;
        private boolean autoFormat;
        private boolean lineNumbers;
    }
}