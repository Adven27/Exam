import env.container.ContainerOperator
import env.core.Environment
import env.db.mysql.EnvAwareMySqlContainer
import env.db.postgresql.EnvAwarePostgreSqlContainer
import env.grpc.GrpcMockContainer
import env.mq.rabbit.EnvAwareRabbitContainer
import env.mq.redis.EnvAwareRedisContainer
import env.wiremock.WiremockOperator
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

        environment.operators.forEach { (_, s) -> assertTrue(s.running()) }
        assertEquals(5672, environment.find<EnvAwareRabbitContainer>("RABBIT").port())
        assertEquals("5672", System.getProperty("env.mq.rabbit.port"))
        assertEquals(PG_URL, environment.find<EnvAwarePostgreSqlContainer>("POSTGRES").jdbcUrl)
        assertEquals(PG_URL, System.getProperty("env.db.postgresql.url"))
    }

    @Test
    fun dynamicEnvironment() {
        environment = SomeEnvironment(false).apply { up() }

        environment.operators.forEach { (_, s) -> assertTrue(s.running()) }
        assertNotEquals(5672, environment.find<EnvAwareRabbitContainer>("RABBIT").port())
        assertNotEquals(PG_URL, environment.find<EnvAwarePostgreSqlContainer>("POSTGRES").jdbcUrl)
    }

    @After
    fun tearDown() {
        environment.down()
    }
}

class SomeEnvironment(fixed: Boolean) : Environment(
    mapOf(
        "RABBIT" to ContainerOperator(
            EnvAwareRabbitContainer(fixedEnv = fixed)
                .withLogConsumer(Slf4jLogConsumer(logger).withPrefix("RABBIT"))
        ),
        "REDIS" to ContainerOperator(EnvAwareRedisContainer(fixedEnv = fixed)),
        "POSTGRES" to ContainerOperator(EnvAwarePostgreSqlContainer(fixedEnv = fixed)),
        "MYSQL" to ContainerOperator(EnvAwareMySqlContainer(fixedEnv = fixed)),
        "GRPC" to ContainerOperator(GrpcMockContainer(1, fixed, listOf("common.proto", "wallet.proto"))),
        "WIREMOCK" to WiremockOperator(fixed)
    )
)
