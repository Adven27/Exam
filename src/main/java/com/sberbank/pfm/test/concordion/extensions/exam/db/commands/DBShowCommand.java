package com.sberbank.pfm.test.concordion.extensions.exam.db.commands;

import com.sberbank.pfm.test.concordion.extensions.exam.html.Html;
import org.concordion.api.CommandCall;
import org.concordion.api.Evaluator;
import org.concordion.api.ResultRecorder;
import org.dbunit.JdbcDatabaseTester;
import org.dbunit.database.IDatabaseConnection;

import java.sql.SQLException;

import static org.dbunit.dataset.filter.DefaultColumnFilter.includedColumnsTable;

public class DBShowCommand extends DBCommand {

    public DBShowCommand(JdbcDatabaseTester dbTester) {
        super(dbTester);
    }

    @Override
    public void setUp(CommandCall commandCall, Evaluator eval, ResultRecorder resultRecorder) {
        Html el = new Html(commandCall.getElement()).style("table table-condensed");
        IDatabaseConnection conn = null;
        try {
            conn = dbTester.getConnection();
            renderTable(el, includedColumnsTable(
                    conn.createTable(el.takeAwayAttr("table", eval)), parseCols(el, eval).cols()));
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
}