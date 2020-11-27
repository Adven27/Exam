import env.container.ContainerOperator
import env.core.Environment
import env.core.Environment.Prop
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
    private lateinit var sut: Environment

    @Test
    fun fixedEnvironment() {
        System.setProperty("SPECS_FIXED_ENV", "true")

        sut = SomeEnvironment().apply { up() }

        sut.operators.forEach { (_, s) -> assertTrue(s.running()) }
        assertEquals(Prop("env.mq.rabbit.port", "5672"), sut.find<EnvAwareRabbitContainer>("RABBIT").config().port)
        assertEquals("5672", System.getProperty("env.mq.rabbit.port"))
        assertEquals(
            Prop("env.db.postgresql.url", PG_URL),
            sut.find<EnvAwarePostgreSqlContainer>("POSTGRES").config().jdbcUrl
        )
        assertEquals(PG_URL, System.getProperty("env.db.postgresql.url"))
    }

    @Test
    fun dynamicEnvironment() {
        System.setProperty("SPECS_FIXED_ENV", "false")

        sut = SomeEnvironment().apply { up() }

        sut.operators.forEach { (_, s) -> assertTrue(s.running()) }
        assertNotEquals("5672", sut.find<EnvAwareRabbitContainer>("RABBIT").config().port.value)
        assertNotEquals(PG_URL, sut.find<EnvAwarePostgreSqlContainer>("POSTGRES").config().jdbcUrl.value)
    }

    @After
    fun tearDown() {
        sut.down()
    }
}

class SomeEnvironment : Environment(
    mapOf(
        "RABBIT" to ContainerOperator(
            EnvAwareRabbitContainer(fixedEnv = fixedEnv)
                .withLogConsumer(Slf4jLogConsumer(logger).withPrefix("RABBIT"))
        ),
        "REDIS" to ContainerOperator(EnvAwareRedisContainer(fixedEnv = fixedEnv)),
        "POSTGRES" to ContainerOperator(EnvAwarePostgreSqlContainer(fixedEnv = fixedEnv)),
        "MYSQL" to ContainerOperator(EnvAwareMySqlContainer(fixedEnv = fixedEnv)),
        "GRPC" to ContainerOperator(GrpcMockContainer(1, fixedEnv, listOf("common.proto", "wallet.proto"))),
        "WIREMOCK" to WiremockOperator(fixedEnv)
    )
) {
    companion object {
        val fixedEnv
            get() = "SPECS_FIXED_ENV".fromPropertyOrElse(false)
    }
}
