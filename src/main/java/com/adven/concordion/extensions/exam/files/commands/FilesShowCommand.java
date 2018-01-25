package com.adven.concordion.extensions.exam.files.commands;

import com.adven.concordion.extensions.exam.files.FilesLoader;
import com.adven.concordion.extensions.exam.html.Html;
import org.concordion.api.CommandCall;
import org.concordion.api.Evaluator;
import org.concordion.api.ResultRecorder;

import static com.adven.concordion.extensions.exam.html.Html.*;

public class FilesShowCommand extends BaseCommand {
    private static final String EMPTY = "<EMPTY>";

    FilesLoader filesLoader;

    public FilesShowCommand(String name, String tag, FilesLoader filesLoader) {
        super(name, tag);
        this.filesLoader = filesLoader;
    }

    @Override
    public void setUp(CommandCall commandCall, Evaluator evaluator, ResultRecorder resultRecorder) {
        Html element = Html.tableSlim(commandCall.getElement());

        final String path = element.takeAwayAttr("dir");

        if (path != null) {

            String evalPath = evaluator.evaluate(path).toString();

            element.childs(
                    thead().childs(
                            th().childs(
                                    italic("").css("fa fa-folder-open fa-pull-left fa-border")
                            ).text(evalPath)
                    )
            );

            String fileNames[] = filesLoader.getFileNames(evalPath);

            if (fileNames == null || fileNames.length == 0) {
                addRow(element, EMPTY);
            } else {
                for (String fName : fileNames) {
                    addRow(element, fName);
                }
            }
        }
    }
}