import env.core.Environment
import env.core.Environment.Prop
import env.db.mysql.MySqlContainerSystem
import env.db.postgresql.PostgreSqlContainerSystem
import env.grpc.GrpcMockContainerSystem
import env.mq.rabbit.RabbitContainerSystem
import env.mq.redis.RedisContainerSystem
import env.wiremock.WiremockSystem
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Ignore
import org.junit.Test
import org.testcontainers.containers.output.Slf4jLogConsumer

private const val PG_URL = "jdbc:postgresql://localhost:5432/test?loggerLevel=OFF"

@Ignore
class EnvTest {
    private lateinit var sut: SomeEnvironment

    @Test
    fun fixedEnvironment() {
        System.setProperty("SPECS_FIXED_ENV", "true")

        sut = SomeEnvironment().apply { up() }

        sut.systems.forEach { (_, s) -> assertTrue(s.running()) }
        assertEquals(Prop("env.mq.rabbit.port", "5672"), sut.rabbit().config().port)
        assertEquals("5672", System.getProperty("env.mq.rabbit.port"))
        assertEquals(Prop("env.db.postgresql.url", PG_URL), sut.postgres().config().jdbcUrl)
        assertEquals(PG_URL, System.getProperty("env.db.postgresql.url"))
    }

    @Test
    fun dynamicEnvironment() {
        System.setProperty("SPECS_FIXED_ENV", "false")

        sut = SomeEnvironment().apply { up() }

        sut.systems.forEach { (_, s) -> assertTrue(s.running()) }
        assertNotEquals("5672", sut.rabbit().config().port.value)
        assertNotEquals(PG_URL, sut.postgres().config().jdbcUrl.value)
    }

    @After
    fun tearDown() {
        sut.down()
    }
}

class SomeEnvironment : Environment(
    mapOf(
        "RABBIT" to RabbitContainerSystem(fixedEnv = fixedEnv),
        "REDIS" to RedisContainerSystem(fixedEnv = fixedEnv),
        "POSTGRES" to PostgreSqlContainerSystem(fixedEnv = fixedEnv),
        "MYSQL" to MySqlContainerSystem(fixedEnv = fixedEnv),
        "GRPC" to GrpcMockContainerSystem(1, fixedEnv, listOf("common.proto", "wallet.proto")).apply {
            withLogConsumer(Slf4jLogConsumer(logger).withPrefix("GRPC-$serviceId"))
        },
        "WIREMOCK" to WiremockSystem(fixedEnv)
    )
) {
    fun rabbit() = find<RabbitContainerSystem>("RABBIT")
    fun postgres() = systems["POSTGRES"] as PostgreSqlContainerSystem

    companion object {
        val fixedEnv
            get() = "SPECS_FIXED_ENV".fromPropertyOrElse(false)
    }
}
