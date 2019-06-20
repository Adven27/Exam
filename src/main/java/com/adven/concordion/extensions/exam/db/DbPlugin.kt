package com.adven.concordion.extensions.exam.db

import com.adven.concordion.extensions.exam.core.ExamPlugin
import com.adven.concordion.extensions.exam.core.commands.ExamCommand
import com.adven.concordion.extensions.exam.db.commands.DBCheckCommand
import com.adven.concordion.extensions.exam.db.commands.DBSetCommand
import com.adven.concordion.extensions.exam.db.commands.DBShowCommand

class DbPlugin(private val dbTester: ExamDbTester) : ExamPlugin {

    @Suppress("unused")
    constructor(
        driver: String,
        url: String,
        user: String,
        password: String,
        schema: String? = null,
        config: Map<String, Any?> = emptyMap()
    ) : this(ExamDbTester(driver, url, user, password, schema, config))

    init {
        dbTester.executors[ExamDbTester.DEFAULT_DATASOURCE] = dbTester
    }

    /**
     * @param defaultDB Default datasource: <e:db-set
     * @param others map of additional datasources: <e:db-set ds="other"
     *
     *
     * ExamDbTester(
     * "org.h2.Driver", "jdbc:h2:mem:test;INIT=CREATE SCHEMA IF NOT EXISTS SA\\;SET SCHEMA SA", "sa", ""
     * ),
     * mapOf("other" to ExamDbTester(
     * "org.postgresql.Driver", "jdbc:postgresql://localhost:5432/postgres", "postgres", "postgres"
     * ))
     *
     */
    @Suppress("unused")
    constructor(defaultDB: ExamDbTester, others: Map<String, ExamDbTester>) : this(defaultDB) {
        for ((key, value) in others) {
            dbTester.executors[key] = value
        }
    }

    override fun commands(): List<ExamCommand> = listOf(
        DBShowCommand("db-show", TABLE, dbTester),
        DBCheckCommand("db-check", TABLE, dbTester),
        DBSetCommand("db-set", TABLE, dbTester)
    )

    companion object {
        private const val TABLE = "table"
    }
}
