package io.github.adven27.concordion.extensions.exam.db

import mu.KLogging
import org.dbunit.JdbcDatabaseTester
import org.dbunit.database.DatabaseConfig
import org.dbunit.database.DatabaseConfig.FEATURE_ALLOW_EMPTY_FIELDS
import org.dbunit.database.DatabaseConfig.PROPERTY_DATATYPE_FACTORY
import org.dbunit.database.DatabaseConfig.PROPERTY_METADATA_HANDLER
import org.dbunit.database.IDatabaseConnection
import org.dbunit.dataset.datatype.AbstractDataType
import org.dbunit.dataset.datatype.DataType
import org.dbunit.ext.db2.Db2DataTypeFactory
import org.dbunit.ext.db2.Db2MetadataHandler
import org.dbunit.ext.h2.H2DataTypeFactory
import org.dbunit.ext.hsqldb.HsqldbDataTypeFactory
import org.dbunit.ext.mssql.MsSqlDataTypeFactory
import org.dbunit.ext.mysql.MySqlDataTypeFactory
import org.dbunit.ext.mysql.MySqlMetadataHandler
import org.dbunit.ext.oracle.OracleDataTypeFactory
import org.dbunit.ext.postgresql.PostgresqlDataTypeFactory
import org.postgresql.util.PGobject
import java.sql.PreparedStatement
import java.sql.ResultSet
import java.sql.SQLException
import java.sql.Statement
import java.sql.Types
import java.util.concurrent.ConcurrentHashMap

@Suppress("LongParameterList")
open class DbTester @JvmOverloads constructor(
    driver: String,
    url: String,
    user: String,
    password: String,
    schema: String? = null,
    val dbUnitConfig: DbUnitConfig = DbUnitConfig(),
    private val dataTypeConfig: Map<String, (DatabaseConfig) -> DatabaseConfig> = DATA_TYPES,
) : JdbcDatabaseTester(driver, url, user, password, schema), AutoCloseable {

    companion object : KLogging() {
        const val DEFAULT_DATASOURCE = "default"
        val DATA_TYPES: Map<String, (DatabaseConfig) -> DatabaseConfig> = mapOf(
            "Db2" to {
                it.apply {
                    setProperty(PROPERTY_DATATYPE_FACTORY, Db2DataTypeFactory())
                    setProperty(PROPERTY_METADATA_HANDLER, Db2MetadataHandler())
                }
            },
            "DB2/LINUXX8664" to {
                it.apply {
                    setProperty(PROPERTY_DATATYPE_FACTORY, Db2DataTypeFactory())
                    setProperty(PROPERTY_METADATA_HANDLER, Db2MetadataHandler())
                }
            },
            "MySQL" to {
                it.apply {
                    setProperty(PROPERTY_DATATYPE_FACTORY, MySqlDataTypeFactory())
                    setProperty(PROPERTY_METADATA_HANDLER, MySqlMetadataHandler())
                }
            },
            "HSQL Database Engine" to { it.apply { setProperty(PROPERTY_DATATYPE_FACTORY, HsqldbDataTypeFactory()) } },
            "H2" to { it.apply { setProperty(PROPERTY_DATATYPE_FACTORY, H2DataTypeFactory()) } },
            "Oracle" to { it.apply { setProperty(PROPERTY_DATATYPE_FACTORY, OracleDataTypeFactory()) } },
            "PostgreSQL" to { it.apply { setProperty(PROPERTY_DATATYPE_FACTORY, JsonbPostgresqlDataTypeFactory()) } },
            "Microsoft SQL Server" to { it.apply { setProperty(PROPERTY_DATATYPE_FACTORY, MsSqlDataTypeFactory()) } }
        )
    }

    val executors = ConcurrentHashMap<String, DbTester>()
    private var conn: IDatabaseConnection? = null

    fun connectionFor(ds: String?): IDatabaseConnection = executors[ds]?.connection
        ?: throw IllegalArgumentException("DB tester $ds not found. Registered: $executors")

    override fun getConnection(): IDatabaseConnection =
        if (conn == null || conn!!.connection.isClosed) createConnection().also { conn = it } else conn!!

    private fun createConnection(): IDatabaseConnection {
        val conn = super.getConnection()
        val cfg = conn.config

        setDbSpecificProperties(conn.connection.metaData.databaseProductName, cfg)
        cfg.setProperty(FEATURE_ALLOW_EMPTY_FIELDS, true)
        dbUnitConfig.databaseConfigProperties.forEach { (k, v) -> cfg.setProperty(k, v) }
        return conn
    }

    private fun setDbSpecificProperties(dbName: String, cfg: DatabaseConfig) =
        dataTypeConfig[dbName]?.let { it(cfg) } ?: logger.error("No matching database product found $dbName")

    override fun close() {
        try {
            if (conn?.connection?.isClosed != true) conn?.close()
        } catch (e: SQLException) {
            logger.warn("Error on connection closing", e)
        }
    }

    @Suppress("unused")
    fun <R> useStatement(fn: (Statement) -> R): R = connection.connection.createStatement().use { fn(it) }
}

class JsonbPostgresqlDataTypeFactory : PostgresqlDataTypeFactory() {
    override fun createDataType(sqlType: Int, sqlTypeName: String?): DataType =
        if (sqlTypeName == "jsonb") JsonbDataType() else super.createDataType(sqlType, sqlTypeName)

    class JsonbDataType : AbstractDataType("jsonb", Types.OTHER, String::class.java, false) {
        override fun typeCast(obj: Any?): Any = obj.toString()

        override fun getSqlValue(column: Int, resultSet: ResultSet): Any = resultSet.getString(column)

        override fun setSqlValue(value: Any?, column: Int, statement: PreparedStatement) = statement.setObject(
            column,
            PGobject().apply {
                this.type = "json"
                this.value = value?.toString()
            }
        )
    }
}
