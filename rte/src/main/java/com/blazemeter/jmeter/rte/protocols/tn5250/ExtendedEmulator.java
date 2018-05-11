package com.blazemeter.jmeter.rte.protocols.tn5250;

import com.blazemeter.jmeter.rte.core.ExceptionHandler;
import com.blazemeter.jmeter.rte.core.ssl.SSLType;
import com.blazemeter.jmeter.rte.protocols.ReflectionUtils;
import java.io.IOException;
import java.lang.reflect.Field;
import net.infordata.em.tn5250.XI5250Emulator;
import net.infordata.em.tnprot.XITelnet;
import net.infordata.em.tnprot.XITelnetEmulator;

/**
 * Allows configuring port and connection timeout to be used in {@link XI5250Emulator} connections,
 * capture exceptions to later on throw them to client code, handle SSL connections and avoids
 * leaving threads running when disconnected.
 */
public class ExtendedEmulator extends XI5250Emulator {

  private static final Field TELNET_FIELD = ReflectionUtils
      .getAccessibleField(XI5250Emulator.class, "ivTelnet");

  private int port;
  private int connectionTimeoutMillis;
  private SSLType sslType;
  private ExceptionHandler exceptionHandler;

  public ExtendedEmulator(ExceptionHandler exceptionHandler) {
    this.exceptionHandler = exceptionHandler;
    setKeyboardQueue(false);
  }

  // we overwrite this method to avoid any initialization of the thread which is never used
  protected void startKeybThread() {
  }

  public void setPort(int port) {
    this.port = port;
  }

  public void setConnectionTimeoutMillis(int connectionTimeoutMillis) {
    this.connectionTimeoutMillis = connectionTimeoutMillis;
  }

  public void setSslType(SSLType sslType) {
    this.sslType = sslType;
  }

  @Override
  public void setActive(boolean activate) {
    boolean wasActive;
    synchronized (this) {
      if (!activate) {
        setBlinkingCursor(false);
      }
      wasActive = isActive();
      if (activate == wasActive) {
        return;
      }
      if (activate) {
        XITelnet ivTelnet = new ExtendedTelnet(getHost(), port, connectionTimeoutMillis, sslType);
        setIvTelnet(ivTelnet);
        ivTelnet.setEmulator(new TelnetEmulator());
        ivTelnet.connect();
      } else {
        XITelnet ivTelnet = getIvTelnet();
        ivTelnet.disconnect();
        ivTelnet.setEmulator(null);
        setIvTelnet(null);
      }
    }
    firePropertyChange(ACTIVE, wasActive, isActive());
  }

  /*
  It was necessary to use reflection because XI520Emulator class
  has ivTelnet as a private attribute without set and get methods
  */
  private XITelnet getIvTelnet() {
    return ReflectionUtils.getFieldValue(TELNET_FIELD, XITelnet.class, this);
  }

  private void setIvTelnet(XITelnet ivTelnet) {
    ReflectionUtils.setFieldValue(TELNET_FIELD, ivTelnet, this);
  }

  @Override
  protected void catchedIOException(IOException ex) {
    exceptionHandler.setPendingError(ex);
  }

  @Override
  protected void catchedException(Throwable ex) {
    exceptionHandler.setPendingError(ex);
  }

  private class TelnetEmulator implements XITelnetEmulator {

    public final void connecting() {
      ExtendedEmulator.this.connecting();
    }

    public final void connected() {
      ExtendedEmulator.this.connected();
    }

    public final void disconnected() {
      ExtendedEmulator.this.disconnected();
    }

    public final void catchedIOException(IOException ex) {
      ExtendedEmulator.this.catchedIOException(ex);
    }

    public final void receivedData(byte[] buf, int len) {
      ExtendedEmulator.this.receivedData(buf, len);
    }

    public final void receivedEOR() {
      ExtendedEmulator.this.receivedEOR();
    }

    public final void unhandledRequest(byte aIACOpt, String aIACStr) {
      ExtendedEmulator.this.unhandledRequest(aIACOpt, aIACStr);
    }

    public final void localFlagsChanged(byte aIACOpt) {
      ExtendedEmulator.this.localFlagsChanged(aIACOpt);
    }

    public final void remoteFlagsChanged(byte aIACOpt) {
      ExtendedEmulator.this.remoteFlagsChanged(aIACOpt);
    }

  }

}
