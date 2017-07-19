package specs.exam.rs;

import com.jayway.restassured.RestAssured;
import org.concordion.api.AfterSuite;
import org.concordion.api.BeforeSuite;
import org.simpleframework.http.Cookie;
import org.simpleframework.http.Request;
import org.simpleframework.http.Response;
import org.simpleframework.http.core.Container;
import org.simpleframework.http.core.ContainerServer;
import org.simpleframework.transport.Server;
import org.simpleframework.transport.connect.Connection;
import org.simpleframework.transport.connect.SocketConnection;
import specs.exam.Exam;

import java.io.IOException;
import java.io.PrintStream;
import java.net.InetSocketAddress;
import java.net.SocketAddress;

import static org.simpleframework.http.Status.BAD_REQUEST;
import static org.simpleframework.http.Status.OK;

public class Rs extends Exam {

}