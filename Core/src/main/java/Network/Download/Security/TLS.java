package Network.Download.Security;

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
                    .sslProvider(SslProvider.OPENSSL_REFCNT)
                    .protocols("TLSv1.3","TLSv1.2","TLSv1.1")
                    .applicationProtocolConfig(new ApplicationProtocolConfig(
                                                       ApplicationProtocolConfig.Protocol.ALPN,
                                                       ApplicationProtocolConfig.SelectorFailureBehavior.NO_ADVERTISE,
                                                       ApplicationProtocolConfig.SelectedListenerFailureBehavior.ACCEPT,
                                                       ApplicationProtocolNames.HTTP_2,
                                                       ApplicationProtocolNames.SPDY_3_1,
                                                       ApplicationProtocolNames.HTTP_1_1
                                                       ))
                    .build();
            quicSSL = QuicSslContextBuilder.forClient()
                    .trustManager(InsecureTrustManagerFactory.INSTANCE)
                    .earlyData(true)
                    .build();
        } catch (SSLException e) {
            throw new RuntimeException(e);
        }
    }


}
