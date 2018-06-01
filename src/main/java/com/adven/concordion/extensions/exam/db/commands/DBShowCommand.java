package com.adven.concordion.extensions.exam.db.commands;

import com.adven.concordion.extensions.exam.html.Html;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
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

    @SuppressFBWarnings("NP_PARAMETER_MUST_BE_NONNULL_BUT_MARKED_AS_NULLABLE")
    @Override
    public void setUp(CommandCall commandCall, Evaluator eval, ResultRecorder resultRecorder) {
        Html el = table(commandCall.getElement());
        try {
            String tableName = el.takeAwayAttr("table", eval);
            String where = el.takeAwayAttr("where", eval);
            IDatabaseConnection conn = getDbTester().getConnection();
            ITable filteredColumnsTable =
                    includedColumnsTable(isNullOrEmpty(where) ? conn.createTable(tableName)
                                                              : getFilteredTable(conn, tableName, where),
                                         parseCols(el, eval).cols()
                    );

            renderTable(el, filteredColumnsTable);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private ITable getFilteredTable(IDatabaseConnection connection, String tableName, String rowFilter) throws SQLException, DataSetException {
        return connection.createQueryTable(tableName, "SELECT * FROM " + tableName + " WHERE " + rowFilter);
    }
}