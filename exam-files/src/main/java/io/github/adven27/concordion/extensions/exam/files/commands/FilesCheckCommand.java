package io.github.adven27.concordion.extensions.exam.files.commands;

import io.github.adven27.concordion.extensions.exam.core.html.Html;
import io.github.adven27.concordion.extensions.exam.core.utils.CheckUtilsKt;
import io.github.adven27.concordion.extensions.exam.files.FilesLoader;
import io.github.adven27.concordion.extensions.exam.files.FilesResultRenderer;
import net.javacrumbs.jsonunit.core.Configuration;
import org.concordion.api.CommandCall;
import org.concordion.api.Element;
import org.concordion.api.Evaluator;
import org.concordion.api.Fixture;
import org.concordion.api.Result;
import org.concordion.api.ResultRecorder;
import org.concordion.api.listener.AssertEqualsListener;
import org.concordion.api.listener.AssertFailureEvent;
import org.concordion.api.listener.AssertSuccessEvent;
import org.concordion.internal.util.Announcer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xmlunit.diff.NodeMatcher;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static io.github.adven27.concordion.extensions.exam.core.PlaceholdersResolver.resolveToObj;
import static io.github.adven27.concordion.extensions.exam.core.html.HtmlBuilder.codeXml;
import static io.github.adven27.concordion.extensions.exam.core.html.HtmlBuilder.div;
import static io.github.adven27.concordion.extensions.exam.core.html.HtmlBuilder.divCollapse;
import static io.github.adven27.concordion.extensions.exam.core.html.HtmlBuilder.table;
import static io.github.adven27.concordion.extensions.exam.core.html.HtmlBuilder.td;
import static io.github.adven27.concordion.extensions.exam.core.html.HtmlBuilder.tr;
import static java.io.File.separator;
import static java.util.Arrays.asList;
import static kotlin.TuplesKt.to;

public class FilesCheckCommand extends BaseCommand {
    private static final Logger LOG = LoggerFactory.getLogger(FilesCheckCommand.class);
    //FIXME temporary(HA!) reuse json-unit cfg for matchers retrieving
    private final Configuration jsonUnitCfg;
    private final NodeMatcher nodeMatcher;
    private Announcer<AssertEqualsListener> listeners = Announcer.to(AssertEqualsListener.class);
    private FilesLoader filesLoader;

    public FilesCheckCommand(
        String name, String tag, Configuration jsonUnitCfg, NodeMatcher nodeMatcher, FilesLoader filesLoader) {
        super(name, tag);
        this.jsonUnitCfg = jsonUnitCfg;
        this.nodeMatcher = nodeMatcher;
        listeners.addListener(new FilesResultRenderer());
        this.filesLoader = filesLoader;
    }

    @Override
    public void verify(CommandCall commandCall, Evaluator evaluator, ResultRecorder resultRecorder, Fixture fixture) {
        Html root = table(commandCall.getElement());

        final String path = root.takeAwayAttr("dir", evaluator);
        if (path != null) {

            String evalPath = evaluator.evaluate(path).toString();

            String[] names = filesLoader.getFileNames(evalPath);

            List<String> surplusFiles = names.length == 0
                ? new ArrayList<>()
                : new ArrayList<>(asList(names));

            root.childs(flCaption(evalPath));
            addHeader(root, HEADER, FILE_CONTENT);
            boolean empty = true;
            for (Html f : root.childs()) {
                if ("file".equals(f.localName())) {
                    final FilesLoader.FileTag fileTag = filesLoader.readFileTag(f, evaluator);
                    final Object resolvedName = resolveToObj(fileTag.getName(), evaluator);
                    final String expectedName = resolvedName != null ? resolvedName.toString() : fileTag.getName();

                    Html fileNameTD = td(expectedName);
                    Html pre = codeXml("");

                    if (!filesLoader.fileExists(evalPath + separator + expectedName)) {
                        resultRecorder.record(Result.FAILURE);
                        announceFailure(fileNameTD.el(), "", null);
                    } else {
                        resultRecorder.record(Result.SUCCESS);
                        announceSuccess(fileNameTD.el());
                        surplusFiles.remove(expectedName);

                        if (fileTag.getContent() == null) {
                            String id = UUID.randomUUID().toString();
                            final String content = filesLoader.readFile(evalPath, expectedName);
                            if (!content.isEmpty()) {
                                pre = div().style("position: relative").childs(
                                    divCollapse("", id).css("fa fa-expand collapsed"),
                                    div(to("id", id)).css("file collapse").childs(
                                        pre.text(content)
                                    )
                                );
                            }
                        } else {
                            checkContent(
                                evalPath + separator + expectedName,
                                fileTag.getContent(),
                                resultRecorder,
                                pre.el()
                            );
                        }
                    }
                    root.childs(
                        tr().childs(
                            fileNameTD,
                            td().childs(
                                pre.attrs(
                                    to("autoFormat", String.valueOf(fileTag.getAutoFormat())),
                                    to("lineNumbers", String.valueOf(fileTag.getLineNumbers())))))
                    ).remove(f);
                    empty = false;
                }
            }
            for (String file : surplusFiles) {
                resultRecorder.record(Result.FAILURE);
                Html td = td();
                Html tr = tr().childs(
                    td,
                    td().childs(
                        codeXml(filesLoader.readFile(evalPath, file))
                    )
                );
                root.childs(tr);
                announceFailure(td.el(), null, file);
            }
            if (empty) {
                addRow(root, EMPTY, "");
            }
        }
    }

    private Html toPre(FilesLoader.FileTag fileTag, Html pre) {
        String content = fileTag.getContent();
        if (content != null) {
            pre.text(content);
        }
        return pre;
    }

    private void checkContent(String path, String expected, ResultRecorder resultRecorder, Element element) {
        if (!filesLoader.fileExists(path)) {
            xmlDoesNotEqual(resultRecorder, element, "(not set)", expected);
            return;
        }

        String prettyActual = CheckUtilsKt.prettyXml(filesLoader.documentFrom(path));
        try {
            if (CheckUtilsKt.equalToXml(prettyActual, expected, jsonUnitCfg, nodeMatcher)) {
                element.appendText(prettyActual);
                xmlEquals(resultRecorder, element);
            } else {
                xmlDoesNotEqual(resultRecorder, element, prettyActual, expected);
            }
        } catch (Exception e) {
            LOG.debug("Got exception on xml checking: {}", e.getMessage());
            xmlDoesNotEqual(resultRecorder, element, prettyActual, expected);
        }
    }

    private void xmlEquals(ResultRecorder resultRecorder, Element element) {
        resultRecorder.record(Result.SUCCESS);
        announceSuccess(element);
    }

    private void xmlDoesNotEqual(ResultRecorder resultRecorder, Element element, String actual, String expected) {
        resultRecorder.record(Result.FAILURE);
        announceFailure(element, expected, actual);
    }

    private void announceSuccess(Element element) {
        listeners.announce().successReported(new AssertSuccessEvent(element));
    }

    private void announceFailure(Element element, String expected, Object actual) {
        listeners.announce().failureReported(new AssertFailureEvent(element, expected, actual));
    }
}