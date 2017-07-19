package com.sberbank.pfm.test.concordion.extensions.exam.files.commands;

import com.google.common.base.Charsets;
import com.google.common.io.CharSource;
import com.google.common.io.Files;
import com.sberbank.pfm.test.concordion.extensions.exam.PlaceholdersResolver;
import com.sberbank.pfm.test.concordion.extensions.exam.files.FilesResultRenderer;
import com.sberbank.pfm.test.concordion.extensions.exam.html.Html;
import nu.xom.Builder;
import nu.xom.Document;
import nu.xom.ParsingException;
import nu.xom.Serializer;
import org.concordion.api.*;
import org.concordion.api.listener.AssertEqualsListener;
import org.concordion.api.listener.AssertFailureEvent;
import org.concordion.api.listener.AssertSuccessEvent;
import org.concordion.internal.util.Announcer;
import org.xml.sax.SAXException;
import org.xmlunit.builder.DiffBuilder;
import org.xmlunit.diff.Diff;

import javax.xml.transform.TransformerException;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static com.sberbank.pfm.test.concordion.extensions.exam.html.Html.*;
import static java.io.File.separator;
import static java.util.Arrays.asList;
import static org.xmlunit.builder.Input.fromString;

public class FilesCheckCommand extends BaseCommand {
    private Announcer<AssertEqualsListener> listeners = Announcer.to(AssertEqualsListener.class);

    public FilesCheckCommand() {
        listeners.addListener(new FilesResultRenderer());
    }

    /**
     * verify stage.
     *
     * @param commandCall    - command
     * @param evaluator      - evaluator
     * @param resultRecorder - result
     */
    public void verify(CommandCall commandCall, Evaluator evaluator, ResultRecorder resultRecorder) {
        Html root = new Html(commandCall.getElement()).style("table table-condensed");

        final String path = root.takeAwayAttr("dir");
        if (path != null) {
            final File dir = new File(evaluator.evaluate(path).toString());

            String[] names = dir.list();
            List<String> surplusFiles = names == null || names.length == 0 ?
                    new ArrayList<String>() : new ArrayList<>(asList(names));

            addHeader(root.el(), HEADER + dir.getPath(), FILE_CONTENT);
            boolean empty = true;
            for (Html f : root.childs()) {
                if ("file".equals(f.localName())) {
                    final String expectedName = f.attr("name");
                    File actual = new File(dir + separator + expectedName);
                    Html tr = tr();
                    Html fileNameTD = td(expectedName);
                    tr.childs(fileNameTD);
                    Html pre = Html.pre();
                    if (!actual.exists()) {
                        resultRecorder.record(Result.FAILURE);
                        announceFailure(fileNameTD.el(), expectedName, null);
                    } else {
                        resultRecorder.record(Result.SUCCESS);
                        announceSuccess(fileNameTD.el());
                        surplusFiles.remove(expectedName);
                        if (f.hasChildren()) {
                            f.moveChildrenTo(pre);
                            f.moveAttributesTo(pre);
                            checkContent(actual, evaluator, resultRecorder, pre.el());
                        } else {
                            pre.text(readFile(dir, expectedName));
                        }
                    }

                    Html td = td().childs(pre);
                    tr.childs(td);
                    root.childs(tr).remove(f);
                    empty = false;
                }
            }
            for (String file : surplusFiles) {
                resultRecorder.record(Result.FAILURE);
                Html td = td();
                Html tr = tr().childs(
                        td,
                        td().childs(
                                pre().text(readFile(dir, file))
                        )
                );
                root.childs(tr);
                announceFailure(td.el(), null, file);
            }
            if (empty) {
                addRow(root.el(), EMPTY, "");
            }
        }
    }

    private String readFile(File dir, String file) {
        try {
            return Files.toString(new File(dir + separator + file), Charsets.UTF_8);
        } catch (IOException e) {
            return "ERROR WHILE FILE READING";
        }
    }

    private void checkContent(File actual, Evaluator evaluator, ResultRecorder resultRecorder, Element element) {
        StringBuilder xml = new StringBuilder();

        Element[] child = element.getChildElements();
        for (Element aChild : child) {
            xml.append(aChild.toXML());
        }
        element.moveChildrenTo(new Element("tmp"));
        String expected = prettyPrint(documentFrom(PlaceholdersResolver.resolve(xml.toString(), evaluator)));
        element.appendText(expected);

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

    private Document documentFrom(String xml) {
        try {
            return new Builder().build(CharSource.wrap(xml).openStream());
        } catch (ParsingException | IOException e) {
            throw new RuntimeException("invlaid xml", e);
        }
    }

    private Document documentFrom(File xml) {
        try {
            return new Builder().build(xml);
        } catch (ParsingException | IOException e) {
            throw new RuntimeException("invlaid xml", e);
        }
    }

    private String prettyPrint(Document document) {
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            Serializer serializer = new Serializer(out, "UTF-8");
            serializer.setIndent(4);
            serializer.write(document);
            String pretty = out.toString("UTF-8");
            return pretty.substring(pretty.indexOf('\n') + 1); // replace first line
        } catch (Exception e) {
            throw new RuntimeException("invlaid xml", e);
        }
    }

    private boolean assertEqualsXml(String actual, String expected)
            throws TransformerException, SAXException, IOException {
        Diff diff = DiffBuilder.compare(fromString(expected)).withTest(fromString(actual)).
                ignoreComments().ignoreWhitespace().build();
        if (!diff.hasDifferences()) {
            return true;
        }
        throw new RuntimeException(diff.toString());
    }

    private void xmlEquals(ResultRecorder resultRecorder, Element element) {
        resultRecorder.record(Result.SUCCESS);
        announceSuccess(element);
    }

    private void xmlDoesNotEqual(ResultRecorder resultRecorder, Element element, String actual, String expected) {
        //FIXME если добавлять всегда, то в случае успешной проверки блок с ожидаемым результатом почему-то пропадает
        element.addStyleClass("xml");
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