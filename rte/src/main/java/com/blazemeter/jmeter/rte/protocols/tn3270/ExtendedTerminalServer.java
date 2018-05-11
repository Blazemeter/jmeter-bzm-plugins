package com.blazemeter.jmeter.rte.protocols.tn3270;

import com.blazemeter.jmeter.rte.core.ExceptionHandler;
import com.blazemeter.jmeter.rte.core.ssl.SSLSocketFactory;
import com.blazemeter.jmeter.rte.core.ssl.SSLType;
import com.bytezone.dm3270.streams.BufferListener;
import com.bytezone.dm3270.streams.TelnetSocket.Source;
import com.bytezone.dm3270.streams.TerminalServer;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;
import java.security.GeneralSecurityException;
import java.time.LocalDateTime;

/*
 * Performs the same as {@link TerminalServer}, but in this case uses a socket that supports SSL and
 * connection timeout. Apart from that, instead of handling the exceptions on the class itself, they
 * are thrown to Tn3270Client.
 */
public class ExtendedTerminalServer extends TerminalServer {

  private ExceptionHandler exceptionHandler;
  private int connectionTimeoutMillis;
  private SSLType sslType;

  private final int serverPort;
  private final String serverURL;
  private Socket socket = new Socket();
  private OutputStream serverOut;

  private final byte[] buffer = new byte[4096];
  private volatile boolean running;

  private final BufferListener telnetListener;

  public ExtendedTerminalServer(String serverURL, int serverPort, BufferListener listener,
      SSLType sslType, int connectionTimeoutMillis, ExceptionHandler exceptionHandler) {
    super(serverURL, serverPort, listener);
    this.serverPort = serverPort;
    this.sslType = sslType;
    this.serverURL = serverURL;
    this.connectionTimeoutMillis = connectionTimeoutMillis;
    this.telnetListener = listener;
    this.exceptionHandler = exceptionHandler;
  }

  @Override
  public void run() {
    try {
      socket = createSocket();
    } catch (GeneralSecurityException | IOException ex) {
      exceptionHandler.setPendingError(ex);
      return;
    }
    try {
      InputStream serverIn = socket.getInputStream();
      serverOut = socket.getOutputStream();
      running = true;
      while (running) {
        int bytesRead = serverIn.read(buffer);
        if (bytesRead < 0) {
          close();
          break;
        }

        byte[] message = new byte[bytesRead];
        System.arraycopy(buffer, 0, message, 0, bytesRead);
        telnetListener.listen(Source.SERVER, message, LocalDateTime.now(), true);
      }
    } catch (IOException ex) {
      if (running) {
        close();
        exceptionHandler.setPendingError(ex);
      }
    }
  }

  private Socket createSocket() throws IOException, GeneralSecurityException {
    if (sslType != null && sslType != SSLType.NONE) {
      SSLSocketFactory sslSocketFactory = new SSLSocketFactory(sslType);
      sslSocketFactory.init();
      return sslSocketFactory.createSocket(serverURL, serverPort, connectionTimeoutMillis);
    } else {
      Socket socket = new Socket();
      socket.connect(new InetSocketAddress(serverURL, serverPort), connectionTimeoutMillis);
      return socket;
    }
  }

  public synchronized void write(byte[] buffer) {
    if (!running) {
      // the no-op may come here if socket is closed from remote end and client has not been closed
      if (buffer != ExtendedTelnetState.NO_OP) {
        exceptionHandler.setPendingError(new SocketException("socketClosed "));
      }
      return;
    }

    try {
      serverOut.write(buffer);
      serverOut.flush();
    } catch (IOException e) {
      exceptionHandler.setPendingError(e);
    }
  }

  @Override
  public void close() {
    try {
      running = false;

      if (socket != null) {
        socket.close();
      }

      if (telnetListener != null) {
        telnetListener.close();
      }
    } catch (IOException ex) {
      exceptionHandler.setPendingError(ex);
    }
  }
}
