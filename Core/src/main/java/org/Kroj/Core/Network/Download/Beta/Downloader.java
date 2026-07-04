package org.Kroj.Core.Network.Download.Beta;

import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http2.Http2HeadersFrame;
import io.netty.handler.codec.http3.Http3HeadersFrame;
import org.Kroj.Core.Tools.TestUnit.Tester;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.util.concurrent.atomic.AtomicReference;

import static org.Kroj.Core.Network.Download.Beta.Status.*;

public class Downloader {

    private AtomicReference<Protocol> protocol;
    private volatile Status status;


    private static final VarHandle STATUS;

    static {
        try {
            STATUS = MethodHandles.lookup()
                    .findVarHandle(Downloader.class, "status", Status.class);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public Downloader() throws NoSuchFieldException, IllegalAccessException {
        this.protocol = new AtomicReference<Protocol>(Protocol.H2);
    }

    private enum Protocol {
        H3,H2,H1_1
    }


    public void connect() {

    }

    public void sendGet() {

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
    }

    public void onContent(ByteBuf buf) {
        buf.release();
        System.out.println(protocol);
    }

    public void main(String[] args) throws NoSuchFieldException, IllegalAccessException {
        Downloader d = new Downloader();
        Tester.speedTest(100000,() -> {
//            d.STATUS.compareAndSet(Downloader.this,Idle,Connected);
//            d.STATUS.compareAndSet(Downloader.this,Connected,Idle);
        });
    }

}
