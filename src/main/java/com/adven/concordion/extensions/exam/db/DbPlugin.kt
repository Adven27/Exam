package com.adven.concordion.extensions.exam.db

import com.adven.concordion.extensions.exam.core.ExamPlugin
import com.adven.concordion.extensions.exam.core.commands.ExamCommand
import com.adven.concordion.extensions.exam.core.html.TABLE
import com.adven.concordion.extensions.exam.db.commands.DBCheckCommand
import com.adven.concordion.extensions.exam.db.commands.DBCleanCommand
import com.adven.concordion.extensions.exam.db.commands.DBSetCommand
import com.adven.concordion.extensions.exam.db.commands.DBShowCommand
import org.joda.time.format.DateTimeFormat
import java.util.*
import kotlin.collections.List
import kotlin.collections.Map
import kotlin.collections.component1
import kotlin.collections.component2
import kotlin.collections.contentToString
import kotlin.collections.emptyMap
import kotlin.collections.iterator
import kotlin.collections.listOf
import kotlin.collections.set

class DbPlugin(private val dbTester: DbTester, private val valuePrinter: ValuePrinter = ValuePrinter.Simple()) : ExamPlugin {

    /***
     * @param dbUnitConfig properties for org.dbunit.database.DatabaseConfig
     */
    @JvmOverloads
    @Suppress("unused")
    constructor(
        driver: String,
        url: String,
        user: String,
        password: String,
        schema: String? = null,
        dbUnitConfig: Map<String, Any?> = emptyMap(),
        valuePrinter: ValuePrinter = ValuePrinter.Simple()
    ) : this(DbTester(driver, url, user, password, schema, dbUnitConfig), valuePrinter)

    init {
        dbTester.executors[DbTester.DEFAULT_DATASOURCE] = dbTester
    }

    /**
     * @param defaultTester Default datasource: <e:db-set
     * @param others map of additional datasources: <e:db-set ds="other"
     *
     *
     * DbTester(
     * "org.h2.Driver", "jdbc:h2:mem:test;INIT=CREATE SCHEMA IF NOT EXISTS SA\\;SET SCHEMA SA", "sa", ""
     * ),
     * mapOf("other" to DbTester(
     * "org.postgresql.Driver", "jdbc:postgresql://localhost:5432/postgres", "postgres", "postgres"
     * ))
     *
     */
    @Suppress("unused")
    constructor(defaultTester: DbTester, others: Map<String, DbTester>) : this(defaultTester) {
        for ((key, value) in others) {
            dbTester.executors[key] = value
        }
    }

    override fun commands(): List<ExamCommand> = listOf(
        DBShowCommand("db-show", TABLE, dbTester, valuePrinter),
        DBCheckCommand("db-check", TABLE, dbTester, valuePrinter),
        DBSetCommand("db-set", TABLE, dbTester, valuePrinter),
        DBCleanCommand("db-clean", "span", dbTester)
    )

    interface ValuePrinter {
        class Simple @JvmOverloads constructor(private val dateFormat: String = "yyyy-MM-dd hh:mm:ss.sss") : ValuePrinter {
            override fun print(value: Any?): String = when (value) {
                value == null -> "(null)"
                is Array<*> -> value.contentToString()
                is Date -> DateTimeFormat.forPattern(dateFormat).print(value.time)
                else -> value.toString()
            }
        }

        fun print(value: Any?): String
    }
}