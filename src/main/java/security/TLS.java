package security;

import javax.net.ssl.*;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.*;
import java.security.cert.CertificateException;

public class TLS {

    public static SSLServerSocket createServerSocketSSL(final int port) throws NoSuchAlgorithmException, KeyStoreException, UnrecoverableKeyException, IOException, KeyManagementException, CertificateException {
        KeyStore ks = KeyStore.getInstance("JKS");
        ks.load(new FileInputStream("src/main/resources/group2.monitor.p12"), "group2".toCharArray());

        KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
        kmf.init(ks, "group2".toCharArray());

        TrustManagerFactory tmf = TrustManagerFactory.getInstance("SunX509");
        tmf.init(ks);

        SSLContext sc = SSLContext.getInstance("TLS");
        TrustManager[] trustManagers = tmf.getTrustManagers();
        sc.init(kmf.getKeyManagers(), trustManagers, null);

        SSLServerSocketFactory ssf = sc.getServerSocketFactory();

        return (SSLServerSocket) ssf.createServerSocket(port);
    }

    public static SSLSocket createClientSocketSSL(final String host, final int port) throws NoSuchAlgorithmException, KeyStoreException, UnrecoverableKeyException, IOException, KeyManagementException, CertificateException {
        final KeyStore ks = KeyStore.getInstance("JKS");
        ks.load(new FileInputStream("src/main/resources/group2.monitor.p12"), "group2".toCharArray());

        final KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
        kmf.init(ks, "group2".toCharArray());

        final TrustManagerFactory tmf = TrustManagerFactory.getInstance("SunX509");
        tmf.init(ks);

        final SSLContext sc = SSLContext.getInstance("TLS");
        final TrustManager[] trustManagers = tmf.getTrustManagers();
        sc.init(kmf.getKeyManagers(), trustManagers, null);

        final SSLSocketFactory ssf = sc.getSocketFactory();
        final SSLSocket socket = (SSLSocket) ssf.createSocket(host, port);
        socket.startHandshake();

        return socket;
    }
}