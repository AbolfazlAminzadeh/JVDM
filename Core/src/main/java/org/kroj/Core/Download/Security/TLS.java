package org.kroj.Core.Download.Security;

import io.netty.handler.codec.http3.Http3;
import io.netty.handler.codec.quic.Quic;
import io.netty.handler.codec.quic.QuicSslContext;
import io.netty.handler.codec.quic.QuicSslContextBuilder;
import io.netty.handler.ssl.*;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;

import javax.net.ssl.SSLException;

public class TLS {

    public static final SslContext ssl;
    public static final QuicSslContext quicSSL;

    static {
        try {

            ssl = SslContextBuilder.forClient()
                    .trustManager(InsecureTrustManagerFactory.INSTANCE)
                    .sslProvider(
                            // Required For Speed
                            OpenSsl.isAvailable() ? SslProvider.OPENSSL :
                            SslProvider.JDK
                    )
                    // Android Uncompatible

                    .protocols("TLSv1.3","TLSv1.2","TLSv1.1")
                    .applicationProtocolConfig(
                            new ApplicationProtocolConfig(
                            ApplicationProtocolConfig.Protocol.ALPN,
                            ApplicationProtocolConfig.SelectorFailureBehavior.NO_ADVERTISE,
                            ApplicationProtocolConfig.SelectedListenerFailureBehavior.ACCEPT,
                            ApplicationProtocolNames.HTTP_2,
                            ApplicationProtocolNames.HTTP_1_1
                    ))

                    .build();

            quicSSL = Quic.isAvailable() ? QuicSslContextBuilder.forClient()
                    .trustManager(InsecureTrustManagerFactory.INSTANCE)
                    .applicationProtocols(Http3.supportedApplicationProtocols())
                    .earlyData(true)
                    .build() : null;
        } catch (SSLException e) {
            throw new RuntimeException(e);
        }
    }


}
