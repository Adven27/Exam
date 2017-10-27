package com.adven.concordion.extensions.exam.db.commands;

import com.adven.concordion.extensions.exam.html.Html;
import org.concordion.api.CommandCall;
import org.concordion.api.Evaluator;
import org.concordion.api.ResultRecorder;
import org.dbunit.IDatabaseTester;
import org.dbunit.database.IDatabaseConnection;
import org.dbunit.dataset.DataSetException;
import org.dbunit.dataset.ITable;

import java.sql.SQLException;

import static com.adven.concordion.extensions.exam.html.Html.table;
import static com.google.common.base.Strings.isNullOrEmpty;
import static org.dbunit.dataset.filter.DefaultColumnFilter.includedColumnsTable;

public class DBShowCommand extends DBCommand {

    public DBShowCommand(String name, String tag, IDatabaseTester dbTester) {
        super(name, tag, dbTester);
    }

    @Override
    public void setUp(CommandCall commandCall, Evaluator eval, ResultRecorder resultRecorder) {
        Html el = table(commandCall.getElement());
        try {
            IDatabaseConnection connection = dbTester.getConnection();
            String tableName = el.takeAwayAttr("table", eval);
            String rowFilter = el.takeAwayAttr("where", eval);
            ITable filteredColumnsTable =
                    includedColumnsTable(isNullOrEmpty(rowFilter) ? connection.createTable(tableName)
                                                                  : getFilteredTable(connection, tableName, rowFilter),
                                         parseCols(el, eval).cols());

            renderTable(el, filteredColumnsTable);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private ITable getFilteredTable(IDatabaseConnection connection, String tableName, String rowFilter) throws SQLException, DataSetException {
        return connection.createQueryTable(tableName, "SELECT * FROM " + tableName + " WHERE " + rowFilter);
    }
}