package com.adven.concordion.extensions.exam.db.commands;

import com.adven.concordion.extensions.exam.db.DbResultRenderer;
import com.adven.concordion.extensions.exam.html.Html;
import org.concordion.api.CommandCall;
import org.concordion.api.Element;
import org.concordion.api.Evaluator;
import org.concordion.api.ResultRecorder;
import org.concordion.api.listener.AssertEqualsListener;
import org.concordion.api.listener.AssertFailureEvent;
import org.concordion.api.listener.AssertSuccessEvent;
import org.concordion.internal.util.Announcer;
import org.dbunit.DatabaseUnitException;
import org.dbunit.IDatabaseTester;
import org.dbunit.assertion.DbComparisonFailure;
import org.dbunit.assertion.DiffCollectingFailureHandler;
import org.dbunit.assertion.Difference;
import org.dbunit.database.IDatabaseConnection;
import org.dbunit.dataset.Column;
import org.dbunit.dataset.DataSetException;
import org.dbunit.dataset.ITable;
import org.dbunit.dataset.SortedTable;
import org.dbunit.util.QualifiedTableName;

import java.util.List;

import static com.adven.concordion.extensions.exam.html.Html.*;
import static com.google.common.base.Strings.isNullOrEmpty;
import static org.concordion.api.Result.FAILURE;
import static org.concordion.api.Result.SUCCESS;
import static org.dbunit.Assertion.assertEquals;
import static org.dbunit.dataset.filter.DefaultColumnFilter.includedColumnsTable;

public class DBCheckCommand extends DBCommand {
    private Announcer<AssertEqualsListener> listeners = Announcer.to(AssertEqualsListener.class);

    public DBCheckCommand(String name, String tag, IDatabaseTester dbTester) {
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
            final ITable actual = getActualTable();
            final ITable filteredActual = includedColumnsTable(actual, getExpectedTable().getTableMetaData().getColumns());

            assertEq(new Html(commandCall.getElement()), resultRecorder, getExpectedTable(), filteredActual);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private ITable getActualTable() throws Exception {
        IDatabaseConnection conn = getDbTester().getConnection();
        String tableName = getExpectedTable().getTableMetaData().getTableName();
        String qualifiedName = new QualifiedTableName(tableName, conn.getSchema()).getQualifiedName();

        return conn.createQueryTable(qualifiedName, "select * from " + qualifiedName + where());
    }

    private String where() {
        return isNullOrEmpty(getWhere()) ? "" : " WHERE " + getWhere();
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

            Html act = Html.tableSlim();
            renderTable(act, actual);
            div.childs(span("but was: "), act);
        }
        for (Difference diff : (List<Difference>) diffHandler.getDiffList()) {
            System.err.println("***** DIFF " + diff.toString());
        }
        checkResult(root, expectedTable, diffHandler.getDiffList(), resultRecorder);
    }

    private void checkResult(Html el, ITable expected, List<Difference> diffs, ResultRecorder resultRecorder) {
        try {
            String title = el.attr("caption");
            el.childs(dbCaption(expected, title));

            Column[] cols = expected.getTableMetaData().getColumns();
            Html header = thead();
            Html thr = tr();
            for (Column col : cols) {
                thr.childs(th(col.getColumnName()));
            }
            el.childs(header.childs(thr));

            if (expected.getRowCount() == 0) {
                Html td = td("<EMPTY>").attr("colspan", String.valueOf(cols.length));
                Html tr = tr().childs(td);
                el.childs(tr);
                success(resultRecorder, td.el());
            } else {
                for (int i = 0; i < expected.getRowCount(); i++) {
                    Html tr = tr();
                    for (Column col : cols) {
                        Object expectedValue = expected.getValue(i, col.getColumnName());
                        String displayedExpected = expectedValue == null ? "(null)" : expectedValue.toString();
                        Html td = td(displayedExpected);
                        tr.childs(td);
                        Difference fail = findFail(diffs, i, col);
                        if (fail != null) {
                            failure(resultRecorder, td.el(), fail.getActualValue(), displayedExpected);
                        } else {
                            success(resultRecorder, td.el());
                        }
                    }
                    el.childs(tr);
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