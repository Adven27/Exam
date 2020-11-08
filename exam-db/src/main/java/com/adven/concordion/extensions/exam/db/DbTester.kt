package com.adven.concordion.extensions.exam.db

import org.dbunit.JdbcDatabaseTester
import org.dbunit.database.DatabaseConfig
import org.dbunit.database.DatabaseConfig.PROPERTY_DATATYPE_FACTORY
import org.dbunit.database.DatabaseConfig.PROPERTY_METADATA_HANDLER
import org.dbunit.database.IDatabaseConnection
import org.dbunit.ext.db2.Db2DataTypeFactory
import org.dbunit.ext.h2.H2DataTypeFactory
import org.dbunit.ext.hsqldb.HsqldbDataTypeFactory
import org.dbunit.ext.mysql.MySqlDataTypeFactory
import org.dbunit.ext.mysql.MySqlMetadataHandler
import org.dbunit.ext.oracle.OracleDataTypeFactory
import org.dbunit.ext.postgresql.PostgresqlDataTypeFactory
import java.util.concurrent.ConcurrentHashMap

/**
 * @param config properties for org.dbunit.database.DatabaseConfig
 */
open class DbTester @JvmOverloads constructor(
    driver: String,
    url: String,
    user: String,
    password: String,
    schema: String? = null,
    val dbUnitConfig: DbUnitConfig = DbUnitConfig()
) : JdbcDatabaseTester(driver, url, user, password, schema) {

    companion object {
        const val DEFAULT_DATASOURCE = "default"
    }

    val executors = ConcurrentHashMap<String, DbTester>()

    fun connectionFor(ds: String?): IDatabaseConnection {
        return executors[ds]?.connection ?: throw IllegalArgumentException("DB tester $ds not found. Registered: $executors")
    }

    override fun getConnection(): IDatabaseConnection {
        val conn = super.getConnection()
        val dbName: String = conn.connection.metaData.databaseProductName
        val cfg = conn.config

        cfg.setProperty(DatabaseConfig.FEATURE_ALLOW_EMPTY_FIELDS, true)
        when (dbName) {
            "HSQL Database Engine" -> cfg.setProperty(PROPERTY_DATATYPE_FACTORY, HsqldbDataTypeFactory())
            "H2" -> cfg.setProperty(PROPERTY_DATATYPE_FACTORY, H2DataTypeFactory())
            "Db2" -> cfg.setProperty(PROPERTY_DATATYPE_FACTORY, Db2DataTypeFactory())
            "Oracle" -> cfg.setProperty(PROPERTY_DATATYPE_FACTORY, OracleDataTypeFactory())
            "PostgreSQL" -> cfg.setProperty(PROPERTY_DATATYPE_FACTORY, PostgresqlDataTypeFactory())
            "MySQL" -> {
                cfg.setProperty(PROPERTY_DATATYPE_FACTORY, MySqlDataTypeFactory())
                cfg.setProperty(PROPERTY_METADATA_HANDLER, MySqlMetadataHandler())
            }
            else -> System.err.println("No matching database product found $dbName")
        }
        dbUnitConfig.databaseConfigProperties.forEach { (k, v) -> cfg.setProperty(k, v) }
        return conn
    }
}
