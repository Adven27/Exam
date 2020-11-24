package env.grpc;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.BindMode;
import org.testcontainers.containers.FixedHostPortGenericContainer;
import org.testcontainers.containers.output.Slf4jLogConsumer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.utility.MountableFile;

import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static env.core.Environment.findAvailableTcpPort;

public class GrpcMockContainer extends FixedHostPortGenericContainer<GrpcMockContainer> {
    private static final Logger log = LoggerFactory.getLogger(GrpcMockContainer.class);
    private int serviceId;

    public GrpcMockContainer(final int serviceId, boolean fixedEnv, final List<String> protos) {
        this(serviceId, fixedEnv, null, protos);
    }

    public GrpcMockContainer(final int serviceId, final List<String> protos) {
        this(serviceId, false, null, protos);
    }

    public GrpcMockContainer(final int serviceId, boolean fixedEnv, String wiremock, final List<String> protos) {
        super("adven27/grpc-wiremock");
        this.serviceId = serviceId;
        this.withFixedExposedPort(findAndSetBasePort(fixedEnv) + serviceId, 50000)
            .waitingFor(Wait.forLogMessage(".*Started GrpcWiremock.*\\s", 1))
            .withStartupTimeout(Duration.ofSeconds(180))
            .withCreateContainerCmdModifier(cmd -> {
                String random = UUID.randomUUID().toString();
                cmd.withHostName("grpc-mock-" + serviceId + "-" + random);
                cmd.withName("grpc-mock-" + serviceId + "-" + random);
            })
            .withLogConsumer(new Slf4jLogConsumer(LoggerFactory.getLogger("SERVICE-ID-" + serviceId)));

        protos.forEach(p -> this.withCopyFileToContainer(MountableFile.forClasspathResource(p), "/proto/" + p));
        Optional.ofNullable(wiremock).ifPresent(w -> this.withClasspathResourceMapping(w, "/wiremock", BindMode.READ_WRITE));

        if (fixedEnv) {
            withFixedExposedPort(20000 + serviceId, 8888);
        }
    }

    @Override
    public void start() {
        super.start();
        log.info("{} started on port {}; mock API port {}", getDockerImageName(), getMappedPort(50000), getMappedPort(8888));
    }

    private static int findAndSetBasePort(boolean fixedEnv) {
        return fixedEnv ? 10000 : findAvailableTcpPort();
    }

    public int mockPort() {
        return isRunning() ? getMappedPort(8888) : 20000 + serviceId;
    }

    public int port() {
        return isRunning() ? getMappedPort(50000) : 10000 + serviceId;
    }
}