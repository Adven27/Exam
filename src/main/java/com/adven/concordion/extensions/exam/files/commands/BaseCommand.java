package com.adven.concordion.extensions.exam.files.commands;

import com.adven.concordion.extensions.exam.html.Html;
import org.concordion.api.AbstractCommand;
import org.concordion.api.Element;

import java.io.File;

class BaseCommand extends AbstractCommand {
    protected static final String EMPTY = "<EMPTY>";
    protected static final String HEADER = "FILES IN FOLDER: ";
    protected static final String FILE_CONTENT = "FILE CONTENT";

    protected void addHeader(Element element, String... texts) {
        addInTR(element, "th", texts);
    }

    protected void addHeader(Html element, String... texts) {
        addInTR(element, "th", texts);
    }

    protected void addRow(Element element, String... texts) {
        addInTR(element, "td", texts);
    }

    protected void addRow(Element element, Element... els) {
        addInTR(element, "td", els);
    }

    private void addInTR(Element element, String cell, String... texts) {
        Element tr = new Element("tr");
        for (String text : texts) {
            tr.appendChild(new Element(cell).appendText(text));
        }
        element.appendChild(tr);
    }

    private void addInTR(Html element, String cell, String... texts) {
        Html tr = Html.tr();
        for (String text : texts) {
            tr.childs(new Html(cell).text(text));
        }
        element.childs(tr);
    }

    private void addInTR(Element element, String cell, Element... els) {
        Element tr = new Element("tr");
        for (Element e : els) {
            Element c = new Element(cell);
            c.appendChild(e);
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