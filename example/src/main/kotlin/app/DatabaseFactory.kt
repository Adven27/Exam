package app

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils.create
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.javatime.datetime
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.LocalDateTime

@Suppress("MagicNumber")
object DatabaseFactory {
    fun init() {
        Database.connect(
            "jdbc:h2:mem:test;DB_CLOSE_DELAY=-1;INIT=CREATE SCHEMA IF NOT EXISTS SA\\;SET SCHEMA SA",
            driver = "org.h2.Driver",
            user = "sa"
        )
        transaction {
            create(JobResult)
            create(Widgets)
            Widgets.insert {
                it[name] = "widget one"
                it[quantity] = 27
                it[updatedAt] = LocalDateTime.now()
            }
            Widgets.insert {
                it[name] = "widget two"
                it[quantity] = 14
                it[updatedAt] = LocalDateTime.now()
            }
        }
    }

    suspend fun <T> dbQuery(block: () -> T): T = withContext(Dispatchers.IO) { transaction { block() } }
}

@Suppress("MagicNumber")
object Widgets : Table() {
    val id = integer("id").autoIncrement()
    val name = varchar("name", 10)
    val quantity = integer("quantity")
    val updatedAt = datetime("updated")
    override val primaryKey = PrimaryKey(id)
}

@Suppress("MagicNumber")
object JobResult : Table() {
    val id = integer("id").autoIncrement()
    val result = varchar("result", 10).nullable()
    override val primaryKey = PrimaryKey(id)
}

data class Widget(val id: Int, val name: String, val quantity: Int, val updatedAt: LocalDateTime)
data class NewWidget(val id: Int?, val name: String?, val quantity: Int?)
