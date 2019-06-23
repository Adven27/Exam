package com.adven.concordion.extensions.exam.db

import com.adven.concordion.extensions.exam.core.ExamPlugin
import com.adven.concordion.extensions.exam.core.commands.ExamCommand
import com.adven.concordion.extensions.exam.core.html.TABLE
import com.adven.concordion.extensions.exam.db.commands.DBCheckCommand
import com.adven.concordion.extensions.exam.db.commands.DBCleanCommand
import com.adven.concordion.extensions.exam.db.commands.DBSetCommand
import com.adven.concordion.extensions.exam.db.commands.DBShowCommand

class DbPlugin(private val dbTester: DbTester) : ExamPlugin {

    /***
     * @param dbUnitConfig properties for org.dbunit.database.DatabaseConfig
     */
    @JvmOverloads
    constructor(
        driver: String,
        url: String,
        user: String,
        password: String,
        schema: String? = null,
        dbUnitConfig: Map<String, Any?> = emptyMap()
    ) : this(DbTester(driver, url, user, password, schema, dbUnitConfig))

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
        DBShowCommand("db-show", TABLE, dbTester),
        DBCheckCommand("db-check", TABLE, dbTester),
        DBSetCommand("db-set", TABLE, dbTester),
        DBCleanCommand("db-clean", "span", dbTester)
    )
}
