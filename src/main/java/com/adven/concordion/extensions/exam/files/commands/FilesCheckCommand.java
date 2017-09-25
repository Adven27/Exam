package com.adven.concordion.extensions.exam.files.commands;

import com.adven.concordion.extensions.exam.files.FilesResultRenderer;
import com.adven.concordion.extensions.exam.html.Html;
import net.javacrumbs.jsonunit.core.Configuration;
import nu.xom.Builder;
import nu.xom.Document;
import nu.xom.ParsingException;
import nu.xom.Serializer;
import org.concordion.api.*;
import org.concordion.api.listener.AssertEqualsListener;
import org.concordion.api.listener.AssertFailureEvent;
import org.concordion.api.listener.AssertSuccessEvent;
import org.concordion.internal.util.Announcer;
import org.xmlunit.builder.DiffBuilder;
import org.xmlunit.diff.DefaultNodeMatcher;
import org.xmlunit.diff.Diff;
import org.xmlunit.diff.DifferenceEvaluators;
import org.xmlunit.diff.ElementSelectors;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static com.adven.concordion.extensions.exam.html.Html.*;
import static com.google.common.base.Strings.isNullOrEmpty;
import static java.io.File.separator;
import static java.util.Arrays.asList;
import static org.xmlunit.diff.DifferenceEvaluators.chain;

public class FilesCheckCommand extends BaseCommand {
    //FIXME temporary(HA!) reuse json-unit cfg for matchers retrieving
    private final Configuration jsonUnitCfg;
    private Announcer<AssertEqualsListener> listeners = Announcer.to(AssertEqualsListener.class);

    public FilesCheckCommand(String name, String tag, Configuration jsonUnitCfg) {
        super(name, tag);
        this.jsonUnitCfg = jsonUnitCfg;
        listeners.addListener(new FilesResultRenderer());
    }

    /**
     * {@inheritDoc}.
     */
    public void verify(CommandCall commandCall, Evaluator evaluator, ResultRecorder resultRecorder) {
        Html root = Html.tableSlim(commandCall.getElement());

        final String path = root.takeAwayAttr("dir", evaluator);
        if (path != null) {
            final File dir = new File(evaluator.evaluate(path).toString());

            String[] names = dir.list();
            List<String> surplusFiles = names == null || names.length == 0 ?
                    new ArrayList<String>() : new ArrayList<>(asList(names));

            root.childs(caption(dir.getPath()));
            addHeader(root, HEADER, FILE_CONTENT);
            boolean empty = true;
            for (Html f : root.childs()) {
                if ("file".equals(f.localName())) {
                    final FileTag fileTag = readFileTag(f, evaluator);
                    final String expectedName = fileTag.name();
                    File actual = new File(dir + separator + expectedName);
                    Html fileNameTD = td(expectedName);
                    Html pre = codeXml("");
                    if (!actual.exists()) {
                        resultRecorder.record(Result.FAILURE);
                        announceFailure(fileNameTD.el(), expectedName, null);
                    } else {
                        resultRecorder.record(Result.SUCCESS);
                        announceSuccess(fileNameTD.el());
                        surplusFiles.remove(expectedName);

                        if (fileTag.content() == null) {
                            String id = UUID.randomUUID().toString();
                            final String content = readFile(dir, expectedName);
                            if (!isNullOrEmpty(content)) {
                                pre = div().childs(
                                        buttonCollapse("show", id).style("width:100%"),
                                        div().attr("id", id).css("file collapse").childs(
                                                pre.text(content)
                                        )
                                );
                            }
                        } else {
                            checkContent(actual, fileTag.content(), resultRecorder, pre.text(fileTag.content()).el());
                        }
                    }
                    root.childs(
                            tr().childs(
                                    fileNameTD,
                                    td(pre.
                                            attr("autoFormat", String.valueOf(fileTag.autoFormat())).
                                            attr("lineNumbers", String.valueOf(fileTag.lineNumbers())))
                            )
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
                                codeXml(readFile(dir, file))
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

    private void checkContent(File actual, String expected, ResultRecorder resultRecorder, Element element) {
        if (!actual.exists()) {
            xmlDoesNotEqual(resultRecorder, element, "(not set)", expected);
            return;
        }

        String prettyPrintedActual = prettyPrint(documentFrom(actual));
        try {
            if (assertEqualsXml(prettyPrintedActual, expected)) {
                xmlEquals(resultRecorder, element);
            } else {
                xmlDoesNotEqual(resultRecorder, element, prettyPrintedActual, expected);
            }
        } catch (Exception e) {
            e.printStackTrace();
            xmlDoesNotEqual(resultRecorder, element, prettyPrintedActual, expected);
        }
    }

    private Document documentFrom(File xml) {
        try {
            return new Builder().build(xml);
        } catch (ParsingException | IOException e) {
            throw new RuntimeException("invalid xml", e);
        }
    }

    private String prettyPrint(Document document) {
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            Serializer serializer = new Serializer(out, "UTF-8");
            serializer.setIndent(4);
            serializer.write(document);
            String pretty = out.toString("UTF-8");
            return pretty;
        } catch (Exception e) {
            throw new RuntimeException("invalid xml", e);
        }
    }

    private boolean assertEqualsXml(String actual, String expected) {
        Diff diff = DiffBuilder.compare(expected.trim()).
                checkForSimilar().withNodeMatcher(new DefaultNodeMatcher(ElementSelectors.byNameAndText)).
                withTest(actual.trim()).
                withDifferenceEvaluator(
                        chain(
                                DifferenceEvaluators.Default,
                                new PlaceholderSupportDiffEvaluator(jsonUnitCfg)
                        )
                ).
                ignoreComments().ignoreWhitespace().
                build();

        //FIXME Reports are visible only on logs, show them in spec too
        if (diff.hasDifferences()) {
            throw new RuntimeException(diff.toString());
        }
        return true;
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