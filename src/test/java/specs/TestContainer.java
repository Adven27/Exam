package specs;

import org.simpleframework.http.Cookie;
import org.simpleframework.http.Request;
import org.simpleframework.http.Response;
import org.simpleframework.http.core.Container;

import java.io.IOException;
import java.io.PrintStream;

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
            if ("POST".equals(req.getMethod())) {
                restPOST(req, body);
            } else if ("GET".equals(req.getMethod())) {
                if ("/ui".equals(req.getAddress().toString())) {
                    uiPage(body);
                } else {
                    restGET(req, body);
                }
            }
            body.close();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void restPOST(Request req, PrintStream body) throws IOException {
        String content = req.getContent().trim();
        body.println(mirrorRequestBodyAndAddCookiesIfPresent(req, content));
    }

    private void uiPage(PrintStream body) {
        body.println("<html><head></head><body><span>Dummy page</span></body></html>");
    }

    private void restGET(Request req, PrintStream body) {
        String cookies = cookies(req);
        body.println("{\"get\":\"" + req.getAddress().toString() + "\"" +
                ("".equals(cookies) ? "" : ", " + cookies) + "}");
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