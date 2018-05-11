package com.blazemeter.jmeter.rte.protocols.tn5250;

import com.blazemeter.jmeter.rte.core.ssl.SSLSocketFactory;
import com.blazemeter.jmeter.rte.core.ssl.SSLType;
import com.blazemeter.jmeter.rte.protocols.ReflectionUtils;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;
import java.security.GeneralSecurityException;
import javax.net.ssl.SSLException;
import net.infordata.em.tnprot.XITelnet;

/**
 * Handle telnet connection by providing connection timeout and security on top of {@link
 * XITelnet}.
 */
public class ExtendedTelnet extends XITelnet {

  private static final Field SOCKET_FIELD = ReflectionUtils
      .getAccessibleField(XITelnet.class, "ivSocket");
  private static final Field INPUT_STREAM_FIELD = ReflectionUtils
      .getAccessibleField(XITelnet.class, "ivIn");
  private static final Field OUTPUT_STREAM_FIELD = ReflectionUtils
      .getAccessibleField(XITelnet.class, "ivOut");
  private static final Field USED_FIELD = ReflectionUtils
      .getAccessibleField(XITelnet.class, "ivUsed");
  private static final Field IAC_PARSER_STATUS_FIELD = ReflectionUtils
      .getAccessibleField(XITelnet.class, "ivIACParserStatus");
  private static final Method CLOSE_SOCKET_METHOD = ReflectionUtils
      .getAccessibleMethod(XITelnet.class, "closeSocket");
  private static final int READ_BUFFER_SIZE_BYTES = 1024;

  private final int connectTimeoutMillis;
  private final SSLType sslType;
  private RxThread readThread;

  public ExtendedTelnet(String aHost, int aPort, int connectTimeoutMillis, SSLType sslType) {
    super(aHost, aPort);
    this.connectTimeoutMillis = connectTimeoutMillis;
    this.sslType = sslType;
  }

  @Override
  public synchronized void connect() {
    if (getIvUsed()) {
      throw new IllegalStateException("XITelnet cannot be recycled");
    } else {
      this.disconnect();
      this.connecting();
      try {
        Socket ivSocket = createSocket();
        setIvSocket(ivSocket);
        InputStream ivIn = getIvSocket().getInputStream();
        setIvIn(ivIn);
        OutputStream ivOut = getIvSocket().getOutputStream();
        setIvOut(ivOut);
        readThread = new ExtendedTelnet.RxThread();
        readThread.start();
        setIvUsed(true);
        this.connected();
      } catch (IOException e) {
        catchedIOException(e);
      } catch (GeneralSecurityException e) {
        catchedIOException(new SSLException(e));
      }
    }
  }

  private Socket createSocket() throws IOException, GeneralSecurityException {
    if (sslType != null && sslType != SSLType.NONE) {
      SSLSocketFactory sslSocketFactory = new SSLSocketFactory(sslType);
      sslSocketFactory.init();
      /*
      In XITelnet is used ivFirstHost instead of getHost(), but we are not supposed to use hosts
      with firstHostIp#SecondHostIp format in JMeter
       */
      return sslSocketFactory
          .createSocket(this.getHost(), this.getPort(), connectTimeoutMillis);
    } else {
      Socket socket = new Socket();
      socket.connect(new InetSocketAddress(this.getHost(), this.getPort()), connectTimeoutMillis);
      return socket;
    }
  }

  /*
  It was required to use reflection on the following attributes as they are private in XITelnet
  class.
   */
  private Socket getIvSocket() {
    return ReflectionUtils.getFieldValue(SOCKET_FIELD, Socket.class, this);
  }

  private void setIvSocket(Socket ivSocket) {
    ReflectionUtils.setFieldValue(SOCKET_FIELD, ivSocket, this);
  }

  private InputStream getIvIn() {
    return ReflectionUtils.getFieldValue(INPUT_STREAM_FIELD, InputStream.class, this);
  }

  private void setIvIn(InputStream ivIn) {
    ReflectionUtils.setFieldValue(INPUT_STREAM_FIELD, ivIn, this);
  }

  private void setIvOut(OutputStream ivOut) {
    ReflectionUtils.setFieldValue(OUTPUT_STREAM_FIELD, ivOut, this);
  }

  private boolean getIvUsed() {
    return ReflectionUtils.getFieldValue(USED_FIELD, Boolean.class, this);
  }

  private void setIvUsed(boolean ivUsed) {
    ReflectionUtils.setFieldValue(USED_FIELD, ivUsed, this);
  }

  private int getivIACParserStatus() {
    return ReflectionUtils.getFieldValue(IAC_PARSER_STATUS_FIELD, Integer.class, this);
  }

  @Override
  public synchronized void sendEOR() throws IOException {
    checkIfAlreadyClosed();
    super.sendEOR();
  }

  private void checkIfAlreadyClosed() throws IOException {
    if (getIvSocket() == null) {
      throw new SocketException("Connection already closed");
    }
  }

  @Override
  protected synchronized int processIAC(byte bb) throws IOException {
    checkIfAlreadyClosed();
    return super.processIAC(bb);
  }

  @Override
  public synchronized void send(byte[] aBuf, int aLen) {
    try {
      checkIfAlreadyClosed();
      super.send(aBuf, aLen);
    } catch (IOException e) {
      catchedIOException(e);
    }
  }

  @Override
  public synchronized void disconnect() { //!!V 03/03/98
    if (readThread != null) {
      readThread.terminate();
      readThread = null;
    }
    closeIvSocket();
  }

  private void closeIvSocket() {
    ReflectionUtils.invokeMethod(CLOSE_SOCKET_METHOD, this);
  }

  //This class implements the Receptor Thread of the SSL Telnet connection.
  //It's a copy of XITelnet().RxThread() class.
  @SuppressWarnings("all")
  class RxThread extends Thread {

    private boolean ivTerminate = false;

    private RxThread() {
      super("ExtendedTelnet rx thread");
    }

    private void terminate() {
      this.ivTerminate = true;
      if (this != Thread.currentThread()) {
        this.interrupt();
      }
    }

    @Override
    public void run() {
      byte[] buf = new byte[READ_BUFFER_SIZE_BYTES];
      byte[] rBuf = new byte[READ_BUFFER_SIZE_BYTES];

      try {
        while (!this.ivTerminate) {
          InputStream input = getIvIn();
          // the input may be null if doDisconnect was invoked after the while condition evaluation
          if (input == null) {
            return;
          }
          int len = input.read(buf);

          int i = 0;
          int j;
          for (j = 0; i < len; ++i) {
            rBuf[j] = buf[i];
            if (getivIACParserStatus() == 0 && buf[i] != -1) {
              ++j;
            } else {
              if (getivIACParserStatus() == 0 && buf[i] == -1) {
                if (j > 0) {
                  ExtendedTelnet.this.receivedData(rBuf, j);
                }

                j = 0;
              }

              j += ExtendedTelnet.this.processIAC(buf[i]);
            }
          }

          if (j > 0) {
            ExtendedTelnet.this.receivedData(rBuf, j);
          }
        }
      } catch (IOException varviii) {
        if (!this.ivTerminate) {
          ExtendedTelnet.this.catchedIOException(varviii);
        }
      }

    }
  }
}
