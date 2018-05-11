package com.blazemeter.jmeter.rte.core.ssl;

import static org.apache.jmeter.util.SSLManager.JAVAX_NET_SSL_KEY_STORE;

import com.helger.commons.annotation.VisibleForTesting;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.TrustManagerFactory;

public class SSLSocketFactory {

  private static final String KEY_STORE_PASSWORD_PROPERTY = "javax.net.ssl.keyStorePassword";

  private final SSLType sslType;
  private final char[] ksPwd;
  private SSLContext sslctx;
  private String keyStorePath;

  public SSLSocketFactory(SSLType sslType) {
    this.sslType = sslType;
    this.ksPwd = System.getProperty(KEY_STORE_PASSWORD_PROPERTY).toCharArray();
    this.keyStorePath = System.getProperty(JAVAX_NET_SSL_KEY_STORE);
  }

  @VisibleForTesting
  public static void setKeyStore(String keyStore) {
    System.setProperty(JAVAX_NET_SSL_KEY_STORE, keyStore);
  }

  @VisibleForTesting
  public static void setKeyStorePassword(String keyStorePassword) {
    System.setProperty(KEY_STORE_PASSWORD_PROPERTY, keyStorePassword);
  }

  public void init() throws GeneralSecurityException, IOException {
    KeyStore keystore = buildKeyStore();
    KeyManagerFactory keymf = buildKeyManagerFactory(keystore);
    KeyStore trustedStore = buildTrustedStore();
    TrustManagerFactory trustmf = buildTrustManagerFactory(trustedStore);
    buildSSLContext(sslType, keymf, trustmf);
  }

  private KeyStore buildKeyStore() throws GeneralSecurityException, IOException {
    KeyStore keystore = KeyStore.getInstance(KeyStore.getDefaultType());
    File ksFile = new File(keyStorePath);
    keystore.load(ksFile.exists() ? new FileInputStream(ksFile) : null, ksPwd);
    return keystore;
  }

  private KeyManagerFactory buildKeyManagerFactory(KeyStore keystore)
      throws GeneralSecurityException {
    KeyManagerFactory keymf =
        KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
    keymf.init(keystore, ksPwd);
    return keymf;
  }

  private KeyStore buildTrustedStore() throws GeneralSecurityException, IOException {
    File tsFile = new File(keyStorePath); //Theoretically it's ok to use the same file
    // for keystore and truststore
    KeyStore trustedStore = KeyStore.getInstance(KeyStore.getDefaultType());
    trustedStore.load(new FileInputStream(tsFile), ksPwd);
    return trustedStore;
  }

  private TrustManagerFactory buildTrustManagerFactory(KeyStore trustedStore)
      throws GeneralSecurityException {
    TrustManagerFactory trustmf =
        TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
    trustmf.init(trustedStore);
    return trustmf;
  }

  private void buildSSLContext(SSLType sslType, KeyManagerFactory keymf,
      TrustManagerFactory trustmf) throws GeneralSecurityException {
    sslctx = SSLContext.getInstance(sslType.toString());
    sslctx.init(keymf.getKeyManagers(), trustmf.getTrustManagers(), null);
  }

  public Socket createSocket(String host, int port, int timeoutMillis) throws IOException {
    SSLSocket socket = (SSLSocket) sslctx.getSocketFactory().createSocket();
    socket.connect(new InetSocketAddress(host, port), timeoutMillis);
    socket.startHandshake();
    return socket;
  }

  public ServerSocket createServerSocket(int port) throws IOException {
    return sslctx.getServerSocketFactory().createServerSocket(port);
  }

}
