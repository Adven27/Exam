package specs.db

import com.adven.concordion.extensions.exam.db.builder.DataSetBuilder
import org.joda.time.LocalDateTime
import org.joda.time.format.DateTimeFormat.forPattern
import specs.Specs

open class Db : Specs() {

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

    fun addRecordWithField(id: String, name: String, age: String, birthday: String, field: String, value: String): Boolean {
        addRecord(id, name, age, birthday)
        try {
            dbTester.dataSet = DataSetBuilder().newRowTo("PERSON_FIELDS")
                .withFields(listOf("ID", "NAME", "VALUE", "PERSON_ID").zip(listOf(id, field, value, id)).toMap())
                .add().build()
            dbTester.onSetup()
        } catch (e: Exception) {
            throw RuntimeException(e)
        }
        return true
    }
}