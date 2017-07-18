package com.sberbank.pfm.test.concordion.extensions.exam.db.commands;

import org.concordion.api.CommandCall;
import org.concordion.api.Element;
import org.concordion.api.Evaluator;
import org.concordion.api.ResultRecorder;
import org.dbunit.JdbcDatabaseTester;
import org.dbunit.database.IDatabaseConnection;
import org.dbunit.dataset.DefaultDataSet;
import org.dbunit.dataset.ITable;
import org.dbunit.operation.DatabaseOperation;

import static org.dbunit.operation.DatabaseOperation.CLEAN_INSERT;
import static org.dbunit.operation.DatabaseOperation.INSERT;

public class DBSetCommand extends DBCommand {
    public DBSetCommand(JdbcDatabaseTester dbTester) {
        super(dbTester);
    }

    @Override
    public void setUp(CommandCall commandCall, Evaluator evaluator, ResultRecorder resultRecorder) {
        super.setUp(commandCall, evaluator, resultRecorder);
        try {
            Element el = commandCall.getElement();
            renderTable(el, expectedTable);
            setUpDB(expectedTable, parseInsertMode(el));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void setUpDB(ITable iTable, DatabaseOperation insertMode) throws Exception {
        IDatabaseConnection connection = null;
        try {
            connection = dbTester.getConnection();
            dbTester.setSetUpOperation(insertMode);
            dbTester.setDataSet(new DefaultDataSet(iTable));
            dbTester.onSetup();
        } finally {
            if (connection != null) {
                try {
                    dbTester.closeConnection(connection);
                } catch (Exception e) {
                    System.err.println("Could not close connection " + e);
                    e.printStackTrace();
                }
            }
        }
    }

    private DatabaseOperation parseInsertMode(Element el) {
        return el.getAttributeValue("mode") != null && el.getAttributeValue("mode").equals("add") ?
                INSERT : CLEAN_INSERT;
    }
}