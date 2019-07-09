@file:JvmName("SutApp")

package app

import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.joda.JodaModule
import io.ktor.application.Application
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.application.log
import io.ktor.features.CallLogging
import io.ktor.features.ContentNegotiation
import io.ktor.http.HttpStatusCode
import io.ktor.jackson.jackson
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.routing.*
import io.ktor.server.engine.embeddedServer
import io.ktor.server.jetty.Jetty
import org.jetbrains.exposed.sql.*
import org.joda.time.DateTime
import java.util.*
import java.util.concurrent.TimeUnit

fun Application.module() {
    install(CallLogging)
    install(ContentNegotiation) {
        jackson {
            setTimeZone(TimeZone.getDefault())
            registerModule(JodaModule()/*.addSerializer(DateTime::class.java,
                DateTimeSerializer(
                        JacksonJodaDateFormat(DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss Z")), 2))*/)
            configure(SerializationFeature.INDENT_OUTPUT, true)
            configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
        }
    }
    DatabaseFactory.init()
    val service = WidgetService()
    routing {
        get("/widgets/") {
            log.info("service.getAll: " + service.getAll().toString())
            call.respond(service.getAll())
        }

        get("/widgets/{id}") {
            val widget = service.getBy(call.parameters["id"]?.toInt()!!)
            if (widget == null) call.respond(HttpStatusCode.NotFound)
            else call.respond(widget)
        }

        post("/widgets/") {
            val widget = call.receive<NewWidget>()
            return@post when {
                widget.name == null -> call.respond(HttpStatusCode.BadRequest, mapOf("error" to "name is required"))
                widget.quantity == null -> call.respond(HttpStatusCode.BadRequest, mapOf("error" to "quantity is required"))
                else -> call.respond(HttpStatusCode.Created, service.add(widget))
            }
        }

        put("/widgets/") {
            val widget = call.receive<NewWidget>()
            val updated = service.update(widget)
            if (updated == null) call.respond(HttpStatusCode.NotFound)
            else call.respond(HttpStatusCode.OK, updated)
        }

        delete("/widgets/{id}") {
            val removed = service.delete(call.parameters["id"]?.toInt()!!)
            //FIXME empty body response check
            if (removed) call.respond(HttpStatusCode.OK, mapOf("result" to "ok"))
            else call.respond(HttpStatusCode.NotFound, mapOf("result" to "error"))
        }
    }
}

private val engine = embeddedServer(Jetty, 8080, watchPaths = listOf("MainKt"), module = Application::module)

fun main() {
    start()
}

fun start() = engine.start()
fun stop() = engine.stop(1L, 2L, TimeUnit.SECONDS)

class WidgetService {
    suspend fun getAll(): List<Widget> = DatabaseFactory.dbQuery { Widgets.selectAll().map { toWidget(it) } }

    suspend fun getBy(id: Int): Widget? = DatabaseFactory.dbQuery {
        Widgets.select { (Widgets.id eq id) }.mapNotNull { toWidget(it) }.singleOrNull()
    }

    suspend fun update(widget: NewWidget): Widget? {
        val id = widget.id
        return if (id == null) {
            add(widget)
        } else {
            DatabaseFactory.dbQuery {
                Widgets.update({ Widgets.id eq id }) {
                    it[name] = widget.name!!
                    it[quantity] = widget.quantity!!
                    it[updatedAt] = DateTime()
                }
            }
            getBy(id)
        }
    }

    suspend fun add(widget: NewWidget): Widget {
        var key = 0
        DatabaseFactory.dbQuery {
            key = (Widgets.insert {
                it[name] = widget.name!!
                it[quantity] = widget.quantity!!
                it[updatedAt] = DateTime()
            } get Widgets.id)
        }
        return getBy(key)!!
    }

    suspend fun delete(id: Int): Boolean = DatabaseFactory.dbQuery { Widgets.deleteWhere { Widgets.id eq id } > 0 }

    private fun toWidget(row: ResultRow) = Widget(
            id = row[Widgets.id],
            name = row[Widgets.name],
            quantity = row[Widgets.quantity],
            updatedAt = row[Widgets.updatedAt]
    )
}