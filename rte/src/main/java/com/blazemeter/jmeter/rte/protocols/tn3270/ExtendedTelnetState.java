package com.blazemeter.jmeter.rte.protocols.tn3270;

import com.bytezone.dm3270.display.ScreenDimensions;
import com.bytezone.dm3270.streams.TelnetState;
import com.bytezone.dm3270.telnet.TN3270ExtendedSubcommand.Function;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/*
 * An extension of {@link TelnetState} that performs the same but supports ExtendedTerminalServer,
 * also exceptions are properly handled and standard outputs are replaced with log framework
 */
public class ExtendedTelnetState extends TelnetState {

  public static final byte[] NO_OP = {(byte) 0xFF, (byte) 0xF1};

  private static final Logger LOG = LoggerFactory.getLogger(ExtendedTelnetState.class);

  private static final String[] TERMINAL_TYPES = {"IBM-3278-2-E", "IBM-3278-3-E", "IBM-3278-4-E",
      "IBM-3278-5-E"};

  // preferences
  private boolean do3270Extended;
  private boolean doBinary;
  private boolean doEOR;
  private boolean doTerminalType;
  private String doDeviceType;

  // current status
  private boolean does3270Extended;
  private boolean doesEOR;
  private boolean doesBinary;
  private boolean doesTerminalType;
  private String deviceType = "";
  private List<Function> functions;

  private String terminal = "";
  private ExtendedTerminalServer terminalServer;

  // IO
  private final AtomicLong lastAccess;
  private volatile boolean running = false;
  private Thread thread;

  private final ScreenDimensions primary = new ScreenDimensions(24, 80);
  private ScreenDimensions secondary = new ScreenDimensions(24, 80);

  private int totalReads;
  private int totalWrites;
  private int totalBytesRead;
  private int totalBytesWritten;

  public ExtendedTelnetState() {
    setDo3270Extended(true);       // prefer extended
    setDoDeviceType(2);

    setDoEOR(true);
    setDoBinary(true);
    setDoTerminalType(true);
    lastAccess = new AtomicLong(System.currentTimeMillis());
  }

  public void setTerminalServer(ExtendedTerminalServer terminalServer) {
    this.terminalServer = terminalServer;
    thread = new Thread(this);
    thread.start();
  }

  @Override
  public void setLastAccess(LocalDateTime dateTime, int bytes) {
    lastAccess.set(System.currentTimeMillis());
    ++totalReads;
    totalBytesRead += bytes;
  }

  @Override
  public void write(byte[] buffer) {
    if (terminalServer != null) {
      terminalServer.write(buffer);
    }

    lastAccess.set(System.currentTimeMillis());

    ++totalWrites;
    totalBytesWritten += buffer.length;
  }
  // communicated with the server.
  // certain period, and when it wakes it issues a NOOP if nothing else has
  // This thread exists simply to keep the connection alive. It sleeps for a

  @Override
  public void run() {
    long lastTimeIChecked;
    running = true;
    long limit = 120;      // seconds to wait

    while (running) {
      try {
        lastTimeIChecked = lastAccess.get();
        long delay = (System.currentTimeMillis() - lastTimeIChecked) / 1000;
        long sleep = limit - delay;

        if (sleep > 1) {
          Thread.sleep(sleep * 1000);
        }

        if (lastTimeIChecked == lastAccess.get()) {
          write(NO_OP);
        }
      } catch (InterruptedException e) {
        if (running) {
          LOG.debug("TelnetState was interrupted.");
        }
        close();
        return;
      }
    }
  }

  public void close() {
    if (thread != null) {
      running = false;
      thread.interrupt();
    }
  }

  public ScreenDimensions getPrimary() {
    return primary;
  }

  public ScreenDimensions getSecondary() {
    return secondary;
  }

  public String getSummary() {
    if (totalReads == 0 || totalWrites == 0) {
      return "Nothing to report";
    }

    int averageReads = totalBytesRead / totalReads;
    int averageWrites = totalBytesWritten / totalWrites;
    int totalIOBytes = totalBytesRead + totalBytesWritten;
    int totalIO = totalReads + totalWrites;
    int averageIO = totalIOBytes / totalIO;

    return String.format("          Total        Bytes    Average%n")
        + String.format("         -------   ----------   -------%n")
        + String
        .format("Reads     %,5d       %,7d     %,4d %n", totalReads, totalBytesRead, averageReads)
        + String.format("Writes    %,5d       %,7d     %,4d %n", totalWrites, totalBytesWritten,
        averageWrites)
        + String.format("         -------   ----------   -------%n")
        + String.format("          %,5d       %,7d     %,4d %n", totalIO,
        totalIOBytes, averageIO);
  }
  // ---------------------------------------------------------------------------------//
  // Set actual (what was communicated during negotiations)
  // ---------------------------------------------------------------------------------//

  public void setDoes3270Extended(boolean state) {
    LOG.debug("Does Extended: {}", state);
    does3270Extended = state;
  }

  public void setDoesEOR(boolean state) {
    LOG.debug("Does EOR: {}", state);
    doesEOR = state;
  }

  public void setDoesBinary(boolean state) {
    LOG.debug("Does Binary: {}", state);
    doesBinary = state;
  }

  public void setDoesTerminalType(boolean state) {
    LOG.debug("Does Terminal type: {}", state);
    doesTerminalType = state;
  }

  public void setTerminal(String terminal) {
    LOG.debug("Terminal: {}", terminal);
    this.terminal = terminal;
  }
  // called from TN3270ExtendedSubcommand.process()

  @Override
  public void setDeviceType(String deviceType) {
    LOG.debug("Device Type: {}", deviceType);
    this.deviceType = deviceType;
    int modelNo = 0;
    for (int i = 0; i <= 3; i++) {
      if (TERMINAL_TYPES[i].equals(deviceType)) {
        modelNo = i + 2;
        break;
      }
    }

    switch (modelNo) {
      case 0:
        secondary = new ScreenDimensions(24, 80);
        break;
      case 1:
        secondary = new ScreenDimensions(32, 80);
        break;
      case 2:
        secondary = new ScreenDimensions(43, 80);
        break;
      case 3:
        secondary = new ScreenDimensions(27, 132);
        break;
      default:
        secondary = new ScreenDimensions(24, 80);
        LOG.debug("Model not found: {}", deviceType);
    }
  }
  // called from TN3270ExtendedSubcommand.process()

  public void setFunctions(List<Function> functions) {
    LOG.debug("Functions: {}", functions);
    this.functions = functions;
  }
  // ---------------------------------------------------------------------------------//
  // Ask actual
  // ---------------------------------------------------------------------------------//

  public boolean does3270Extended() {
    return does3270Extended;
  }

  public boolean doesEOR() {
    return doesEOR || does3270Extended;
  }

  public boolean doesBinary() {
    return doesBinary || does3270Extended;
  }

  public boolean doesTerminalType() {
    return doesTerminalType || does3270Extended;
  }

  public String getTerminal() {
    return terminal;
  }
  // ---------------------------------------------------------------------------------//
  // Ask preferences
  // ---------------------------------------------------------------------------------//

  public boolean do3270Extended() {
    return do3270Extended;
  }

  public boolean doEOR() {
    return doEOR;
  }

  public boolean doBinary() {
    return doBinary;
  }

  public boolean doTerminalType() {
    return doTerminalType;
  }

  public String doDeviceType() {
    return doDeviceType;
  }
  // ---------------------------------------------------------------------------------//
  // Set preferences
  // ---------------------------------------------------------------------------------//

  public void setDo3270Extended(boolean state) {
    do3270Extended = state;
  }

  public void setDoBinary(boolean state) {
    doBinary = state;
  }

  public void setDoEOR(boolean state) {
    doEOR = state;
  }

  public void setDoTerminalType(boolean state) {
    doTerminalType = state;
  }

  @Override
  public void setDoDeviceType(int modelNo) {
    doDeviceType = TERMINAL_TYPES[modelNo - 2];
    LOG.debug("setting: {}", doDeviceType);
  }

  @Override
  public String toString() {
    return String.format("3270 ext ........ %s%n", does3270Extended)
        + String.format("binary .......... %s%n", doesBinary)
        + String.format("EOR ............. %s%n", doesEOR)
        + String.format("terminal type ... %s%n", doesTerminalType)
        + String.format("terminal ........ %s%n", terminal)
        + String.format("device type ..... %s%n", deviceType)
        + String.format("functions ....... %s", functions);
  }

}
