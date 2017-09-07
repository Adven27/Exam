package com.adven.concordion.extensions.exam.files.commands;

import com.adven.concordion.extensions.exam.commands.ExamCommand;
import com.adven.concordion.extensions.exam.html.Html;
import org.concordion.api.Element;

import java.io.File;

import static com.adven.concordion.extensions.exam.html.Html.*;

class BaseCommand extends ExamCommand {
    protected static final String EMPTY = "<EMPTY>";
    protected static final String HEADER = "";
    protected static final String FILE_CONTENT = "";

    public BaseCommand(String name, String tag) {
        super(name, tag);
    }

    protected void addHeader(Html element, String... texts) {
        Html header = thead();
        Html tr = thead();
        for (String text : texts) {
            tr.childs(th(text));
        }
        element.childs(header.childs(tr));
    }

    protected void addRow(Html element, String... texts) {
        Html tr = tr();
        for (String text : texts) {
            tr.childs(td(text));
        }
        element.childs(tr);
    }

    protected void addRow(Element element, Html... els) {
        addInTR(element, "td", els);
    }

    private void addInTR(Element element, String cell, Html... els) {
        Element tr = new Element("tr");
        for (Html e : els) {
            Element c = new Element(cell);
            c.appendChild(e.el());
            tr.appendChild(c);
        }
        element.appendChild(tr);
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
}