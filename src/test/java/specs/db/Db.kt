package specs.db

import com.adven.concordion.extensions.exam.db.DbTester
import com.adven.concordion.extensions.exam.db.builder.DataSetBuilder
import org.concordion.api.BeforeSpecification
import org.dbunit.IDatabaseTester
import org.joda.time.LocalDateTime
import org.joda.time.format.DateTimeFormat.forPattern
import specs.Specs

open class Db : Specs() {
    private val dbTester = dbTester()

    @BeforeSpecification
    fun setUp() {
        dbTester.dataSet = DataSetBuilder().newRowTo("PERSON").add().build()
        dbTester.onSetup()
    }

    fun addRecord(id: String, name: String, age: String, birthday: String): Boolean {
        try {
            val bd = LocalDateTime.parse(birthday, forPattern("dd.MM.yyyy")).toDate()
            dbTester.dataSet = DataSetBuilder().newRowTo("PERSON")
                .withFields(listOf("ID", "NAME", "AGE", "BIRTHDAY").zip(listOf(id, name, age, bd)).toMap())
                .add().build()
            dbTester.onSetup()
        } catch (e: Exception) {
            throw RuntimeException(e)
        }
        return true
    }

    companion object {
        private const val CREATE_TABLES = ("CREATE TABLE IF NOT EXISTS PERSON "
                + "(ID INT PRIMARY KEY, NAME VARCHAR2(255 CHAR), AGE NUMBER, IQ NUMBER, BIRTHDAY TIMESTAMP)\\;"
                + "CREATE TABLE IF NOT EXISTS EMPTY (NAME VARCHAR2(255 CHAR), VALUE NUMBER)")

        private fun dbTester(): IDatabaseTester {
            return DbTester(
                "org.h2.Driver", "jdbc:h2:mem:test;DB_CLOSE_DELAY=-1;"
                        + "INIT=CREATE SCHEMA IF NOT EXISTS SA\\;SET SCHEMA SA\\;" + CREATE_TABLES,
                "sa", ""
            )
        }
    }
}