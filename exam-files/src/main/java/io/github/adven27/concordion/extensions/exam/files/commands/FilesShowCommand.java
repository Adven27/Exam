package io.github.adven27.concordion.extensions.exam.files.commands;

import io.github.adven27.concordion.extensions.exam.core.html.Html;
import io.github.adven27.concordion.extensions.exam.files.FilesLoader;
import org.concordion.api.CommandCall;
import org.concordion.api.Evaluator;
import org.concordion.api.Fixture;
import org.concordion.api.ResultRecorder;

import static io.github.adven27.concordion.extensions.exam.core.html.HtmlBuilder.italic;
import static io.github.adven27.concordion.extensions.exam.core.html.HtmlBuilder.table;
import static io.github.adven27.concordion.extensions.exam.core.html.HtmlBuilder.th;
import static io.github.adven27.concordion.extensions.exam.core.html.HtmlBuilder.thead;

public class FilesShowCommand extends BaseCommand {
    private static final String EMPTY = "<EMPTY>";
    private FilesLoader filesLoader;

    public FilesShowCommand(String name, String tag, FilesLoader filesLoader) {
        super(name, tag);
        this.filesLoader = filesLoader;
    }

    @Override
    public void setUp(CommandCall commandCall, Evaluator evaluator, ResultRecorder resultRecorder, Fixture fixture) {
        final Html element = table(commandCall.getElement());
        final String path = element.takeAwayAttr("dir");

        if (path != null) {
            String evalPath = evaluator.evaluate(path).toString();

            element.childs(
                thead().childs(
                    th().childs(
                        italic(" ").css("far fa-folder-open me-1")
                    ).text(evalPath)
                )
            );

            String[] fileNames = filesLoader.getFileNames(evalPath);

            if (fileNames.length == 0) {
                addRow(element, EMPTY);
            } else {
                for (String fName : fileNames) {
                    addRow(element, fName);
                }
            }
        }
    }
}