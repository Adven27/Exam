package specs.db

import io.github.adven27.concordion.extensions.exam.core.toDate
import io.github.adven27.concordion.extensions.exam.db.builder.DataSetBuilder
import specs.Specs
import java.time.LocalDate

open class Db : Specs() {

    fun addRecord(id: String, name: String, age: String, birthday: String): Boolean {
        dbTester.apply {
            dataSet = DataSetBuilder().newRowTo("PERSON")
                .withFields(
                    mapOf(
                        "ID" to id,
                        "NAME" to name,
                        "AGE" to age,
                        "BIRTHDAY" to LocalDate.parse(birthday).toDate()
                    )
                )
                .add().build()
            onSetup()
        }
        return true
    }

    fun addRecordWithField(
        id: String,
        name: String,
        age: String,
        birthday: String,
        field: String,
        value: String
    ): Boolean {
        addRecord(id, name, age, birthday)
        dbTester.apply {
            dataSet = DataSetBuilder().newRowTo("PERSON_FIELDS")
                .withFields(mapOf("ID" to id, "NAME" to field, "VALUE" to value, "PERSON_ID" to id))
                .add().build()
            onSetup()
        }
        return true
    }
}

open class DbSet : Specs() {
    fun insert(table: String, id: String, name: String, height: String, weight: String, manufactured: String): Boolean {
        dbTester.apply {
            dataSet = DataSetBuilder().newRowTo(table)
                .withFields(
                    mapOf(
                        "ID" to id,
                        "NAME" to name,
                        "HEIGHT" to height,
                        "WEIGHT" to weight,
                        "BIRTHDAY" to LocalDate.parse(manufactured).toDate()
                    )
                ).add().build()
            onSetup()
        }
        return true
    }
}
