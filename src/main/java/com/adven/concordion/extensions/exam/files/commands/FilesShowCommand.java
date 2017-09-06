package com.adven.concordion.extensions.exam.files.commands;

import com.adven.concordion.extensions.exam.html.Html;
import org.concordion.api.CommandCall;
import org.concordion.api.Evaluator;
import org.concordion.api.ResultRecorder;

import java.io.File;

public class FilesShowCommand extends BaseCommand {
    private static final String EMPTY = "<EMPTY>";

    public FilesShowCommand(String name, String tag) {
        super(name, tag);
    }

    @Override
    public void setUp(CommandCall commandCall, Evaluator evaluator, ResultRecorder resultRecorder) {
        Html element = Html.tableSlim(commandCall.getElement());

        final String path = element.takeAwayAttr("dir");
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