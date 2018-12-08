package com.adven.concordion.extensions.exam.configurators

import com.adven.concordion.extensions.exam.ExamExtension
import org.dbunit.JdbcDatabaseTester
import org.dbunit.database.DatabaseConfig.PROPERTY_DATATYPE_FACTORY
import org.dbunit.database.IDatabaseConnection
import org.dbunit.ext.db2.Db2DataTypeFactory
import org.dbunit.ext.h2.H2DataTypeFactory
import org.dbunit.ext.hsqldb.HsqldbDataTypeFactory
import org.dbunit.ext.oracle.OracleDataTypeFactory
import org.dbunit.ext.postgresql.PostgresqlDataTypeFactory
import java.util.*

class DbTester(private val extension: ExamExtension) {
    private var driver = "org.h2.Driver"
    private var url = "jdbc:h2:mem:test;INIT=CREATE SCHEMA IF NOT EXISTS SA\\;SET SCHEMA SA"
    private var user = "sa"
    private var schema: String? = null
    private var password = ""

    fun driver(driver: String): DbTester {
        this.driver = driver
        return this
    }

    fun url(url: String): DbTester {
        this.url = url
        return this
    }

    fun user(user: String): DbTester {
        this.user = user
        return this
    }

    fun password(password: String): DbTester {
        this.password = password
        return this
    }

    fun schema(schema: String): DbTester {
        this.schema = schema
        return this
    }

    fun from(props: Properties): DbTester {
        driver = props.getProperty("hibernate.connection.driver_class")
        url = props.getProperty("connection.url")
        user = props.getProperty("hibernate.connection.username")
        password = props.getProperty("hibernate.connection.password")
        return this
    }

    fun end(): ExamExtension {
        return extension.dbTester(ExamDbTester(driver, url, user, password, schema))
    }
}

/**
 * Fix for warning "Potential problem found:
 * The configured data type factory 'class org.dbunit.dataset.datatype.DefaultDataTypeFactory'"
 */
class ExamDbTester(driver: String, url: String, user: String, password: String, schema: String?)
    : JdbcDatabaseTester(driver, url, user, password, schema) {
    override fun getConnection(): IDatabaseConnection {
        val conn = super.getConnection()
        val dbName: String = conn.connection.metaData.databaseProductName
        val cfg = conn.config
        when (dbName) {
            "HSQL Database Engine" -> cfg.setProperty(PROPERTY_DATATYPE_FACTORY, HsqldbDataTypeFactory())
            "H2" -> cfg.setProperty(PROPERTY_DATATYPE_FACTORY, H2DataTypeFactory())
            "Db2" -> cfg.setProperty(PROPERTY_DATATYPE_FACTORY, Db2DataTypeFactory())
            "Oracle" -> cfg.setProperty(PROPERTY_DATATYPE_FACTORY, OracleDataTypeFactory())
            "PostgreSQL" -> cfg.setProperty(PROPERTY_DATATYPE_FACTORY, PostgresqlDataTypeFactory())
            else -> System.err.println("No matching database product found $dbName")
        }
        return conn
    }
}