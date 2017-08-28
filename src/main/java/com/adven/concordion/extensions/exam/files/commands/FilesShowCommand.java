package com.adven.concordion.extensions.exam.files.commands;

import org.concordion.api.CommandCall;
import org.concordion.api.Element;
import org.concordion.api.Evaluator;
import org.concordion.api.ResultRecorder;

import java.io.File;

public class FilesShowCommand extends BaseCommand {
    private static final String EMPTY = "<EMPTY>";

    @Override
    public void setUp(CommandCall commandCall, Evaluator evaluator, ResultRecorder resultRecorder) {
        Element element = commandCall.getElement();
        element.addStyleClass("table table-condensed");

        final String path = element.getAttributeValue("dir");
        element.removeAttribute("dir");
        if (path != null) {
            final File dir = new File(evaluator.evaluate(path).toString());

            addHeader(element, dir.getPath());

            File[] files = dir.listFiles();
            if (files == null || files.length == 0) {
                addRow(element, EMPTY);
            } else {
                for (File f : files) {
                    addRow(element, f.getName());
                }
            }
        }
    }
}