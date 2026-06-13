package Network.Download.Part;

import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http2.HttpConversionUtil;

import java.net.URI;

public class Request {
    public static HttpRequest createRangeRequest(Part part) {
        URI uri = part.getUri();
        String reqTarget = uri.getRawQuery() != null
                ? uri.getRawPath() + "?" + uri.getRawQuery()
                : uri.getRawPath();
        HttpRequest req = new DefaultFullHttpRequest(
                HttpVersion.HTTP_1_1,
                HttpMethod.GET,
                reqTarget
        );

        req.headers().set(HttpConversionUtil.ExtensionHeaderNames.SCHEME.text(), "https");
        req.headers().set(HttpHeaderNames.HOST, uri.getHost());
        if (part.getEnd() >= 0) {
            req.headers().set(HttpHeaderNames.RANGE,
                    "bytes=" + part.getStart() + "-" + part.getEnd());
        }
        return req;
    }
}
