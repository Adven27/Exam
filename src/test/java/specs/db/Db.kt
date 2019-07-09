package specs.db

import com.adven.concordion.extensions.exam.db.builder.DataSetBuilder
import org.concordion.api.BeforeSpecification
import org.joda.time.LocalDateTime
import org.joda.time.format.DateTimeFormat.forPattern
import specs.Specs

open class Db : Specs() {

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
}