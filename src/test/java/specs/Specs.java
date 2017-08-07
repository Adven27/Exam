package specs;

import com.jayway.restassured.RestAssured;
import com.sberbank.pfm.test.concordion.extensions.exam.ExamExtension;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.concordion.api.AfterSuite;
import org.concordion.api.BeforeSuite;
import org.concordion.api.extension.Extension;
import org.concordion.api.option.ConcordionOptions;
import org.concordion.integration.junit4.ConcordionRunner;
import org.junit.runner.RunWith;
import org.simpleframework.http.Cookie;
import org.simpleframework.http.Request;
import org.simpleframework.http.Response;
import org.simpleframework.http.core.Container;
import org.simpleframework.http.core.ContainerServer;
import org.simpleframework.transport.Server;
import org.simpleframework.transport.connect.Connection;
import org.simpleframework.transport.connect.SocketConnection;

import java.io.IOException;
import java.io.PrintStream;
import java.net.InetSocketAddress;
import java.net.SocketAddress;

import static org.simpleframework.http.Status.BAD_REQUEST;
import static org.simpleframework.http.Status.OK;

@RunWith(ConcordionRunner.class)
@ConcordionOptions(declareNamespaces = {"c", "http://www.concordion.org/2007/concordion", "e", ExamExtension.NS})
public class Specs {
    private static Server server;
    @SuppressFBWarnings(value = "URF_UNREAD_FIELD", justification = "особенности подключения расширений в concordion")
    @Extension
    private final ExamExtension exam = new ExamExtension().
            dbTester("org.h2.Driver", "jdbc:h2:mem:test;INIT=CREATE SCHEMA IF NOT EXISTS SA\\;SET SCHEMA SA", "sa", "");

    @BeforeSuite
    public static void startServer() throws Exception {
        if (server == null) {
            server = startServer(8081);
        }
    }

    @AfterSuite
    public static void stopServer() throws Exception {
        if (server != null) {
            server.stop();
            server = null;
        }
    }

    private static Server startServer(int port) throws IOException {
        if (server == null) {
            RestAssured.baseURI = "http://localhost";
            RestAssured.port = port;
            RestAssured.basePath = "/";
            server = new ContainerServer(new TestContainer());
            Connection connection = new SocketConnection(server);
            SocketAddress address = new InetSocketAddress(port);
            connection.connect(address);
        }
        return server;
    }

    private static class TestContainer implements Container {
        @Override
        public void handle(Request req, Response resp) {
            try {
                for (Cookie cookie : req.getCookies()) {
                    resp.setCookie(cookie);
                }
                resp.setStatus("/status/400".equals(req.getAddress().toString()) ? BAD_REQUEST : OK);
                PrintStream body = resp.getPrintStream();
                if ("POST".equals(req.getMethod())) {
                    String content = req.getContent().trim();
                    body.println(mirrorRequestBodyAndAddCookiesIfPresent(req, content));
                } else if ("GET".equals(req.getMethod())) {
                    String cookies = cookies(req);
                    body.println("{\"get\":\"" + req.getAddress().toString() + "\"" +
                            ("".equals(cookies) ? "" : ", " + cookies) + "}");
                }
                body.close();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        private String mirrorRequestBodyAndAddCookiesIfPresent(Request req, String content) {
            return ("".equals(content) ? "{" : content.substring(0, content.length() - 1)) + cookies(req) + "}";
        }

        private String cookies(Request req) {
            return req.getCookies().isEmpty() ? "" : "\"cookies\":{ " + cookiesToStr(req) + "}";
        }

        private String cookiesToStr(Request req) {
            StringBuffer sb = new StringBuffer("");
            for (Cookie c : req.getCookies()) {
                sb.append(",\"" + c.getName() + "\":\"" + c.getValue() + "\"");
            }
            return sb.toString().substring(1);
        }
    }
}