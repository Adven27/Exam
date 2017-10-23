package specs;

import org.simpleframework.http.Cookie;
import org.simpleframework.http.Request;
import org.simpleframework.http.Response;
import org.simpleframework.http.core.Container;

import java.io.IOException;
import java.io.PrintStream;

import static com.jayway.restassured.http.Method.POST;
import static com.jayway.restassured.http.Method.PUT;
import static java.lang.String.format;
import static org.simpleframework.http.Status.BAD_REQUEST;
import static org.simpleframework.http.Status.OK;

class TestContainer implements Container {
    @Override
    public void handle(Request req, Response resp) {
        try {
            for (Cookie cookie : req.getCookies()) {
                resp.setCookie(cookie);
            }
            resp.setStatus("/status/400".equals(req.getAddress().toString()) ? BAD_REQUEST : OK);
            PrintStream body = resp.getPrintStream();
            if ("/ui".equals(req.getAddress().toString())) {
                uiPage(body);
            } else {
                body.println(createBody(cookies(req), getContent(req)));
            }
            body.close();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void uiPage(PrintStream body) {
        body.println("<html><head></head><body><span>Dummy page</span></body></html>");
    }

    private String createBody(String cookies, String content) {
        return "{" + ("".equals(content) ? "" : content.substring(1, content.length() - 1))
                + (!"".equals(content) && !"".equals(cookies) ? ", " : "")
                + cookies
                + "}";
    }

    private String getContent(Request req) throws IOException {
        return isRequestWithBody(req) ? req.getContent().trim()
                                      : format("{\"%s\":\"%s\"}", req.getMethod().toLowerCase(), req.getAddress());
    }

    private boolean isRequestWithBody(Request req) {
        String method = req.getMethod();
        return POST.name().equals(method) || PUT.name().equals(method);
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