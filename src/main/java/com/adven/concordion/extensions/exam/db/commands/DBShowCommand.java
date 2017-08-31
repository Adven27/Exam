package com.adven.concordion.extensions.exam.db.commands;

import com.adven.concordion.extensions.exam.html.Html;
import org.concordion.api.CommandCall;
import org.concordion.api.Evaluator;
import org.concordion.api.ResultRecorder;
import org.dbunit.JdbcDatabaseTester;
import org.dbunit.database.IDatabaseConnection;
import org.dbunit.dataset.DataSetException;
import org.dbunit.dataset.IRowValueProvider;
import org.dbunit.dataset.ITable;
import org.dbunit.dataset.RowFilterTable;
import org.dbunit.dataset.filter.IRowFilter;

import java.sql.SQLException;

import static com.adven.concordion.extensions.exam.html.Html.table;
import static com.google.common.base.Strings.isNullOrEmpty;
import static java.lang.String.valueOf;
import static org.dbunit.dataset.filter.DefaultColumnFilter.includedColumnsTable;

public class DBShowCommand extends DBCommand {

    public DBShowCommand(String name, String tag, JdbcDatabaseTester dbTester) {
        super(name, tag, dbTester);
    }

    @Override
    public void setUp(CommandCall commandCall, Evaluator eval, ResultRecorder resultRecorder) {
        Html el = table(commandCall.getElement());
        IDatabaseConnection conn = null;
        try {
            conn = dbTester.getConnection();
            ITable filteredColumnsTable = includedColumnsTable(conn.createTable(el.takeAwayAttr("table", eval)),
                                                               parseCols(el, eval).cols());

            String rowFilter = el.takeAwayAttr("where", eval);
            renderTable(el, isNullOrEmpty(rowFilter) ?
                    filteredColumnsTable : new RowFilterTable(filteredColumnsTable, getRowFilter(rowFilter)));
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) {
                    System.err.println("Could not close connection " + e);
                    e.printStackTrace();
                }
            }
        }
    }

    private IRowFilter getRowFilter(final String filter) {
        return new IRowFilter() {
            @Override
            public boolean accept(IRowValueProvider rowValue) {
                for (String pair: filter.split(";")) {
                    String[] expression = pair.split("=");
                    Object columnValue = getColumnValue(rowValue, expression[0]);
                    if (!valueOf(columnValue).equalsIgnoreCase(expression[1])) {
                        return false;
                    }
                }
                return true;
            }

            private Object getColumnValue(IRowValueProvider rowValue, String columnName) {
                try {
                    return rowValue.getColumnValue(columnName);
                } catch (DataSetException e) {
                    throw new RuntimeException(e);
                }
            }
        };
    }
}