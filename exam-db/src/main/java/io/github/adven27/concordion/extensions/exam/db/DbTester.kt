package io.github.adven27.concordion.extensions.exam.db

import mu.KLogging
import org.dbunit.JdbcDatabaseTester
import org.dbunit.database.DatabaseConfig
import org.dbunit.database.DatabaseConfig.PROPERTY_DATATYPE_FACTORY
import org.dbunit.database.DatabaseConfig.PROPERTY_METADATA_HANDLER
import org.dbunit.database.IDatabaseConnection
import org.dbunit.dataset.datatype.AbstractDataType
import org.dbunit.dataset.datatype.DataType
import org.dbunit.ext.db2.Db2DataTypeFactory
import org.dbunit.ext.db2.Db2MetadataHandler
import org.dbunit.ext.h2.H2DataTypeFactory
import org.dbunit.ext.hsqldb.HsqldbDataTypeFactory
import org.dbunit.ext.mysql.MySqlDataTypeFactory
import org.dbunit.ext.mysql.MySqlMetadataHandler
import org.dbunit.ext.oracle.OracleDataTypeFactory
import org.dbunit.ext.postgresql.PostgresqlDataTypeFactory
import org.postgresql.util.PGobject
import java.sql.PreparedStatement
import java.sql.ResultSet
import java.sql.Types
import java.util.concurrent.ConcurrentHashMap

open class DbTester @JvmOverloads constructor(
    driver: String,
    url: String,
    user: String,
    password: String,
    schema: String? = null,
    val dbUnitConfig: DbUnitConfig = DbUnitConfig()
) : JdbcDatabaseTester(driver, url, user, password, schema), AutoCloseable {

    companion object : KLogging() {
        const val DEFAULT_DATASOURCE = "default"
    }

    val executors = ConcurrentHashMap<String, DbTester>()
    private var conn: IDatabaseConnection? = null

    fun connectionFor(ds: String?): IDatabaseConnection = executors[ds]?.connection
        ?: throw IllegalArgumentException("DB tester $ds not found. Registered: $executors")

    override fun getConnection(): IDatabaseConnection = if (conn == null || conn!!.connection.isClosed) {
        createConnection().also { conn = it }
    } else {
        conn!!
    }

    private fun createConnection(): IDatabaseConnection {
        val conn = super.getConnection()
        val cfg = conn.config

        setDbSpecificProperties(conn.connection.metaData.databaseProductName, cfg)
        cfg.setProperty(DatabaseConfig.FEATURE_ALLOW_EMPTY_FIELDS, true)
        dbUnitConfig.databaseConfigProperties.forEach { (k, v) -> cfg.setProperty(k, v) }
        return conn
    }

    private fun setDbSpecificProperties(dbName: String, cfg: DatabaseConfig) {
        when (dbName) {
            "HSQL Database Engine" -> cfg.setProperty(PROPERTY_DATATYPE_FACTORY, HsqldbDataTypeFactory())
            "H2" -> cfg.setProperty(PROPERTY_DATATYPE_FACTORY, H2DataTypeFactory())
            "Db2", "DB2/LINUXX8664" -> {
                cfg.setProperty(PROPERTY_DATATYPE_FACTORY, Db2DataTypeFactory())
                cfg.setProperty(PROPERTY_METADATA_HANDLER, Db2MetadataHandler())
            }
            "Oracle" -> cfg.setProperty(PROPERTY_DATATYPE_FACTORY, OracleDataTypeFactory())
            "PostgreSQL" -> cfg.setProperty(PROPERTY_DATATYPE_FACTORY, JsonbPostgresqlDataTypeFactory())
            "MySQL" -> {
                cfg.setProperty(PROPERTY_DATATYPE_FACTORY, MySqlDataTypeFactory())
                cfg.setProperty(PROPERTY_METADATA_HANDLER, MySqlMetadataHandler())
            }
            else -> logger.error("No matching database product found $dbName")
        }
    }

    override fun close() {
        conn?.close()
    }
}

class JsonbPostgresqlDataTypeFactory : PostgresqlDataTypeFactory() {
    override fun createDataType(sqlType: Int, sqlTypeName: String?): DataType {
        return when (sqlTypeName) {
            "jsonb" -> return JsonbDataType()
            else -> super.createDataType(sqlType, sqlTypeName)
        }
    }

    class JsonbDataType : AbstractDataType("jsonb", Types.OTHER, String::class.java, false) {
        override fun typeCast(obj: Any?): Any = obj.toString()

        override fun getSqlValue(column: Int, resultSet: ResultSet): Any = resultSet.getString(column)

        override fun setSqlValue(value: Any?, column: Int, statement: PreparedStatement) {
            statement.setObject(
                column,
                PGobject().apply {
                    this.type = "json"
                    this.value = value?.toString()
                }
            )
        }
    }
}
