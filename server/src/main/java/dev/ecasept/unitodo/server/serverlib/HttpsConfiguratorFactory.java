package dev.ecasept.unitodo.server.serverlib;

import com.sun.net.httpserver.HttpsConfigurator;
import com.sun.net.httpserver.HttpsParameters;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLParameters;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.*;
import java.security.cert.CertificateException;

public class HttpsConfiguratorFactory {
    /** Creates an HttpsConfigurator using the specified keystore password and location. */
    public static HttpsConfigurator create(String keystorePassword, String keystoreLocation) {
        char[] pw = keystorePassword.toCharArray();
        KeyStore ks;
        try {
            ks = KeyStore.getInstance("JKS");
        } catch (KeyStoreException e) {
            throw new RuntimeException("Error initializing keystore: " + keystoreLocation, e);
        }
        try (FileInputStream fis = new FileInputStream(keystoreLocation)) {
            ks.load(fis, pw);
        } catch (FileNotFoundException e) {
            throw new IllegalArgumentException("Keystore file not found: " + keystoreLocation, e);
        } catch (IOException e) {
            throw new RuntimeException("Error reading keystore file: " + keystoreLocation, e);
        } catch (NoSuchAlgorithmException | CertificateException e) {
            throw new RuntimeException("Error loading keystore: " + keystoreLocation, e);
        }

        KeyManagerFactory kmf;
        try {
            kmf = KeyManagerFactory.getInstance("SunX509");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Error initializing KeyManagerFactory", e);
        }
        try {
            kmf.init(ks, pw);
        } catch (UnrecoverableKeyException | KeyStoreException | NoSuchAlgorithmException e) {
            throw new RuntimeException("Error initializing KeyManagerFactory with keystore: " + keystoreLocation, e);
        }

        SSLContext sslContext;
        try {
            sslContext = SSLContext.getInstance("TLS");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Error initializing SSLContext", e);
        }
        try {
            sslContext.init(kmf.getKeyManagers(), null, null);
        } catch (KeyManagementException e) {
            throw new RuntimeException("Error initializing SSLContext with KeyManagerFactory", e);
        }


        return new HttpsConfigurator(sslContext) {
            public void configure(HttpsParameters params) {
                SSLParameters sslParams = sslContext.getDefaultSSLParameters();
                params.setSSLParameters(sslParams);
            }
        };
    }
}
