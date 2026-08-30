package org.Kroj.Core.Network.Download.Beta;

import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http2.Http2HeadersFrame;
import io.netty.handler.codec.http3.Http3HeadersFrame;
import org.Kroj.Core.Network.Download.Part.Part;
import org.Kroj.Core.Tools.URL.URL;

import java.net.URI;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

public class Downloader {

    private enum Protocol {
        H3,H2,H1_1
    }

    private final AtomicReference<Protocol> protocol = new AtomicReference<>(Protocol.H2);
    private final AtomicReference<Status> status = new AtomicReference<>(Status.Idle);
    public final AtomicLong last = new AtomicLong(-1);

    private final DownloadListener listener;
    private final Part part;

    public Downloader(Part part) {
        this.part = part;
        listener = new DownloadListener() {
            @Override
            public void onHeadersReceived() {

            }

            @Override
            public void onDownloadComplete() {

            }
        };
    } 


    public void onH1Headers(HttpResponse response) {
        this.protocol.set(Protocol.H1_1);
        var contentLength = response.headers().get(HttpHeaderNames.CONTENT_LENGTH);
        var CONTENT_DISPOSITION = response.headers().get(HttpHeaderNames.CONTENT_DISPOSITION);
        System.out.println(contentLength+" "+CONTENT_DISPOSITION);
    }

    public void onH2Headers(Http2HeadersFrame response) {
        this.protocol.set(Protocol.H2);
        var contentLength = response.headers().get(HttpHeaderNames.CONTENT_LENGTH);
        var CONTENT_DISPOSITION = response.headers().get(HttpHeaderNames.CONTENT_DISPOSITION);
        System.out.println(contentLength+" "+CONTENT_DISPOSITION);
    }

    public void onH3Headers(Http3HeadersFrame response) {
        this.protocol.set(Protocol.H3);
        var contentLength = response.headers().get(HttpHeaderNames.CONTENT_LENGTH);
        var CONTENT_DISPOSITION = response.headers().get(HttpHeaderNames.CONTENT_DISPOSITION);
        System.out.println(contentLength+" "+CONTENT_DISPOSITION);
        response.headers().forEach(System.out::println);
        listener.onHeadersReceived();

    }

    public void onContent(ByteBuf buf) {
        buf.release();
    }

    public void finish(ByteBuf buf) {
        onContent(buf);
        listener.onDownloadComplete();
    }

    public static void main(String[] args) throws NoSuchFieldException, IllegalAccessException {
        URI uri = URL.getSafeURI("https://dkstatics-public.digikala.com/digikala-products/af0103d79469c23779191b2d310192061bb2af40_1774359684.jpg?x-oss-process=image/resize,m_lfit,h_2400,w_2400/quality,q_100");
        Downloader d = new Downloader(new Part(uri,"eno1",0,-1));

    }

}
