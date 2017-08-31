package com.adven.concordion.extensions.exam.db.commands;

import com.adven.concordion.extensions.exam.html.Html;
import com.adven.concordion.extensions.exam.db.DbResultRenderer;
import org.concordion.api.CommandCall;
import org.concordion.api.Element;
import org.concordion.api.Evaluator;
import org.concordion.api.ResultRecorder;
import org.concordion.api.listener.AssertEqualsListener;
import org.concordion.api.listener.AssertFailureEvent;
import org.concordion.api.listener.AssertSuccessEvent;
import org.concordion.internal.util.Announcer;
import org.dbunit.DatabaseUnitException;
import org.dbunit.JdbcDatabaseTester;
import org.dbunit.assertion.DbComparisonFailure;
import org.dbunit.assertion.DiffCollectingFailureHandler;
import org.dbunit.assertion.Difference;
import org.dbunit.dataset.Column;
import org.dbunit.dataset.DataSetException;
import org.dbunit.dataset.ITable;
import org.dbunit.dataset.SortedTable;

import java.util.List;

import static com.adven.concordion.extensions.exam.html.Html.*;
import static com.google.common.base.Strings.isNullOrEmpty;
import static org.concordion.api.Result.FAILURE;
import static org.concordion.api.Result.SUCCESS;
import static org.dbunit.Assertion.assertEquals;
import static org.dbunit.dataset.filter.DefaultColumnFilter.includedColumnsTable;

public class DBCheckCommand extends DBCommand {
    private Announcer<AssertEqualsListener> listeners = Announcer.to(AssertEqualsListener.class);

    public DBCheckCommand(String name, String tag, JdbcDatabaseTester dbTester) {
        super(name, tag, dbTester);
        listeners.addListener(new DbResultRenderer());
    }

    private void failure(ResultRecorder resultRecorder, Element element, Object actual, String expected) {
        resultRecorder.record(FAILURE);
        listeners.announce().failureReported(new AssertFailureEvent(element, expected, actual));
    }

    private void success(ResultRecorder resultRecorder, Element element) {
        resultRecorder.record(SUCCESS);
        listeners.announce().successReported(new AssertSuccessEvent(element));
    }

    @Override
    public void verify(CommandCall commandCall, Evaluator evaluator, ResultRecorder resultRecorder) {
        try {
            final ITable actual = dbTester.getConnection().createTable(expectedTable.getTableMetaData().getTableName());
            final ITable filteredActual = includedColumnsTable(actual, expectedTable.getTableMetaData().getColumns());

            assertEq(new Html(commandCall.getElement()), resultRecorder, expectedTable, filteredActual);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void assertEq(Html root, ResultRecorder resultRecorder, ITable expected, ITable actual)
            throws DatabaseUnitException {
        final DiffCollectingFailureHandler diffHandler = new DiffCollectingFailureHandler();
        Column[] columns = expected.getTableMetaData().getColumns();
        SortedTable expectedTable = new SortedTable(expected, columns);
        try {
            assertEquals(expectedTable, new SortedTable(actual, columns), diffHandler);
        } catch (DbComparisonFailure f) {
            //TODO move to ResultRenderer
            resultRecorder.record(FAILURE);
            Html div = div().css("rest-failure bd-callout bd-callout-danger").childs(div(f.getMessage()));
            root.below(div);

            Html exp = tableSlim();
            div.childs(span("Expected: "), exp);
            root = exp;

            Html act = tableSlim();
            renderTable(act, actual);
            div.childs(span("but was: "), act);
        }
        for (Difference diff : (List<Difference>) diffHandler.getDiffList()) {
            System.err.println("***** DIFF " + diff.toString());
        }
        checkResult(root.el(), expectedTable, diffHandler.getDiffList(), resultRecorder);
    }

    private void checkResult(Element el, ITable expected, List<Difference> diffs, ResultRecorder resultRecorder) {
        try {
            String title = el.getAttributeValue("caption");
            el.appendChild(caption(isNullOrEmpty(title) ? expected.getTableMetaData().getTableName() : title).el());

            Column[] cols = expected.getTableMetaData().getColumns();
            Element headerRow = new Element("tr");
            for (Column col : cols) {
                headerRow.appendChild(new Element("th").appendText(col.getColumnName()));
            }
            el.appendChild(headerRow);

            if (expected.getRowCount() == 0) {
                Element tr = new Element("tr");
                el.appendChild(tr);
                Element td = new Element("td").
                        appendText("<EMPTY>").addAttribute("colspan", String.valueOf(cols.length));
                tr.appendChild(td);
                success(resultRecorder, td);
            } else {
                for (int i = 0; i < expected.getRowCount(); i++) {
                    Element tr = new Element("tr");
                    for (Column col : cols) {
                        Object expectedValue = expected.getValue(i, col.getColumnName());
                        String displayedExpected = expectedValue == null ? "(null)" : expectedValue.toString();
                        Element td = new Element("td").appendText(displayedExpected);
                        tr.appendChild(td);
                        Difference fail = findFail(diffs, i, col);
                        if (fail != null) {
                            failure(resultRecorder, td, fail.getActualValue(), displayedExpected);
                        } else {
                            success(resultRecorder, td);
                        }
                    }
                    el.appendChild(tr);
                }
            }
        } catch (DataSetException e) {
            throw new RuntimeException(e);
        }
    }

    private Difference findFail(List<Difference> diffs, int i, Column col) {
        Difference fail = null;
        for (Difference diff : diffs) {
            if (diff.getRowIndex() == i && diff.getColumnName().equals(col.getColumnName())) {
                fail = diff;
                break;
            }
        }
        return fail;
    }
}