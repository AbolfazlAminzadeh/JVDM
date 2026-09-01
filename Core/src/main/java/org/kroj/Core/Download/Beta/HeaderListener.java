package org.kroj.Core.Download.Beta;

import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http2.Http2Headers;
import io.netty.handler.codec.http3.Http3Headers;

public interface HeaderListener {
    default void onH1Headers(HttpHeaders headers) {
        throw new UnsupportedHttpVersion("A Classed Called HttpHeadars (HTTP 1) without defining listener for it");
    }
    default void onH2Headers(Http2Headers headers) {
        throw new UnsupportedHttpVersion("A Classed Called Http2Headars without defining listener for it");
    }
    default void onH3Headers(Http3Headers headers) throws Exception {
        throw new UnsupportedHttpVersion("A Classed Called Http3Headars without defining listener for it");
    }
}
