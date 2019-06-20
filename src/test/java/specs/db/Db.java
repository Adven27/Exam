package specs.db;

import com.adven.concordion.extensions.exam.db.ExamDbTester;
import com.adven.concordion.extensions.exam.db.TableData;
import org.concordion.api.BeforeSpecification;
import org.dbunit.IDatabaseTester;
import org.joda.time.LocalDateTime;
import specs.Specs;

import java.util.Date;

import static org.joda.time.format.DateTimeFormat.forPattern;

public class Db extends Specs {
    private static final String CREATE_TABLES = "CREATE TABLE IF NOT EXISTS PERSON "
        + "(ID INT PRIMARY KEY, NAME VARCHAR2(255 CHAR), AGE NUMBER, IQ NUMBER, BIRTHDAY TIMESTAMP)\\;"
        + "CREATE TABLE IF NOT EXISTS EMPTY (NAME VARCHAR2(255 CHAR), VALUE NUMBER)";

    private final IDatabaseTester dbTester = dbTester();

    private static IDatabaseTester dbTester() {
        return new ExamDbTester(
            "org.h2.Driver", "jdbc:h2:mem:test;DB_CLOSE_DELAY=-1;"
            + "INIT=CREATE SCHEMA IF NOT EXISTS SA\\;SET SCHEMA SA\\;" + CREATE_TABLES,
            "sa", "");
    }

    @BeforeSpecification
    public void setUp() throws Exception {
        dbTester.setDataSet(new TableData("PERSON", "NAME", "AGE", "BIRTHDAY").build());
        dbTester.onSetup();
    }

    public boolean addRecord(String id, String name, String age, String birthday) {
        try {
            Date bd = LocalDateTime.parse(birthday, forPattern("dd.MM.yyyy")).toDate();
            dbTester.setDataSet(
                new TableData("PERSON", "ID", "NAME", "AGE", "BIRTHDAY").row(id, name, age, bd).build()
            );
            dbTester.onSetup();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return true;
    }
}