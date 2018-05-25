package specs;

import com.adven.concordion.extensions.exam.ExamExtension;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.concordion.api.AfterSuite;
import org.concordion.api.BeforeSuite;
import org.concordion.api.extension.Extension;
import org.concordion.api.option.ConcordionOptions;
import org.concordion.integration.junit4.ConcordionRunner;
import org.junit.runner.RunWith;
import org.simpleframework.http.core.ContainerServer;
import org.simpleframework.transport.Server;
import org.simpleframework.transport.connect.Connection;
import org.simpleframework.transport.connect.SocketConnection;
import org.springframework.kafka.test.rule.KafkaEmbedded;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;

import static org.concordion.internal.ConcordionBuilder.NAMESPACE_CONCORDION_2007;

@RunWith(ConcordionRunner.class)
@ConcordionOptions(declareNamespaces = {"c", NAMESPACE_CONCORDION_2007, "e", ExamExtension.NS})
public class Specs {

    protected static final String CONSUME_TOPIC = "test.consume.topic";
    protected static final String PRODUCE_TOPIC = "test.produce.topic";

    private static final int PORT = 8081;
    private static Server server;

    protected static final KafkaEmbedded kafka = new KafkaEmbedded(1, true, CONSUME_TOPIC);

    static {
        try {
            kafka.before();
        } catch (Exception e) {
            throw new RuntimeException("Failed to start kafka", e);
        }
    }

    @SuppressFBWarnings(value = "URF_UNREAD_FIELD", justification = "особенности подключения расширений в concordion")
    @Extension
    private final ExamExtension exam = new ExamExtension().
            rest().port(PORT).end().
            db().end().
            ui().headless().end().
            kafka().brokers(kafka.getBrokersAsString()).end();


    @AfterSuite
    public static void stopServer() throws Exception {
        if (server != null) {
            server.stop();
            server = null;
        }
        kafka.after();
    }

    @BeforeSuite
    public static void startServer() throws Exception {
        if (server == null) {
            server = startSrv();
        }
    }

    private static Server startSrv() throws IOException {
        if (server == null) {
            server = new ContainerServer(new TestContainer());
            Connection connection = new SocketConnection(server);
            SocketAddress address = new InetSocketAddress(PORT);
            connection.connect(address);
        }
        return server;
    }
}