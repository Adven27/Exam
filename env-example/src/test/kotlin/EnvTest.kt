import env.container.ContainerizedSystem
import env.core.Environment
import env.db.mysql.SpecAwareMySqlContainer
import env.db.postgresql.SpecAwarePostgreSqlContainer
import env.grpc.GrpcMockContainer
import env.mq.rabbit.SpecAwareRabbitContainer
import env.mq.redis.SpecAwareRedisContainer
import env.wiremock.WiremockSystem
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Ignore
import org.junit.Test
import org.testcontainers.containers.output.Slf4jLogConsumer

private const val PG_URL = "jdbc:postgresql://localhost:5432/test?loggerLevel=OFF"

@Ignore
class EnvTest {
    private val environment = Environment(
        mapOf(
            "RABBIT" to ContainerizedSystem(
                SpecAwareRabbitContainer()
                    .withLogConsumer(Slf4jLogConsumer(SpecAwareRabbitContainer.logger).withPrefix("RABBIT"))
            ),
            "RABBIT_FIXED" to ContainerizedSystem(
                SpecAwareRabbitContainer(fixedEnv = true, portSystemPropertyName = "fixed.rabbit.port")
            ),
            "REDIS" to ContainerizedSystem(SpecAwareRedisContainer()),
            "POSTGRES" to ContainerizedSystem(SpecAwarePostgreSqlContainer()),
            "POSTGRES_FIXED" to ContainerizedSystem(
                SpecAwarePostgreSqlContainer(fixedEnv = true, urlSystemPropertyName = "fixed.postgres.url")
            ),
            "MYSQL" to ContainerizedSystem(SpecAwareMySqlContainer()),
            "GRPC" to ContainerizedSystem(GrpcMockContainer(1, listOf("common.proto", "wallet.proto"))),
            "WIREMOCK" to WiremockSystem()
        )
    )

    @Test
    fun test() {
        environment.up()
        environment.systems.forEach { (_, s) -> assertTrue(s.running()) }

        assertNotEquals(5672, environment.find<SpecAwareRabbitContainer>("RABBIT").port())
        assertEquals(5672, environment.find<SpecAwareRabbitContainer>("RABBIT_FIXED").port())
        assertEquals("5672", System.getProperty("fixed.rabbit.port"))

        assertNotEquals(PG_URL, environment.find<SpecAwarePostgreSqlContainer>("POSTGRES").jdbcUrl)
        assertEquals(PG_URL, environment.find<SpecAwarePostgreSqlContainer>("POSTGRES_FIXED").jdbcUrl)
        assertEquals(PG_URL, System.getProperty("fixed.postgres.url"))

        environment.down()
    }
}
