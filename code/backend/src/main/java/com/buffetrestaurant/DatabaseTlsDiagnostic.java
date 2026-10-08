package com.buffetrestaurant;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

/** Temporary, password-free probe to distinguish Render network failures from JDBC failures. */
final class DatabaseTlsDiagnostic {
    private static final byte[] POSTGRES_SSL_REQUEST = {0, 0, 0, 8, 4, (byte) 0xd2, 0x16, 0x2f};

    private DatabaseTlsDiagnostic() {}

    static void runWhenEnabled() {
        if (!"true".equalsIgnoreCase(System.getenv("DB_TLS_DIAGNOSTIC"))) {
            return;
        }
        String host = System.getenv("SUPABASE_DB_HOST");
        if (host == null || host.isBlank()) {
            System.err.println("[db-tls] SUPABASE_DB_HOST is missing");
            return;
        }
        System.err.println("[db-tls] Java " + System.getProperty("java.version")
                + ", DNS " + Arrays.toString(resolve(host)));
        String portText = System.getenv().getOrDefault("SUPABASE_DB_PORT", "5432");
        try {
            probe(host, Integer.parseInt(portText));
        } catch (Exception e) {
            System.err.println("[db-tls] configured port: " + describe(e));
        }
        // Supabase's transaction-pooler listener is a diagnostic alternate route only.
        if (!"6543".equals(portText)) {
            try {
                probe(host, 6543);
            } catch (Exception e) {
                System.err.println("[db-tls] alternate port: " + describe(e));
            }
        }
    }

    private static InetAddress[] resolve(String host) {
        try {
            return InetAddress.getAllByName(host);
        } catch (Exception e) {
            System.err.println("[db-tls] DNS failed: " + describe(e));
            return new InetAddress[0];
        }
    }

    private static void probe(String host, int port) throws Exception {
        try (Socket plain = new Socket()) {
            plain.connect(new InetSocketAddress(host, port), 5000);
            plain.setSoTimeout(5000);
            System.err.println("[db-tls] port " + port + " connected to " + plain.getRemoteSocketAddress());
            plain.getOutputStream().write(POSTGRES_SSL_REQUEST);
            int reply = plain.getInputStream().read();
            System.err.println("[db-tls] port " + port + " SSLRequest reply: " + reply);
            if (reply != 'S') {
                return;
            }

            // The probe never sends credentials or queries. Certificate verification is disabled
            // here only to match the application's sslmode=require behavior during this test.
            X509TrustManager trust = new X509TrustManager() {
                public void checkClientTrusted(X509Certificate[] chain, String authType) {}
                public void checkServerTrusted(X509Certificate[] chain, String authType) {}
                public X509Certificate[] getAcceptedIssuers() { return new X509Certificate[0]; }
            };
            SSLContext context = SSLContext.getInstance("TLS");
            context.init(null, new TrustManager[] {trust}, new SecureRandom());
            try (SSLSocket tls = (SSLSocket) context.getSocketFactory()
                    .createSocket(plain, host, port, false)) {
                tls.setSoTimeout(5000);
                tls.startHandshake();
                System.err.println("[db-tls] port " + port + " handshake: "
                        + tls.getSession().getProtocol() + " / " + tls.getSession().getCipherSuite());
            }
        }
    }

    private static String describe(Exception e) {
        return e.getClass().getSimpleName() + ": " + e.getMessage();
    }
}
