@file:JvmName("SutApp")

package app

import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer
import io.ktor.application.Application
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.application.log
import io.ktor.features.CallLogging
import io.ktor.features.ContentNegotiation
import io.ktor.http.HttpStatusCode
import io.ktor.jackson.jackson
import io.ktor.request.receive
import io.ktor.request.receiveOrNull
import io.ktor.response.respond
import io.ktor.routing.delete
import io.ktor.routing.get
import io.ktor.routing.post
import io.ktor.routing.put
import io.ktor.routing.routing
import io.ktor.server.engine.embeddedServer
import io.ktor.server.jetty.Jetty
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.update
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.TimeZone

@Suppress("LongMethod")
fun Application.module() {
    install(CallLogging)
    install(ContentNegotiation) {
        jackson {
            setTimeZone(TimeZone.getDefault())
            registerModule(
                JavaTimeModule()
                    .addSerializer(LocalDateTime::class.java, LocalDateTimeSerializer(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS")))
            )
            configure(SerializationFeature.INDENT_OUTPUT, true)
            configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
        }
    }
    DatabaseFactory.init()
    val jobs: MutableList<Int> = ArrayList()
    val widgetService = WidgetService()
    val jobService = JobService()
    routing {
        post("/jobs") {
            log.info("trigger job " + call.receiveOrNull<NewWidget>())
            val id = jobService.add()
            jobs.add(id)

            // do some hard work async
            GlobalScope.launch {
                delay(2000L)
                jobService.update(id, "done")
                jobs.remove(id)
            }
            call.respond("""{ "id" : $id }""")
        }

        get("/jobs/{id}") {
            log.info("is job running?")
            call.respond("""{ "running" : "${jobs.contains(call.parameters["id"]?.toInt()!!)}" }""")
        }

        get("/widgets/") {
            log.info("service.getAll: " + widgetService.getAll().toString())
            call.respond(widgetService.getAll())
        }

        get("/widgets/{id}") {
            val widget = widgetService.getBy(call.parameters["id"]?.toInt()!!)
            if (widget == null) call.respond(HttpStatusCode.NotFound)
            else call.respond(widget)
        }

        post("/widgets/") {
            val widget = call.receive<NewWidget>()
            return@post when {
                widget.name == null -> call.respond(HttpStatusCode.BadRequest, mapOf("error" to "name is required"))
                widget.quantity == null -> call.respond(
                    HttpStatusCode.BadRequest,
                    mapOf("error" to "quantity is required")
                )
                else -> {
                    try {
                        call.respond(HttpStatusCode.Created, widgetService.add(widget))
                    } catch (expected: Exception) {
                        call.respond(HttpStatusCode.BadRequest, mapOf("error" to expected.message))
                    }
                }
            }
        }

        put("/widgets/") {
            val widget = call.receive<NewWidget>()
            val updated = widgetService.update(widget)
            if (updated == null) call.respond(HttpStatusCode.NotFound)
            else call.respond(HttpStatusCode.OK, updated)
        }

        delete("/widgets/{id}") {
            val removed = widgetService.delete(call.parameters["id"]?.toInt()!!)
            if (removed) call.respond(HttpStatusCode.OK)
            else call.respond(HttpStatusCode.NotFound)
        }
    }
}

private val engine = embeddedServer(Jetty, 8080, watchPaths = listOf("MainKt"), module = Application::module)

fun main() {
    start()
}

fun start() = engine.start()
fun stop() = engine.stop(1000L, 2000L)

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
                    it[updatedAt] = LocalDateTime.now()
                }
            }
            getBy(id)
        }
    }

    suspend fun add(widget: NewWidget): Widget {
        var key = 0
        DatabaseFactory.dbQuery {
            key = (
                Widgets.insert {
                    it[name] = if (widget.name!!.isBlank()) throw BlankNotAllowed() else widget.name
                    it[quantity] = widget.quantity!!
                    it[updatedAt] = LocalDateTime.now()
                } get Widgets.id
                )
        }
        return getBy(key)!!
    }

    class BlankNotAllowed : RuntimeException("blank value not allowed")

    suspend fun delete(id: Int): Boolean = DatabaseFactory.dbQuery { Widgets.deleteWhere { Widgets.id eq id } > 0 }

    private fun toWidget(row: ResultRow) = Widget(
        id = row[Widgets.id],
        name = row[Widgets.name],
        quantity = row[Widgets.quantity],
        updatedAt = row[Widgets.updatedAt]
    )
}

class JobService {
    suspend fun add(): Int {
        var key = 0
        DatabaseFactory.dbQuery {
            key = (JobResult.insert {} get JobResult.id)
        }
        return key
    }

    suspend fun update(id: Int, jobResult: String) {
        DatabaseFactory.dbQuery {
            JobResult.update({ JobResult.id eq id }) {
                it[result] = jobResult
            }
        }
    }
}
