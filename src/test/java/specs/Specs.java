package specs;

import com.adven.concordion.extensions.exam.ExamExtension;
import com.adven.concordion.extensions.exam.db.kv.repositories.InMemoryRepository;
import com.adven.concordion.extensions.exam.mq.MqTester;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.extension.responsetemplating.ResponseTemplateTransformer;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import kotlin.Pair;
import kotlin.collections.MapsKt;
import org.concordion.api.AfterSuite;
import org.concordion.api.BeforeSuite;
import org.concordion.api.extension.Extension;
import org.concordion.api.option.ConcordionOptions;
import org.concordion.integration.junit4.ConcordionRunner;
import org.jetbrains.annotations.NotNull;
import org.junit.runner.RunWith;
import org.springframework.kafka.test.rule.KafkaEmbedded;

import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.concordion.internal.ConcordionBuilder.NAMESPACE_CONCORDION_2007;

@RunWith(ConcordionRunner.class)
@ConcordionOptions(declareNamespaces = {"c", NAMESPACE_CONCORDION_2007, "e", ExamExtension.NS})
public class Specs {
    protected static final String CONSUME_TOPIC = "test.consume.topic";
    protected static final String PRODUCE_TOPIC = "test.produce.topic";
    protected static final KafkaEmbedded kafka = new KafkaEmbedded(1, true, CONSUME_TOPIC);
    @SuppressFBWarnings(value = "MS_MUTABLE_COLLECTION", justification = "коллекция для тестов должна быть мутабельной")
    protected static final Map<String, Map<String, Object>> inMemoryKeyValueDb = new HashMap<>();
    private static final int PORT = 8888;
    private static WireMockServer server = new WireMockServer(wireMockConfig()
            .extensions(new ResponseTemplateTransformer(true)).port(PORT));

    static {
        try {
            kafka.before();
        } catch (Exception e) {
            throw new RuntimeException("Failed to start kafka", e);
        }
    }

    @SuppressFBWarnings(value = "URF_UNREAD_FIELD", justification = "особенности подключения расширений в concordion")
    @Extension
    private final ExamExtension exam = new ExamExtension()
            .rest().port(PORT).end()
            .db().end()
            .ui().headless().end()
            .mq(MapsKt.mapOf(new Pair<String, MqTester>("myQueue", new MqTester() {
                Stack<String> queue = new Stack<>();

                @Override
                public void start() {
                }

                @Override
                public void stop() {
                }

                @Override
                public void send(@NotNull String message) {
                    queue.add(message);
                }

                @NotNull
                @Override
                public String recieve() {
                    return queue.pop();
                }

                @Override
                public void purge() {

                }
            })))
            .kafka().brokers(kafka.getBrokersAsString()).end()
            .keyValueDB(new InMemoryRepository(inMemoryKeyValueDb));

    @AfterSuite
    public static void stopServer() {
        if (server != null) {
            server.stop();
            server = null;
        }
        kafka.after();
    }

    @BeforeSuite
    public static void startServer() {
        if (server != null) {
            String req = "{{{request.body}}}";
            String cookie = "{\"cookies\":\"{{{request.cookies}}}\"}";
            String method = "{\"{{{request.requestLine.method}}}\":\"{{{request.url}}}\"}";

            server.stubFor(post(anyUrl()).atPriority(1).withHeader("Content-type", equalTo("application/soap+xml; charset=UTF-8;"))
                    .willReturn(aResponse().withBody(req)));

            server.stubFor(get(urlPathEqualTo("/ui")).atPriority(1)
                    .willReturn(aResponse().withBody("<html><head></head><body><span>Dummy page</span></body></html>")));

            server.stubFor(post(urlPathEqualTo("/status/400")).atPriority(1).willReturn(aResponse().withBody(req).withStatus(400)));
            server.stubFor(put(urlPathEqualTo("/status/400")).atPriority(1).willReturn(aResponse().withBody(req).withStatus(400)));
            server.stubFor(any(urlPathEqualTo("/status/400")).atPriority(2).willReturn(aResponse().withBody(method).withStatus(400)));

            server.stubFor(post(anyUrl()).withCookie("cook", matching(".*")).atPriority(3).willReturn(aResponse().withBody(cookie)));
            server.stubFor(put(anyUrl()).withCookie("cook", matching(".*")).atPriority(3).willReturn(aResponse().withBody(cookie)));
            server.stubFor(post(anyUrl()).atPriority(4).willReturn(aResponse().withBody(req)));
            server.stubFor(put(anyUrl()).atPriority(4).willReturn(aResponse().withBody(req)));

            server.stubFor(any(anyUrl()).withCookie("cook", matching(".*")).atPriority(5)
                    .willReturn(aResponse()
                            .withHeader("my_header", "some value")
                            .withBody("{\"{{{request.requestLine.method}}}\":\"{{{request.url}}}\", \"cookies\":\"{{{request.cookies}}}\"}")));
            server.stubFor(any(anyUrl()).atPriority(6).willReturn(aResponse().withBody(method)));
            server.start();
        }
    }
}