import env.container.ContainerizedSystem
import env.core.Environment
import env.db.mysql.SpecAwareMySqlContainer
import env.db.postgresql.SpecAwarePostgreSqlContainer
import env.grpc.GrpcMockContainer
import env.mq.rabbit.SpecAwareRabbitContainer
import env.mq.redis.SpecAwareRedisContainer
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
    private lateinit var environment: Environment

    @Test
    fun fixedEnvironment() {
        environment = SomeEnvironment(true).apply { up() }

        environment.systems.forEach { (_, s) -> assertTrue(s.running()) }
        assertEquals(5672, environment.find<SpecAwareRabbitContainer>("RABBIT").port())
        assertEquals("5672", System.getProperty("env.mq.rabbit.port"))
        assertEquals(PG_URL, environment.find<SpecAwarePostgreSqlContainer>("POSTGRES").jdbcUrl)
        assertEquals(PG_URL, System.getProperty("env.db.postgresql.url"))
    }

    @Test
    fun dynamicEnvironment() {
        environment = SomeEnvironment(false).apply { up() }

        environment.systems.forEach { (_, s) -> assertTrue(s.running()) }
        assertNotEquals(5672, environment.find<SpecAwareRabbitContainer>("RABBIT").port())
        assertNotEquals(PG_URL, environment.find<SpecAwarePostgreSqlContainer>("POSTGRES").jdbcUrl)
    }

    @After
    fun tearDown() {
        environment.down()
    }
}

class SomeEnvironment(fixed: Boolean) : Environment(
    mapOf(
        "RABBIT" to ContainerizedSystem(
            SpecAwareRabbitContainer(fixedEnv = fixed)
                .withLogConsumer(Slf4jLogConsumer(logger).withPrefix("RABBIT"))
        ),
        "REDIS" to ContainerizedSystem(SpecAwareRedisContainer(fixedEnv = fixed)),
        "POSTGRES" to ContainerizedSystem(SpecAwarePostgreSqlContainer(fixedEnv = fixed)),
        "MYSQL" to ContainerizedSystem(SpecAwareMySqlContainer(fixedEnv = fixed)),
        "GRPC" to ContainerizedSystem(GrpcMockContainer(1, fixed, listOf("common.proto", "wallet.proto"))),
        "WIREMOCK" to WiremockSystem(fixed)
    )
)
