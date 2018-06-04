package specs.db;

import com.adven.concordion.extensions.exam.db.TableData;
import org.concordion.api.BeforeSpecification;
import org.dbunit.JdbcDatabaseTester;
import org.joda.time.LocalDateTime;
import specs.Specs;

import java.util.Date;

import static org.joda.time.format.DateTimeFormat.forPattern;

public class Db extends Specs {
    private static final String CREATE_TABLES = "CREATE TABLE IF NOT EXISTS PERSON "
        + "(NAME VARCHAR2(255 CHAR), AGE NUMBER, IQ NUMBER, BIRTHDAY TIMESTAMP)\\;"
        + "CREATE TABLE IF NOT EXISTS EMPTY (NAME VARCHAR2(255 CHAR), VALUE NUMBER)";

    private final JdbcDatabaseTester dbTester = dbTester();

    private static JdbcDatabaseTester dbTester() {
        try {
            return new JdbcDatabaseTester(
                "org.h2.Driver", "jdbc:h2:mem:test;DB_CLOSE_DELAY=-1;"
                + "INIT=CREATE SCHEMA IF NOT EXISTS SA\\;SET SCHEMA SA\\;" + CREATE_TABLES,
                "sa", "");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    @BeforeSpecification
    public void setUp() throws Exception {
        dbTester.setDataSet(new TableData("PERSON", "NAME", "AGE", "BIRTHDAY").build());
        dbTester.onSetup();
    }

    public boolean addRecord(String name, String age, String birthday) {
        try {
            Date bd = LocalDateTime.parse(birthday, forPattern("dd.MM.yyyy")).toDate();
            dbTester.setDataSet(new TableData("PERSON",
                "NAME", "AGE", "BIRTHDAY").row(name, age, bd).build());
            dbTester.onSetup();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return true;
    }
}